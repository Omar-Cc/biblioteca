package com.biblioteca.repositories.comercial;

import com.biblioteca.models.comercial.Orden;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {
  List<Orden> findByPerfilId(Long perfilId);

  List<Orden> findByEstadoOrden(String estadoOrden);

  List<Orden> findByFechaCreacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

  // Query simple sin filtros
  @Query("SELECT o FROM Orden o WHERE o.perfil.id = :perfilId ORDER BY o.fechaCreacion DESC")
  List<Orden> findByPerfilIdOrderByFechaDesc(@Param("perfilId") Long perfilId);

  @Query("SELECT o FROM Orden o WHERE o.id = :id")
  Optional<Orden> findByIdSimple(@Param("id") Long id);

  // Query con filtros dinámicos
  @Query("SELECT o FROM Orden o WHERE o.perfil.id = :perfilId " +
      "AND (:estado IS NULL OR o.estadoOrden = :estado) " +
      "AND (:fechaDesde IS NULL OR o.fechaCreacion >= :fechaDesde) " +
      "AND (:fechaHasta IS NULL OR o.fechaCreacion <= :fechaHasta) " +
      "ORDER BY o.fechaCreacion DESC")
  List<Orden> findByPerfilIdWithFilters(
      @Param("perfilId") Long perfilId,
      @Param("estado") String estado,
      @Param("fechaDesde") LocalDateTime fechaDesde,
      @Param("fechaHasta") LocalDateTime fechaHasta);
}