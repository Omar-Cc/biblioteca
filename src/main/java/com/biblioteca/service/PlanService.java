package com.biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.biblioteca.dto.comercial.PlanRequestDTO;
import com.biblioteca.dto.comercial.PlanResponseDTO;
import com.biblioteca.models.comercial.Plan;

public interface PlanService {
    // Operaciones CRUD básicas
    PlanResponseDTO crearPlan(PlanRequestDTO planDTO);
    
    Optional<PlanResponseDTO> obtenerPlanPorId(Long id);
    
    List<PlanResponseDTO> obtenerTodosLosPlanes();
    
    List<PlanResponseDTO> obtenerPlanesActivos();
    
    Optional<PlanResponseDTO> actualizarPlan(Long id, PlanRequestDTO planDTO);
    
    boolean eliminarPlan(Long id);
    
    // Para uso interno principalmente
    Optional<Plan> obtenerEntidadPlanPorId(Long id);
    
    // Operaciones específicas
    PlanResponseDTO aplicarDescuento(Long id, double porcentajeDescuento);
    
    boolean activarPlan(Long id);
    
    boolean desactivarPlan(Long id);
}