package com.biblioteca.repositories.comercial;

import com.biblioteca.models.comercial.Factura;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
  boolean existsByOrdenId(Long ordenId);

  Optional<Factura> findByOrdenId(Long ordenId);

  List<Factura> findByOrden_Perfil_Id(Long perfilId); // Asumiendo que Orden tiene Perfil y Perfil tiene Id

  List<Factura> findByFechaEmisionBetween(LocalDate fechaInicio, LocalDate fechaFin);

  // Para reportes, excluyendo anuladas
  List<Factura> findByFechaEmisionBetweenAndEstadoNot(LocalDate fechaInicio, LocalDate fechaFin, String estado);

  List<Factura> findByEstadoNot(String estado);
}