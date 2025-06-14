package com.biblioteca.repositories.contenido;

import com.biblioteca.models.contenido.PublicacionIlustradaFisica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicacionIlustradaFisicaRepository extends JpaRepository<PublicacionIlustradaFisica, Long> {
    // Repositorio para la clase abstracta PublicacionIlustradaFisica.
}