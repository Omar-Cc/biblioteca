package com.biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.biblioteca.dto.comercial.SuscripcionRequestDTO;
import com.biblioteca.dto.comercial.SuscripcionResponseDTO;
import com.biblioteca.models.acceso.Usuario;
import com.biblioteca.models.comercial.Plan;
import com.biblioteca.models.comercial.Suscripcion;

public interface SuscripcionService {
  // Operaciones CRUD básicas
  SuscripcionResponseDTO crearSuscripcion(SuscripcionRequestDTO suscripcionDTO);

  void crearSuscripcionBasicaGratuita(Usuario usuario, Plan planBasico);

  Optional<SuscripcionResponseDTO> obtenerSuscripcionPorId(Long id);

  List<SuscripcionResponseDTO> obtenerSuscripcionesPorUsuario(Long usuarioId);

  Optional<SuscripcionResponseDTO> obtenerSuscripcionActivaPorUsuario(Long usuarioId);

  List<SuscripcionResponseDTO> obtenerTodasLasSuscripciones();

  Optional<SuscripcionResponseDTO> actualizarSuscripcion(Long id, SuscripcionRequestDTO suscripcionDTO);

  boolean eliminarSuscripcion(Long id);

  // Para uso interno principalmente
  Optional<Suscripcion> obtenerEntidadSuscripcionPorId(Long id);

  // Operaciones específicas
  SuscripcionResponseDTO renovarSuscripcion(Long id);

  SuscripcionResponseDTO cambiarPlan(Long id, Long nuevoPlanId);

  SuscripcionResponseDTO cambiarPlan(Long id, Long nuevoPlanId, String modalidadPago);

  boolean cancelarSuscripcion(Long id);

  boolean pausarSuscripcion(Long id);

  boolean reactivarSuscripcion(Long id);

  // Operaciones para manejo de fechas
  List<SuscripcionResponseDTO> obtenerSuscripcionesPorVencer(int diasRestantes);

  List<SuscripcionResponseDTO> obtenerSuscripcionesVencidas();

  // Operación para verificar estado
  boolean verificarSuscripcionActiva(Long usuarioId);

  // Cambio de método de pago
  SuscripcionResponseDTO cambiarMetodoPago(Long id, Long nuevoMetodoPagoId);
}