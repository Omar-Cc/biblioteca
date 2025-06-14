package com.biblioteca.repositories.comercial;

import com.biblioteca.models.comercial.PlanBeneficio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanBeneficioRepository extends JpaRepository<PlanBeneficio, Long> { // ID es Long

  // Buscar una asociación específica por los IDs de Plan y Beneficio
  Optional<PlanBeneficio> findByPlan_IdAndBeneficio_Id(Long planId, Long beneficioId);

  // Encontrar todas las asociaciones para un Plan
  List<PlanBeneficio> findByPlan_Id(Long planId);

  // Encontrar todas las asociaciones para un Beneficio
  List<PlanBeneficio> findByBeneficio_Id(Long beneficioId);

  // Para activar/desactivar
  List<PlanBeneficio> findByPlan_IdAndActivo(Long planId, boolean activo);

  List<PlanBeneficio> findByBeneficio_IdAndActivo(Long beneficioId, boolean activo);

  // Verificar si ya existe una asociación entre un plan y un beneficio
  // específicos
  boolean existsByPlan_IdAndBeneficio_Id(Long planId, Long beneficioId);

}