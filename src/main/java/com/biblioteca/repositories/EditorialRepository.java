package com.biblioteca.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biblioteca.models.contenido.Editorial;

@Repository
public interface EditorialRepository extends JpaRepository<Editorial, Long> {
}