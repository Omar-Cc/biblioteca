package com.biblioteca.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.comercial.PlanRequestDTO;
import com.biblioteca.dto.comercial.PlanResponseDTO;
import com.biblioteca.exceptions.RecursoNoEncontradoException;
import com.biblioteca.mapper.comercial.PlanMapper;
import com.biblioteca.models.comercial.Plan;
import com.biblioteca.repositories.comercial.PlanRepository;
import com.biblioteca.service.PlanService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

  private final PlanRepository planRepository;
  private final PlanMapper planMapper;

  @Override
  @Transactional
  public PlanResponseDTO crearPlan(PlanRequestDTO planDTO) {
    Plan plan = planMapper.toEntity(planDTO);
    // Las colecciones planBeneficios y suscripciones se inicializan vacías por
    // defecto en la entidad Plan.
    // Si el DTO incluyera información para estas colecciones, se manejaría aquí.
    Plan planGuardado = planRepository.save(plan);
    return planMapper.toResponseDTO(planGuardado);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PlanResponseDTO> obtenerPlanPorId(Long id) {
    return planRepository.findById(id)
        .map(planMapper::toResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PlanResponseDTO> obtenerTodosLosPlanes() {
    return planRepository.findAll().stream()
        .map(planMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<PlanResponseDTO> obtenerPlanesActivos() {
    return planRepository.findByActivoTrue().stream()
        .map(planMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Optional<PlanResponseDTO> actualizarPlan(Long id, PlanRequestDTO planDTO) {
    return planRepository.findById(id)
        .map(plan -> {
          planMapper.updateEntityFromDTO(planDTO, plan); // El mapper actualiza la entidad existente
          // La gestión de planBeneficios y suscripciones se haría a través de sus
          // respectivos servicios
          // o si el DTO de Plan incluyera IDs para asociar/desasociar.
          // Por ahora, este método solo actualiza los campos directos de Plan.
          Plan planActualizado = planRepository.save(plan);
          return planMapper.toResponseDTO(planActualizado);
        });
  }

  @Override
  @Transactional
  public boolean eliminarPlan(Long id) {
    if (planRepository.existsById(id)) {
      // Considerar el impacto en PlanBeneficio y Suscripcion.
      // Con CascadeType.ALL y orphanRemoval=true en Plan, las entidades asociadas se
      // eliminarán.
      // Si no, se podría necesitar lógica adicional o la BD podría bloquear la
      // eliminación
      // si hay restricciones de clave externa.
      planRepository.deleteById(id);
      return true;
    }
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Plan> obtenerEntidadPlanPorId(Long id) {
    return planRepository.findById(id);
  }

  @Override
  @Transactional
  public PlanResponseDTO aplicarDescuento(Long id, double porcentajeDescuento) {
    if (porcentajeDescuento < 0 || porcentajeDescuento > 100) {
      throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100");
    }

    Plan plan = planRepository.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Plan no encontrado con ID: " + id));

    double factor = 1 - (porcentajeDescuento / 100.0);

    // Aplicar descuento a los precios (redondeo a entero)
    if (plan.getPrecioMensual() != null) {
      plan.setPrecioMensual((int) Math.round(plan.getPrecioMensual() * factor));
    }
    if (plan.getPrecioAnual() != null) {
      plan.setPrecioAnual((int) Math.round(plan.getPrecioAnual() * factor));
    }

    Plan planActualizado = planRepository.save(plan);
    return planMapper.toResponseDTO(planActualizado);
  }

  @Override
  @Transactional
  public boolean activarPlan(Long id) {
    return planRepository.findById(id)
        .map(plan -> {
          plan.setActivo(true);
          planRepository.save(plan);
          return true;
        })
        .orElse(false);
  }

  @Override
  @Transactional
  public boolean desactivarPlan(Long id) {
    return planRepository.findById(id)
        .map(plan -> {
          plan.setActivo(false);
          planRepository.save(plan);
          return true;
        })
        .orElse(false);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Long> obtenerPlanIdPorNombre(String nombre) {
    return planRepository.findByNombre(nombre)
        .map(Plan::getId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PlanResponseDTO> obtenerPlanPorNombre(String nombre) {
    return planRepository.findByNombre(nombre)
        .map(planMapper::toResponseDTO);
  }
}