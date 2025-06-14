package com.biblioteca.repositories.contenido;

import com.biblioteca.models.contenido.MaterialEducativo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialEducativoRepository extends JpaRepository<MaterialEducativo, Long> {
}