package com.biblioteca.repositories.comercial;

import com.biblioteca.models.comercial.Suscripcion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

  List<Suscripcion> findByUsuarioId(Long usuarioId);

  Optional<Suscripcion> findByUsuarioIdAndEstado(Long usuarioId, String estado);

  boolean existsByUsuarioIdAndEstado(Long usuarioId, String estado);

  // Para suscripciones por vencer: aquellas activas cuya fecha de renovación está
  // en un rango futuro cercano
  List<Suscripcion> findByEstadoAndFechaRenovacionBetween(String estado, LocalDate fechaDesde, LocalDate fechaHasta);

  // Para suscripciones vencidas: aquellas cuya fecha de renovación ya pasó y no
  // están activas o en prueba
  @Query("SELECT s FROM Suscripcion s WHERE s.fechaRenovacion < :fechaActual AND s.estado NOT IN ('ACTIVA', 'PRUEBA')")
  List<Suscripcion> findSuscripcionesVencidas(@Param("fechaActual") LocalDate fechaActual);

  // Para obtener todas las suscripciones de un plan específico
  List<Suscripcion> findByPlanId(Long planId);

  long countByEstado(String estado);

  List<Suscripcion> findByEstado(String estado);

  @Query("SELECT COUNT(s) FROM Suscripcion s WHERE s.estado = 'ACTIVA' AND s.fechaInicio <= :fecha")
  long countSuscripcionesActivasEnFecha(@Param("fecha") LocalDate fecha);

  @Query("SELECT COUNT(s) FROM Suscripcion s WHERE s.estado = 'CANCELADA' AND s.fechaRenovacion BETWEEN :fechaInicio AND :fechaFin")
  long countSuscripcionesCanceladasEnMes(@Param("fechaInicio") LocalDate fechaInicio,
      @Param("fechaFin") LocalDate fechaFin);

  @Query("SELECT p.nombre FROM Suscripcion s JOIN s.plan p WHERE s.estado = 'ACTIVA' GROUP BY p.nombre ORDER BY COUNT(s) DESC")
  Optional<String> findPlanMasPopular();

  @Query("SELECT COUNT(s) FROM Suscripcion s WHERE s.estado = 'ACTIVA' AND EXISTS (SELECT 1 FROM Suscripcion s2 WHERE s2.usuario = s.usuario AND s2.estado = 'PRUEBA' AND s2.fechaRenovacion < s.fechaInicio)")
  long countSuscripcionesPruebaConvertidas();

  List<Suscripcion> findByEstadoAndFechaRenovacionBefore(String estado, LocalDate fecha);

  List<Suscripcion> findByEstadoAndFechaRenovacion(String estado, LocalDate fechaRenovacion);

  @Query("SELECT s FROM Suscripcion s WHERE s.estado = 'PENDIENTE_PAGO' AND s.fechaRenovacion < :fecha")
  List<Suscripcion> findSuscripcionesPendientesVencidas(@Param("fecha") LocalDate fecha);
}