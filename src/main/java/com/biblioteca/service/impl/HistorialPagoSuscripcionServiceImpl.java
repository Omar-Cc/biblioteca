package com.biblioteca.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.comercial.HistorialPagoSuscripcionRequestDTO;
import com.biblioteca.dto.comercial.HistorialPagoSuscripcionResponseDTO;
import com.biblioteca.exceptions.RecursoNoEncontradoException;
import com.biblioteca.mapper.comercial.HistorialPagoSuscripcionMapper;
import com.biblioteca.models.comercial.HistorialPagoSuscripcion;
import com.biblioteca.models.comercial.Pago;
import com.biblioteca.models.comercial.Suscripcion;
import com.biblioteca.repositories.comercial.HistorialPagoSuscripcionRepository;
import com.biblioteca.repositories.comercial.PagoRepository;
import com.biblioteca.repositories.comercial.SuscripcionRepository;
import com.biblioteca.service.HistorialPagoSuscripcionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistorialPagoSuscripcionServiceImpl implements HistorialPagoSuscripcionService {

  private final HistorialPagoSuscripcionRepository historialPagoRepository;
  private final SuscripcionRepository suscripcionRepository; // Usar repositorio directamente
  private final PagoRepository pagoRepository; // Usar repositorio directamente
  private final HistorialPagoSuscripcionMapper historialPagoMapper;

  // Estados posibles de un pago de suscripción (se pueden mover a un Enum si se
  // prefiere)
  public static final String ESTADO_PAGADO = "PAGADO";
  public static final String ESTADO_PENDIENTE = "PENDIENTE";
  public static final String ESTADO_FALLIDO = "FALLIDO";
  public static final String ESTADO_CANCELADO = "CANCELADO";
  public static final String ESTADO_REEMBOLSADO = "REEMBOLSADO";

  @Override
  @Transactional
  public HistorialPagoSuscripcionResponseDTO registrarPago(HistorialPagoSuscripcionRequestDTO pagoDTO) {
    Suscripcion suscripcion = suscripcionRepository.findById(pagoDTO.getSuscripcionId())
        .orElseThrow(
            () -> new RecursoNoEncontradoException("Suscripción no encontrada con ID: " + pagoDTO.getSuscripcionId()));

    HistorialPagoSuscripcion historialPago = historialPagoMapper.toEntity(pagoDTO);
    // El ID será generado por la base de datos
    historialPago.setSuscripcion(suscripcion);

    if (pagoDTO.getPagoId() != null) {
      Pago pago = pagoRepository.findById(pagoDTO.getPagoId())
          .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado con ID: " + pagoDTO.getPagoId()));
      historialPago.setPago(pago);
    }

    if (historialPago.getFechaPago() == null) {
      historialPago.setFechaPago(LocalDateTime.now());
    }
    if (historialPago.getEstado() == null || historialPago.getEstado().isEmpty()) {
      historialPago.setEstado(ESTADO_PENDIENTE);
    }
    if (historialPago.getPeriodo() == null || historialPago.getPeriodo().isEmpty()) {
      historialPago.setPeriodo(generarPeriodo(
          suscripcion.getFechaRenovacion() != null ? suscripcion.getFechaRenovacion() : LocalDate.now()));
    }

    // La relación bidireccional se maneja al guardar la entidad dueña
    // (HistorialPagoSuscripcion)
    // o si Suscripcion tiene CascadeType.PERSIST o ALL en su colección de
    // historialPagos.
    // suscripcion.addHistorialPago(historialPago); // No es estrictamente necesario
    // si la relación está bien mapeada

    HistorialPagoSuscripcion pagoGuardado = historialPagoRepository.save(historialPago);
    return historialPagoMapper.toResponseDTO(pagoGuardado);
  }

  private String generarPeriodo(LocalDate fechaReferencia) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    return fechaReferencia.format(formatter);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<HistorialPagoSuscripcionResponseDTO> obtenerPagoPorId(Long id) {
    return historialPagoRepository.findById(id)
        .map(historialPagoMapper::toResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public List<HistorialPagoSuscripcionResponseDTO> obtenerPagosPorSuscripcion(Long suscripcionId) {
    return historialPagoRepository.findBySuscripcionId(suscripcionId).stream()
        .map(historialPagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Optional<HistorialPagoSuscripcionResponseDTO> actualizarPago(Long id,
      HistorialPagoSuscripcionRequestDTO pagoDTO) {
    return historialPagoRepository.findById(id)
        .map(historialPago -> {
          if (pagoDTO.getEstado() != null && !pagoDTO.getEstado().isEmpty()) {
            historialPago.setEstado(pagoDTO.getEstado());
          }
          if (pagoDTO.getMonto() != null) {
            historialPago.setMonto(pagoDTO.getMonto());
          }
          if (pagoDTO.getPeriodo() != null && !pagoDTO.getPeriodo().isEmpty()) {
            historialPago.setPeriodo(pagoDTO.getPeriodo());
          }
          if (pagoDTO.getPagoId() != null) {
            Pago pago = pagoRepository.findById(pagoDTO.getPagoId())
                .orElseThrow(
                    () -> new RecursoNoEncontradoException("Pago no encontrado con ID: " + pagoDTO.getPagoId()));
            historialPago.setPago(pago);
          }
          // Actualizar fecha de pago si se proporciona
          if (pagoDTO.getFechaPago() != null) {
            historialPago.setFechaPago(pagoDTO.getFechaPago());
          }

          HistorialPagoSuscripcion pagoActualizado = historialPagoRepository.save(historialPago);
          return historialPagoMapper.toResponseDTO(pagoActualizado);
        });
  }

  @Override
  @Transactional
  public boolean eliminarPago(Long id) {
    if (historialPagoRepository.existsById(id)) {
      historialPagoRepository.deleteById(id);
      return true;
    }
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<HistorialPagoSuscripcion> obtenerEntidadPagoPorId(Long id) {
    return historialPagoRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<HistorialPagoSuscripcionResponseDTO> obtenerPagosPorEstado(String estado) {
    return historialPagoRepository.findByEstado(estado).stream()
        .map(historialPagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<HistorialPagoSuscripcionResponseDTO> obtenerPagosPorRangoFechas(LocalDate fechaInicio,
      LocalDate fechaFin) {
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay().minusNanos(1); // Para incluir todo el día de fechaFin

    return historialPagoRepository.findByFechaPagoBetween(inicio, fin).stream()
        .map(historialPagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public HistorialPagoSuscripcionResponseDTO marcarComoPagado(Long id) {
    HistorialPagoSuscripcion historialPago = historialPagoRepository.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Historial de pago no encontrado con ID: " + id));
    historialPago.setEstado(ESTADO_PAGADO);
    // Podría ser útil actualizar la fecha de pago aquí si no estaba ya establecida
    // historialPago.setFechaPago(LocalDateTime.now());
    HistorialPagoSuscripcion pagoGuardado = historialPagoRepository.save(historialPago);
    return historialPagoMapper.toResponseDTO(pagoGuardado);
  }

  @Override
  @Transactional
  public HistorialPagoSuscripcionResponseDTO marcarComoFallido(Long id, String motivoFallo) {
    HistorialPagoSuscripcion historialPago = historialPagoRepository.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Historial de pago no encontrado con ID: " + id));
    historialPago.setEstado(ESTADO_FALLIDO);
    // Considerar añadir el motivoFallo a un campo en la entidad
    // HistorialPagoSuscripcion si existe
    // ejemplo: historialPago.setMotivoFallo(motivoFallo);
    HistorialPagoSuscripcion pagoGuardado = historialPagoRepository.save(historialPago);
    return historialPagoMapper.toResponseDTO(pagoGuardado);
  }

  @Override
  @Transactional(readOnly = true)
  public double calcularTotalPagosPorPeriodo(Long suscripcionId, String periodo) {
    return historialPagoRepository.findBySuscripcionIdAndPeriodoAndEstado(suscripcionId, periodo, ESTADO_PAGADO)
        .stream()
        .mapToDouble(p -> p.getMonto() != null ? p.getMonto() / 100.0 : 0.0) // Convertir centavos a unidades
        .sum();
  }

  @Override
  @Transactional(readOnly = true)
  public double calcularTotalPagosPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay().minusNanos(1);

    return historialPagoRepository.findByFechaPagoBetweenAndEstado(inicio, fin, ESTADO_PAGADO).stream()
        .mapToDouble(p -> p.getMonto() != null ? p.getMonto() / 100.0 : 0.0) // Convertir centavos a unidades
        .sum();
  }

  @Override
  @Transactional
  public HistorialPagoSuscripcionResponseDTO vincularConPagoGeneral(Long id, Long pagoId) {
    HistorialPagoSuscripcion historialPago = historialPagoRepository.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Historial de pago no encontrado con ID: " + id));
    Pago pagoGeneral = pagoRepository.findById(pagoId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Pago general no encontrado con ID: " + pagoId));

    historialPago.setPago(pagoGeneral);

    if (ESTADO_PAGADO.equals(pagoGeneral.getEstado())) {
      historialPago.setEstado(ESTADO_PAGADO);
      if (historialPago.getFechaPago() == null && pagoGeneral.getFechaPago() != null) {
        historialPago.setFechaPago(pagoGeneral.getFechaPago());
      }
    }
    HistorialPagoSuscripcion pagoGuardado = historialPagoRepository.save(historialPago);
    return historialPagoMapper.toResponseDTO(pagoGuardado);
  }
}