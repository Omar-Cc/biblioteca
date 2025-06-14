package com.biblioteca.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biblioteca.models.contenido.Serie;

@Repository
public interface SerieRepository extends JpaRepository<Serie, Long> {
}