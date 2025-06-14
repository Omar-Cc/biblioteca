package com.biblioteca.repositories.contenido;

import com.biblioteca.models.contenido.LibroFisico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibroFisicoRepository extends JpaRepository<LibroFisico, Long> {
}