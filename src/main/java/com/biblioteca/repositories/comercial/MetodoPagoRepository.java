package com.biblioteca.repositories.comercial;

import com.biblioteca.models.comercial.MetodoPago;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {
  List<MetodoPago> findByActivoTrue();

  Optional<MetodoPago> findByNombre(String nombre);
  
  Optional<MetodoPago> findByNombreIgnoreCase(String nombre);

  // Buscar por tipo
  List<MetodoPago> findByTipoAndActivoTrue(String tipo);
  
  // Verificar si existe un método por nombre
  boolean existsByNombre(String nombre);
}