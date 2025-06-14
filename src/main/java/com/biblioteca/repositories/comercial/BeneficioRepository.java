package com.biblioteca.repositories.comercial;

import com.biblioteca.models.comercial.Beneficio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BeneficioRepository extends JpaRepository<Beneficio, Long> {
  List<Beneficio> findByTipoDato(String tipoDato);

  List<Beneficio> findByActivoTrue();

  List<Beneficio> findByActivo(boolean activo);
}