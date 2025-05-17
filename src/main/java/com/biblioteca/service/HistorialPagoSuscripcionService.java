package com.biblioteca.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.biblioteca.dto.comercial.HistorialPagoSuscripcionRequestDTO;
import com.biblioteca.dto.comercial.HistorialPagoSuscripcionResponseDTO;
import com.biblioteca.models.comercial.HistorialPagoSuscripcion;

public interface HistorialPagoSuscripcionService {
  // Operaciones CRUD básicas
  HistorialPagoSuscripcionResponseDTO registrarPago(HistorialPagoSuscripcionRequestDTO pagoDTO);

  Optional<HistorialPagoSuscripcionResponseDTO> obtenerPagoPorId(Long id);

  List<HistorialPagoSuscripcionResponseDTO> obtenerPagosPorSuscripcion(Long suscripcionId);

  Optional<HistorialPagoSuscripcionResponseDTO> actualizarPago(Long id,
      HistorialPagoSuscripcionRequestDTO pagoDTO);

  boolean eliminarPago(Long id);

  // Para uso interno principalmente
  Optional<HistorialPagoSuscripcion> obtenerEntidadPagoPorId(Long id);

  // Operaciones específicas por estado y fecha
  List<HistorialPagoSuscripcionResponseDTO> obtenerPagosPorEstado(String estado);

  List<HistorialPagoSuscripcionResponseDTO> obtenerPagosPorRangoFechas(LocalDate fechaInicio,
      LocalDate fechaFin);

  // Operaciones para facturación
  HistorialPagoSuscripcionResponseDTO marcarComoPagado(Long id);

  HistorialPagoSuscripcionResponseDTO marcarComoFallido(Long id, String motivoFallo);

  // Operaciones para reportes
  double calcularTotalPagosPorPeriodo(Long suscripcionId, String periodo);

  double calcularTotalPagosPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);

  // Vincular con pago general
  HistorialPagoSuscripcionResponseDTO vincularConPagoGeneral(Long id, Long pagoId);
}