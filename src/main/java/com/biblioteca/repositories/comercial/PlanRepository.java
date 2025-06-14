package com.biblioteca.repositories.comercial;

import com.biblioteca.models.comercial.Plan;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByActivoTrue();

    Optional<Plan> findByNombre(String nombre);
}