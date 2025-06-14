package com.biblioteca.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biblioteca.models.acceso.Empleado;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
}