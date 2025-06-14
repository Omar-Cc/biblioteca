package com.biblioteca.repositories.comercial;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biblioteca.models.comercial.HistorialPagoSuscripcion;

@Repository
public interface HistorialPagoSuscripcionRepository extends JpaRepository<HistorialPagoSuscripcion, Long> {
  List<HistorialPagoSuscripcion> findBySuscripcionId(Long suscripcionId);

  List<HistorialPagoSuscripcion> findByEstado(String estado);

  List<HistorialPagoSuscripcion> findByFechaPagoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

  List<HistorialPagoSuscripcion> findBySuscripcionIdAndPeriodoAndEstado(Long suscripcionId, String periodo,
      String estado);

  List<HistorialPagoSuscripcion> findByFechaPagoBetweenAndEstado(LocalDateTime fechaInicio, LocalDateTime fechaFin,
      String estado);
}
