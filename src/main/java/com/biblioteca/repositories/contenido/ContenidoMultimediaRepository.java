package com.biblioteca.repositories.contenido;

import com.biblioteca.models.contenido.ContenidoMultimedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContenidoMultimediaRepository extends JpaRepository<ContenidoMultimedia, Long> {
}