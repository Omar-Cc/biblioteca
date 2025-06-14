package com.biblioteca.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.biblioteca.dto.comercial.PagoRequestDTO;
import com.biblioteca.dto.comercial.PagoResponseDTO;
import com.biblioteca.enums.EstadoPago;
import com.biblioteca.models.comercial.Pago;

public interface PagoService {
  // Operaciones CRUD básicas
  PagoResponseDTO registrarPago(PagoRequestDTO pagoDTO);

  Optional<PagoResponseDTO> obtenerPagoPorId(Long id);

  List<PagoResponseDTO> obtenerPagosPorOrden(Long ordenId);

  List<PagoResponseDTO> obtenerTodosLosPagos();

  Optional<PagoResponseDTO> actualizarPago(Long id, PagoRequestDTO pagoDTO);

  boolean eliminarPago(Long id);

  // Para uso interno principalmente
  Optional<Pago> obtenerEntidadPagoPorId(Long id);

  // Operaciones específicas por estado y fecha
  List<PagoResponseDTO> obtenerPagosPorEstado(EstadoPago estado);

  List<PagoResponseDTO> obtenerPagosPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);

  // Operaciones específicas por método de pago
  List<PagoResponseDTO> obtenerPagosPorMetodoPago(Long metodoPagoId);

  // Operaciones para cambio de estado
  PagoResponseDTO procesarPago(Long id);

  PagoResponseDTO aprobarPago(Long id, String referenciaPago);

  PagoResponseDTO rechazarPago(Long id, String motivo);

  // Operaciones para integración con pasarelas de pago
  PagoResponseDTO verificarEstadoConPasarela(Long id);

  // Operaciones para reportes
  double calcularTotalPagosPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);

  double calcularTotalPagosPorMetodoPago(Long metodoPagoId, LocalDate fechaInicio, LocalDate fechaFin);

  // ⭐ MÉTODOS UNIFICADOS PARA SUSCRIPCIONES
  PagoResponseDTO procesarPagoSuscripcion(Long suscripcionId, Long metodoPagoId, Double monto, String periodo);
  
  PagoResponseDTO simularRenovacionAutomatica(Long suscripcionId);
  
  List<PagoResponseDTO> obtenerPagosPorSuscripcion(Long suscripcionId);
  
  List<PagoResponseDTO> obtenerPagosUnificadosPorUsuario(Long usuarioId);
}