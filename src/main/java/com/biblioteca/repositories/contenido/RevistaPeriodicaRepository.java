package com.biblioteca.repositories.contenido;

import com.biblioteca.models.contenido.RevistaPeriodica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RevistaPeriodicaRepository extends JpaRepository<RevistaPeriodica, Long> {
}