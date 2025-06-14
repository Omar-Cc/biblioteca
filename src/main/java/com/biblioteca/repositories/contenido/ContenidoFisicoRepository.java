package com.biblioteca.repositories.contenido;

import com.biblioteca.models.contenido.ContenidoFisico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContenidoFisicoRepository extends JpaRepository<ContenidoFisico, Long> {
    // Repositorio para la clase abstracta ContenidoFisico.
}