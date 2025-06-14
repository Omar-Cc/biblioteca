package com.biblioteca.repositories.comercial;

import com.biblioteca.models.comercial.Carrito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {
  Optional<Carrito> findByPerfil_Id(Long perfilId);
  
  // Buscar carritos con items
  @Query("SELECT c FROM Carrito c WHERE c.perfil.id = :perfilId AND SIZE(c.items) > 0")
  Optional<Carrito> findByPerfilIdWithItems(@Param("perfilId") Long perfilId);
  
  // Buscar carritos abandonados (sin actividad por cierto tiempo)
  List<Carrito> findByFechaCreacionBefore(LocalDateTime fecha);
  
  // Buscar carritos abandonados con última actividad antes de fecha específica
  @Query("SELECT c FROM Carrito c WHERE c.fechaCreacion < :fechaLimite AND SIZE(c.items) > 0")
  List<Carrito> findCarritosAbandonados(@Param("fechaLimite") LocalDateTime fechaLimite);
  
  // Contar carritos activos (con items)
  @Query("SELECT COUNT(c) FROM Carrito c WHERE SIZE(c.items) > 0")
  long countCarritosActivos();
  
  // Buscar carritos por usuario
  @Query("SELECT c FROM Carrito c WHERE c.perfil.usuario.id = :usuarioId")
  List<Carrito> findByUsuarioId(@Param("usuarioId") Long usuarioId);
  
  // Verificar si existe carrito con items para un perfil
  @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Carrito c WHERE c.perfil.id = :perfilId AND SIZE(c.items) > 0")
  boolean existsCarritoWithItemsByPerfilId(@Param("perfilId") Long perfilId);
}