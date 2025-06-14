package com.biblioteca.repositories.contenido;

import com.biblioteca.models.contenido.AudioLibro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AudioLibroRepository extends JpaRepository<AudioLibro, Long> {
}