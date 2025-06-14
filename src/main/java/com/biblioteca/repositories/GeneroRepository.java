package com.biblioteca.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biblioteca.models.contenido.Genero;

@Repository
public interface GeneroRepository extends JpaRepository<Genero, Long> {

  List<Genero> findByPadreIsNull();

  List<Genero> findByPadreIsNullAndSubgenerosIsEmpty();

  Optional<Genero> findByNombre(String nombre);
}