package com.biblioteca.repositories.comercial;

import com.biblioteca.enums.EstadoPago;
import com.biblioteca.models.comercial.Pago;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

  @EntityGraph(attributePaths = {
      "orden",
      "orden.perfil",
      "orden.perfil.usuario",
      "orden.items",
      "suscripcion",
      "suscripcion.usuario",
      "suscripcion.usuario.lector",
      "suscripcion.plan",
      "metodoPago"
  })
  Optional<Pago> findWithAllRelationsById(Long id);

  List<Pago> findByOrdenId(Long ordenId);

  // ⭐ NUEVOS MÉTODOS PARA SUSCRIPCIONES
  List<Pago> findBySuscripcionId(Long suscripcionId);

  List<Pago> findBySuscripcionUsuarioId(Long usuarioId);

  List<Pago> findByEstado(EstadoPago estado);

  List<Pago> findByFechaPagoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

  List<Pago> findByMetodoPagoId(Long metodoPagoId);

  // Para los cálculos de totales, filtrando por estado "EXITOSO"
  List<Pago> findByFechaPagoBetweenAndEstado(LocalDateTime fechaInicio, LocalDateTime fechaFin, EstadoPago estado);

  List<Pago> findByMetodoPagoIdAndFechaPagoBetweenAndEstado(Long metodoPagoId, LocalDateTime fechaInicio,
      LocalDateTime fechaFin, EstadoPago estado);
}