package com.biblioteca.repositories.contenido;

import com.biblioteca.models.contenido.ContenidoDigital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContenidoDigitalRepository extends JpaRepository<ContenidoDigital, Long> {
    // Repositorio para la clase abstracta ContenidoDigital.
}