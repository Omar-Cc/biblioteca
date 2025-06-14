package com.biblioteca.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biblioteca.models.acceso.Lector;

@Repository
public interface LectorRepository extends JpaRepository<Lector, Long> {

  boolean existsByUsuarioUsername(String username);

  Optional<Lector> findByUsuarioUsername(String username);

  List<Lector> findByApellidoContainingIgnoreCase(String apellido);
}