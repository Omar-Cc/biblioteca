package com.biblioteca.service.impl;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.biblioteca.dto.comercial.PagoRequestDTO;
import com.biblioteca.dto.comercial.PagoResponseDTO;
import com.biblioteca.mapper.comercial.PagoMapper;
import com.biblioteca.models.comercial.MetodoPago;
import com.biblioteca.models.comercial.Orden;
import com.biblioteca.models.comercial.Pago;
import com.biblioteca.service.MetodoPagoService;
import com.biblioteca.service.OrdenService;
import com.biblioteca.service.PagoService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

  private final Map<Long, Pago> pagos = new ConcurrentHashMap<>();
  private final AtomicLong pagoIdCounter = new AtomicLong(0);
  private final PagoMapper pagoMapper;
  private final OrdenService ordenService;
  private final MetodoPagoService metodoPagoService;
  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;

  // Estados posibles de un pago
  public static final String ESTADO_PENDIENTE = "Pendiente";
  public static final String ESTADO_PROCESANDO = "Procesando";
  public static final String ESTADO_COMPLETADO = "Completado";
  public static final String ESTADO_RECHAZADO = "Rechazado";
  public static final String ESTADO_REEMBOLSADO = "Reembolsado";
  public static final String ESTADO_FALLIDO = "Fallido";

  @PostConstruct
  public void initPagosData() {
    try {
      InputStream inputStream = resourceLoader.getResource("classpath:data/pagos-data.json").getInputStream();
      List<PagoRequestDTO> pagosDTOs = objectMapper.readValue(inputStream,
          new TypeReference<List<PagoRequestDTO>>() {
          });
      pagosDTOs.forEach(this::registrarPago);
      System.out.println("Datos iniciales de Pagos cargados desde JSON: " + pagos.size() + " pagos.");
    } catch (Exception e) {
      System.err.println("Error al cargar datos iniciales de pagos desde JSON: " + e.getMessage());
      // No iniciamos con datos por defecto ya que dependemos de órdenes existentes
    }
  }

  @Override
  public PagoResponseDTO registrarPago(PagoRequestDTO pagoDTO) {
    Orden orden = ordenService.obtenerEntidadOrdenPorId(pagoDTO.getOrdenId())
        .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + pagoDTO.getOrdenId()));

    MetodoPago metodoPago = metodoPagoService.obtenerEntidadMetodoPagoPorId(pagoDTO.getMetodoPagoId())
        .orElseThrow(
            () -> new IllegalArgumentException("Método de pago no encontrado con ID: " + pagoDTO.getMetodoPagoId()));

    Pago pago = pagoMapper.toEntity(pagoDTO);
    pago.setId(pagoIdCounter.incrementAndGet());
    pago.setOrden(orden);
    pago.setMetodoPago(metodoPago);
    pago.setFechaPago(LocalDateTime.now());

    // Si no se especifica el estado, asignar pendiente
    if (pago.getEstado() == null || pago.getEstado().isEmpty()) {
      pago.setEstado(ESTADO_PENDIENTE);
    }

    orden.addPago(pago);

    pagos.put(pago.getId(), pago);
    return pagoMapper.toResponseDTO(pago);
  }

  @Override
  public Optional<PagoResponseDTO> obtenerPagoPorId(Long id) {
    return Optional.ofNullable(pagos.get(id))
        .map(pagoMapper::toResponseDTO);
  }

  @Override
  public List<PagoResponseDTO> obtenerPagosPorOrden(Long ordenId) {
    return pagos.values().stream()
        .filter(p -> p.getOrden() != null && p.getOrden().getId().equals(ordenId))
        .map(pagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<PagoResponseDTO> obtenerTodosLosPagos() {
    return pagos.values().stream()
        .map(pagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<PagoResponseDTO> actualizarPago(Long id, PagoRequestDTO pagoDTO) {
    return Optional.ofNullable(pagos.get(id))
        .map(pago -> {
          // No permitimos cambiar la orden

          // Actualizar método de pago si se proporciona nuevo
          if (pagoDTO.getMetodoPagoId() != null &&
              !pago.getMetodoPago().getId().equals(pagoDTO.getMetodoPagoId())) {
            MetodoPago metodoPago = metodoPagoService.obtenerEntidadMetodoPagoPorId(pagoDTO.getMetodoPagoId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Método de pago no encontrado con ID: " + pagoDTO.getMetodoPagoId()));
            pago.setMetodoPago(metodoPago);
          }

          // Actualizar monto si se proporciona
          if (pagoDTO.getMonto() != null) {
            pago.setMonto(pagoDTO.getMonto());
          }

          // Actualizar referencia si se proporciona
          if (pagoDTO.getReferenciaPago() != null) {
            pago.setReferenciaPago(pagoDTO.getReferenciaPago());
          }

          // Actualizar estado si se proporciona
          if (pagoDTO.getEstado() != null && !pagoDTO.getEstado().isEmpty()) {
            pago.setEstado(pagoDTO.getEstado());
          }

          return pagoMapper.toResponseDTO(pago);
        });
  }

  @Override
  public boolean eliminarPago(Long id) {
    Pago pago = pagos.remove(id);
    if (pago != null && pago.getOrden() != null) {
      pago.getOrden().removePago(pago);
      return true;
    }
    return false;
  }

  @Override
  public Optional<Pago> obtenerEntidadPagoPorId(Long id) {
    return Optional.ofNullable(pagos.get(id));
  }

  @Override
  public List<PagoResponseDTO> obtenerPagosPorEstado(String estado) {
    return pagos.values().stream()
        .filter(p -> estado.equals(p.getEstado()))
        .map(pagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<PagoResponseDTO> obtenerPagosPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay();

    return pagos.values().stream()
        .filter(p -> !p.getFechaPago().isBefore(inicio) && p.getFechaPago().isBefore(fin))
        .map(pagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<PagoResponseDTO> obtenerPagosPorMetodoPago(Long metodoPagoId) {
    return pagos.values().stream()
        .filter(p -> p.getMetodoPago() != null && p.getMetodoPago().getId().equals(metodoPagoId))
        .map(pagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public PagoResponseDTO procesarPago(Long id) {
    Pago pago = obtenerEntidadPagoPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + id));

    if (!ESTADO_PENDIENTE.equals(pago.getEstado())) {
      throw new IllegalStateException("Solo se pueden procesar pagos en estado pendiente");
    }

    pago.setEstado(ESTADO_PROCESANDO);
    return pagoMapper.toResponseDTO(pago);
  }

  @Override
  public PagoResponseDTO aprobarPago(Long id, String referenciaPago) {
    Pago pago = obtenerEntidadPagoPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + id));

    if (!ESTADO_PROCESANDO.equals(pago.getEstado()) && !ESTADO_PENDIENTE.equals(pago.getEstado())) {
      throw new IllegalStateException("Solo se pueden aprobar pagos en estado pendiente o procesando");
    }

    pago.setEstado(ESTADO_COMPLETADO);
    if (referenciaPago != null && !referenciaPago.isEmpty()) {
      pago.setReferenciaPago(referenciaPago);
    }

    // Si el pago es para una orden, podemos actualizar su estado
    if (pago.getOrden() != null) {
      try {
        // Si el pago completa el total de la orden, marcarla como completada
        Orden orden = pago.getOrden();
        Integer totalPagado = orden.getPagos().stream()
            .filter(p -> ESTADO_COMPLETADO.equals(p.getEstado()))
            .mapToInt(p -> p.getMonto() != null ? p.getMonto() : 0)
            .sum();

        if (totalPagado >= orden.getTotalOrden()) {
          ordenService.completarOrden(orden.getId());
        }
      } catch (Exception e) {
        // Capturar cualquier error pero no fallar la aprobación del pago
        System.err.println("Error al actualizar estado de la orden: " + e.getMessage());
      }
    }

    return pagoMapper.toResponseDTO(pago);
  }

  @Override
  public PagoResponseDTO rechazarPago(Long id, String motivo) {
    Pago pago = obtenerEntidadPagoPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + id));

    if (ESTADO_COMPLETADO.equals(pago.getEstado()) || ESTADO_REEMBOLSADO.equals(pago.getEstado())) {
      throw new IllegalStateException("No se pueden rechazar pagos completados o reembolsados");
    }

    pago.setEstado(ESTADO_RECHAZADO);
    // Almacenar el motivo o en un registro de eventos

    return pagoMapper.toResponseDTO(pago);
  }

  @Override
  public PagoResponseDTO verificarEstadoConPasarela(Long id) {
    Pago pago = obtenerEntidadPagoPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + id));

    System.out.println("Verificando estado del pago " + id + " con pasarela de pago externa");

    return pagoMapper.toResponseDTO(pago);
  }

  @Override
  public double calcularTotalPagosPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay();

    return pagos.values().stream()
        .filter(p -> !p.getFechaPago().isBefore(inicio) && p.getFechaPago().isBefore(fin))
        .filter(p -> ESTADO_COMPLETADO.equals(p.getEstado()))
        .mapToDouble(p -> p.getMonto() != null ? p.getMonto() / 100.0 : 0)
        .sum();
  }

  @Override
  public double calcularTotalPagosPorMetodoPago(Long metodoPagoId, LocalDate fechaInicio, LocalDate fechaFin) {
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay();

    return pagos.values().stream()
        .filter(p -> p.getMetodoPago() != null && p.getMetodoPago().getId().equals(metodoPagoId))
        .filter(p -> !p.getFechaPago().isBefore(inicio) && p.getFechaPago().isBefore(fin))
        .filter(p -> ESTADO_COMPLETADO.equals(p.getEstado()))
        .mapToDouble(p -> p.getMonto() != null ? p.getMonto() / 100.0 : 0)
        .sum();
  }
}