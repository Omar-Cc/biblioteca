package com.biblioteca.repositories.comercial;

import com.biblioteca.models.comercial.ItemCarrito;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {
  
  // Buscar item específico en carrito
  Optional<ItemCarrito> findByCarritoIdAndContenidoId(Long carritoId, Long contenidoId);
  
  // Buscar todos los items de un carrito
  List<ItemCarrito> findByCarritoId(Long carritoId);
  
  // Buscar items por contenido específico
  List<ItemCarrito> findByContenidoId(Long contenidoId);
  
  // Verificar si existe item en carrito
  boolean existsByCarritoIdAndContenidoId(Long carritoId, Long contenidoId);
  
  // Eliminar todos los items de un carrito
  void deleteByCarritoId(Long carritoId);
  
  // Sumar cantidad total de items en un carrito
  @Query("SELECT COALESCE(SUM(i.cantidad), 0) FROM ItemCarrito i WHERE i.carrito.id = :carritoId")
  int sumCantidadByCarritoId(@Param("carritoId") Long carritoId);
  
  // Calcular valor total del carrito
  @Query("SELECT COALESCE(SUM(i.precio * i.cantidad), 0) FROM ItemCarrito i WHERE i.carrito.id = :carritoId")
  int calcularTotalCarrito(@Param("carritoId") Long carritoId);
  
  // Buscar items con stock insuficiente
  @Query("SELECT i FROM ItemCarrito i JOIN i.contenido c WHERE c.tipo = 'LIBRO_FISICO' AND c.stockDisponible < i.cantidad")
  List<ItemCarrito> findItemsConStockInsuficiente();
}