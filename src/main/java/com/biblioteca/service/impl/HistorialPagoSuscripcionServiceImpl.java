package com.biblioteca.service.impl;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.biblioteca.dto.comercial.HistorialPagoSuscripcionRequestDTO;
import com.biblioteca.dto.comercial.HistorialPagoSuscripcionResponseDTO;
import com.biblioteca.mapper.comercial.HistorialPagoSuscripcionMapper;
import com.biblioteca.models.comercial.HistorialPagoSuscripcion;
import com.biblioteca.models.comercial.Pago;
import com.biblioteca.models.comercial.Suscripcion;
import com.biblioteca.service.HistorialPagoSuscripcionService;
import com.biblioteca.service.PagoService;
import com.biblioteca.service.SuscripcionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistorialPagoSuscripcionServiceImpl implements HistorialPagoSuscripcionService {

  private final List<HistorialPagoSuscripcion> historialPagos = new ArrayList<>();
  private final AtomicLong historialPagoIdCounter = new AtomicLong(0);
  private final HistorialPagoSuscripcionMapper historialPagoMapper;
  private final SuscripcionService suscripcionService;
  private final PagoService pagoService;
  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;

  // Estados posibles de un pago de suscripción
  public static final String ESTADO_PAGADO = "PAGADO";
  public static final String ESTADO_PENDIENTE = "PENDIENTE";
  public static final String ESTADO_FALLIDO = "FALLIDO";
  public static final String ESTADO_CANCELADO = "CANCELADO";
  public static final String ESTADO_REEMBOLSADO = "REEMBOLSADO";

  @PostConstruct
  public void initHistorialPagosData() {
    try {
      InputStream inputStream = resourceLoader.getResource("classpath:data/historial-pagos-suscripcion-data.json")
          .getInputStream();
      List<HistorialPagoSuscripcionRequestDTO> historialPagosDTOs = objectMapper.readValue(inputStream,
          new TypeReference<List<HistorialPagoSuscripcionRequestDTO>>() {
          });
      historialPagosDTOs.forEach(this::registrarPago);
      System.out.println(
          "Datos iniciales de Historial de Pagos cargados desde JSON: " + historialPagos.size() + " registros.");
    } catch (Exception e) {
      System.err.println("Error al cargar datos iniciales de historial de pagos desde JSON: " + e.getMessage());
      // No cargamos datos por defecto porque dependemos de suscripciones existentes
    }
  }

  @Override
  public HistorialPagoSuscripcionResponseDTO registrarPago(HistorialPagoSuscripcionRequestDTO pagoDTO) {

    Suscripcion suscripcion = suscripcionService.obtenerEntidadSuscripcionPorId(pagoDTO.getSuscripcionId())
        .orElseThrow(
            () -> new IllegalArgumentException("Suscripción no encontrada con ID: " + pagoDTO.getSuscripcionId()));

    HistorialPagoSuscripcion historialPago = historialPagoMapper.toEntity(pagoDTO);
    historialPago.setId(historialPagoIdCounter.incrementAndGet());
    historialPago.setSuscripcion(suscripcion);

    // Si se proporciona un ID de pago general, intentar vincularlo
    if (pagoDTO.getPagoId() != null) {
      Pago pago = pagoService.obtenerEntidadPagoPorId(pagoDTO.getPagoId())
          .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + pagoDTO.getPagoId()));
      historialPago.setPago(pago);
    }

    // Establecer fecha de pago y estado por defecto si es necesario
    if (historialPago.getFechaPago() == null) {
      historialPago.setFechaPago(LocalDateTime.now());
    }

    if (historialPago.getEstado() == null || historialPago.getEstado().isEmpty()) {
      historialPago.setEstado(ESTADO_PENDIENTE);
    }

    // Generar período si no se proporciona
    if (historialPago.getPeriodo() == null || historialPago.getPeriodo().isEmpty()) {
      historialPago.setPeriodo(generarPeriodo(suscripcion.getFechaRenovacion()));
    }

    // Mantener la relación bidireccional con la suscripción
    suscripcion.addHistorialPago(historialPago);

    historialPagos.add(historialPago);
    return historialPagoMapper.toResponseDTO(historialPago);
  }

  private String generarPeriodo(LocalDate fechaRenovacion) {
    // Formatear el periodo como "MMMM yyyy" (por ejemplo, "Mayo 2024")
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    return fechaRenovacion.format(formatter);
  }

  @Override
  public Optional<HistorialPagoSuscripcionResponseDTO> obtenerPagoPorId(Long id) {
    return historialPagos.stream()
        .filter(p -> p.getId().equals(id))
        .findFirst()
        .map(historialPagoMapper::toResponseDTO);
  }

  @Override
  public List<HistorialPagoSuscripcionResponseDTO> obtenerPagosPorSuscripcion(Long suscripcionId) {
    return historialPagos.stream()
        .filter(p -> p.getSuscripcion().getId().equals(suscripcionId))
        .map(historialPagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<HistorialPagoSuscripcionResponseDTO> actualizarPago(Long id,
      HistorialPagoSuscripcionRequestDTO pagoDTO) {
    return obtenerEntidadPagoPorId(id)
        .map(historialPago -> {
          // Actualizar estado si se proporciona
          if (pagoDTO.getEstado() != null && !pagoDTO.getEstado().isEmpty()) {
            historialPago.setEstado(pagoDTO.getEstado());
          }

          // Actualizar monto si se proporciona
          if (pagoDTO.getMonto() != null) {
            historialPago.setMonto(pagoDTO.getMonto());
          }

          // Actualizar periodo si se proporciona
          if (pagoDTO.getPeriodo() != null && !pagoDTO.getPeriodo().isEmpty()) {
            historialPago.setPeriodo(pagoDTO.getPeriodo());
          }

          // Actualizar pago general si se proporciona
          if (pagoDTO.getPagoId() != null) {
            Pago pago = pagoService.obtenerEntidadPagoPorId(pagoDTO.getPagoId())
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + pagoDTO.getPagoId()));
            historialPago.setPago(pago);
          }

          return historialPagoMapper.toResponseDTO(historialPago);
        });
  }

  @Override
  public boolean eliminarPago(Long id) {
    return historialPagos.removeIf(p -> p.getId().equals(id));
  }

  @Override
  public Optional<HistorialPagoSuscripcion> obtenerEntidadPagoPorId(Long id) {
    return historialPagos.stream()
        .filter(p -> p.getId().equals(id))
        .findFirst();
  }

  @Override
  public List<HistorialPagoSuscripcionResponseDTO> obtenerPagosPorEstado(String estado) {
    return historialPagos.stream()
        .filter(p -> estado.equals(p.getEstado()))
        .map(historialPagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<HistorialPagoSuscripcionResponseDTO> obtenerPagosPorRangoFechas(LocalDate fechaInicio,
      LocalDate fechaFin) {
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay();

    return historialPagos.stream()
        .filter(p -> !p.getFechaPago().isBefore(inicio) && p.getFechaPago().isBefore(fin))
        .map(historialPagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public HistorialPagoSuscripcionResponseDTO marcarComoPagado(Long id) {
    HistorialPagoSuscripcion historialPago = obtenerEntidadPagoPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Historial de pago no encontrado con ID: " + id));

    historialPago.setEstado(ESTADO_PAGADO);

    return historialPagoMapper.toResponseDTO(historialPago);
  }

  @Override
  public HistorialPagoSuscripcionResponseDTO marcarComoFallido(Long id, String motivoFallo) {
    HistorialPagoSuscripcion historialPago = obtenerEntidadPagoPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Historial de pago no encontrado con ID: " + id));

    historialPago.setEstado(ESTADO_FALLIDO);

    // Agregar el motivo o en observaciones

    return historialPagoMapper.toResponseDTO(historialPago);
  }

  @Override
  public double calcularTotalPagosPorPeriodo(Long suscripcionId, String periodo) {
    return historialPagos.stream()
        .filter(p -> p.getSuscripcion().getId().equals(suscripcionId))
        .filter(p -> periodo.equals(p.getPeriodo()))
        .filter(p -> ESTADO_PAGADO.equals(p.getEstado()))
        .mapToDouble(p -> p.getMonto() / 100.0) // Convertir centavos a unidades
        .sum();
  }

  @Override
  public double calcularTotalPagosPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay();

    return historialPagos.stream()
        .filter(p -> !p.getFechaPago().isBefore(inicio) && p.getFechaPago().isBefore(fin))
        .filter(p -> ESTADO_PAGADO.equals(p.getEstado()))
        .mapToDouble(p -> p.getMonto() / 100.0) // Convertir centavos a unidades
        .sum();
  }

  @Override
  public HistorialPagoSuscripcionResponseDTO vincularConPagoGeneral(Long id, Long pagoId) {
    HistorialPagoSuscripcion historialPago = obtenerEntidadPagoPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Historial de pago no encontrado con ID: " + id));

    Pago pago = pagoService.obtenerEntidadPagoPorId(pagoId)
        .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + pagoId));

    historialPago.setPago(pago);

    // Si el pago general está pagado, actualizar también el estado del pago de
    // suscripción
    if ("PAGADO".equals(pago.getEstado())) {
      historialPago.setEstado(ESTADO_PAGADO);
    }

    return historialPagoMapper.toResponseDTO(historialPago);
  }
}