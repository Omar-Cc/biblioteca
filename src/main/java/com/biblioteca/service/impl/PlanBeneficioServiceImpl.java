package com.biblioteca.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.comercial.PlanBeneficioRequestDTO;
import com.biblioteca.dto.comercial.PlanBeneficioResponseDTO;
import com.biblioteca.exceptions.RecursoNoEncontradoException;
import com.biblioteca.mapper.comercial.PlanBeneficioMapper;
import com.biblioteca.models.comercial.Beneficio;
import com.biblioteca.models.comercial.Plan;
import com.biblioteca.models.comercial.PlanBeneficio;
import com.biblioteca.repositories.comercial.PlanBeneficioRepository;
import com.biblioteca.service.BeneficioService;
import com.biblioteca.service.PlanBeneficioService;
import com.biblioteca.service.PlanService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanBeneficioServiceImpl implements PlanBeneficioService {

  private final PlanBeneficioRepository planBeneficioRepository;
  private final PlanBeneficioMapper planBeneficioMapper;
  private final PlanService planService;
  private final BeneficioService beneficioService;

  @Override
  @Transactional
  public PlanBeneficioResponseDTO asociarBeneficioAPlan(PlanBeneficioRequestDTO planBeneficioDTO) {
    Plan plan = planService.obtenerEntidadPlanPorId(planBeneficioDTO.getPlanId())
        .orElseThrow(
            () -> new RecursoNoEncontradoException("Plan no encontrado con ID: " + planBeneficioDTO.getPlanId()));

    Beneficio beneficio = beneficioService.obtenerEntidadBeneficioPorId(planBeneficioDTO.getBeneficioId())
        .orElseThrow(
            () -> new RecursoNoEncontradoException(
                "Beneficio no encontrado con ID: " + planBeneficioDTO.getBeneficioId()));

    // Verificar si la asociación ya existe usando el método del repositorio
    if (planBeneficioRepository.existsByPlan_IdAndBeneficio_Id(plan.getId(), beneficio.getId())) {
      throw new IllegalArgumentException(
          "La asociación entre el plan ID " + plan.getId() + " y el beneficio ID " + beneficio.getId() + " ya existe.");
    }

    PlanBeneficio planBeneficio = planBeneficioMapper.toEntity(planBeneficioDTO);
    planBeneficio.setPlan(plan);
    planBeneficio.setBeneficio(beneficio);
    // El ID de PlanBeneficio será generado por la BD

    // Mantener la relación bidireccional en la entidad Plan
    plan.addPlanBeneficio(planBeneficio);
    // No es necesario guardar 'plan' explícitamente aquí si la relación es
    // gestionada por JPA
    // y 'PlanBeneficio' es la entidad dueña o hay cascada.
    // El guardado de 'planBeneficio' persistirá la relación.

    PlanBeneficio planBeneficioGuardado = planBeneficioRepository.save(planBeneficio);
    return planBeneficioMapper.toResponseDTO(planBeneficioGuardado);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PlanBeneficioResponseDTO> obtenerAsociacionPorId(Long id) {
    return planBeneficioRepository.findById(id)
        .map(planBeneficioMapper::toResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PlanBeneficioResponseDTO> obtenerAsociacionPorPlanIdYBeneficioId(Long planId, Long beneficioId) {
    return planBeneficioRepository.findByPlan_IdAndBeneficio_Id(planId, beneficioId)
        .map(planBeneficioMapper::toResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PlanBeneficioResponseDTO> obtenerBeneficiosPorPlanId(Long planId) {
    return planBeneficioRepository.findByPlan_Id(planId).stream()
        .map(planBeneficioMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<PlanBeneficioResponseDTO> obtenerPlanesPorBeneficioId(Long beneficioId) {
    return planBeneficioRepository.findByBeneficio_Id(beneficioId).stream()
        .map(planBeneficioMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Optional<PlanBeneficioResponseDTO> actualizarAsociacion(Long id, PlanBeneficioRequestDTO planBeneficioDTO) {
    return planBeneficioRepository.findById(id) // Actualizar usando el ID de PlanBeneficio
        .map(planBeneficioExistente -> {
          // No se permite cambiar Plan o Beneficio, solo 'valor' y 'activo'
          planBeneficioExistente.setValor(planBeneficioDTO.getValor());
          if (planBeneficioDTO.getActivo() != null) {
            planBeneficioExistente.setActivo(planBeneficioDTO.getActivo());
          }
          // Los IDs de plan y beneficio en el DTO se ignoran para la actualización de una
          // asociación existente.
          // Si se quisiera cambiar el plan o beneficio, se debería eliminar la asociación
          // y crear una nueva.
          PlanBeneficio actualizado = planBeneficioRepository.save(planBeneficioExistente);
          return planBeneficioMapper.toResponseDTO(actualizado);
        });
  }

  @Override
  @Transactional
  public boolean eliminarAsociacion(Long id) { // Eliminar por el ID de PlanBeneficio
    Optional<PlanBeneficio> pbOpt = planBeneficioRepository.findById(id);
    if (pbOpt.isPresent()) {
      PlanBeneficio pb = pbOpt.get();
      // Mantener la coherencia de la relación bidireccional en Plan
      if (pb.getPlan() != null) {
        pb.getPlan().removePlanBeneficio(pb);
        // planService.actualizarPlan(pb.getPlan()); // Si es necesario guardar el Plan
        // explícitamente
      }
      planBeneficioRepository.deleteById(id);
      return true;
    }
    return false;
  }

  @Override
  @Transactional
  public boolean eliminarAsociacionPorPlanIdYBeneficioId(Long planId, Long beneficioId) {
    Optional<PlanBeneficio> pbOpt = planBeneficioRepository.findByPlan_IdAndBeneficio_Id(planId, beneficioId);
    if (pbOpt.isPresent()) {
      PlanBeneficio pb = pbOpt.get();
      if (pb.getPlan() != null) {
        pb.getPlan().removePlanBeneficio(pb);
      }
      planBeneficioRepository.delete(pb); // Eliminar la entidad encontrada
      return true;
    }
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PlanBeneficio> obtenerEntidadAsociacionPorId(Long id) {
    return planBeneficioRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PlanBeneficio> obtenerEntidadAsociacionPorPlanIdYBeneficioId(Long planId, Long beneficioId) {
    return planBeneficioRepository.findByPlan_IdAndBeneficio_Id(planId, beneficioId);
  }

  @Override
  @Transactional
  public boolean activarAsociacion(Long id) { // Operar sobre el ID de PlanBeneficio
    return planBeneficioRepository.findById(id)
        .map(planBeneficio -> {
          planBeneficio.setActivo(true);
          planBeneficioRepository.save(planBeneficio);
          return true;
        })
        .orElse(false);
  }

  @Override
  @Transactional
  public boolean desactivarAsociacion(Long id) { // Operar sobre el ID de PlanBeneficio
    return planBeneficioRepository.findById(id)
        .map(planBeneficio -> {
          planBeneficio.setActivo(false);
          planBeneficioRepository.save(planBeneficio);
          return true;
        })
        .orElse(false);
  }

  @Override
  @Transactional
  public List<PlanBeneficioResponseDTO> asociarVariosBeneficiosAPlan(Long planId,
      List<PlanBeneficioRequestDTO> beneficiosDTO) {
    Plan plan = planService.obtenerEntidadPlanPorId(planId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Plan no encontrado con ID: " + planId));

    List<PlanBeneficioResponseDTO> resultados = new ArrayList<>();
    for (PlanBeneficioRequestDTO dto : beneficiosDTO) {
      dto.setPlanId(planId); // Asegurar que el DTO use el planId correcto
      try {
        // Verificar si el beneficio existe
        beneficioService.obtenerEntidadBeneficioPorId(dto.getBeneficioId())
            .orElseThrow(
                () -> new RecursoNoEncontradoException("Beneficio no encontrado con ID: " + dto.getBeneficioId()));

        // Verificar si la asociación ya existe
        if (planBeneficioRepository.existsByPlan_IdAndBeneficio_Id(planId, dto.getBeneficioId())) {
          System.err.println("La asociación para el plan ID " + planId + " y beneficio ID " + dto.getBeneficioId()
              + " ya existe. Omitiendo.");
          // Opcionalmente, podrías cargarla y devolverla si ese es el comportamiento
          // deseado
          // obtenerAsociacionPorPlanIdYBeneficioId(planId,
          // dto.getBeneficioId()).ifPresent(resultados::add);
          continue;
        }

        PlanBeneficio planBeneficio = planBeneficioMapper.toEntity(dto);
        planBeneficio.setPlan(plan);
        planBeneficio.setBeneficio(beneficioService.obtenerEntidadBeneficioPorId(dto.getBeneficioId()).get()); // Ya
                                                                                                               // verificado

        plan.addPlanBeneficio(planBeneficio); // Mantener relación bidireccional

        PlanBeneficio guardado = planBeneficioRepository.save(planBeneficio);
        resultados.add(planBeneficioMapper.toResponseDTO(guardado));

      } catch (IllegalArgumentException | RecursoNoEncontradoException e) {
        System.err.println(
            "Error al asociar beneficio ID " + dto.getBeneficioId() + " al plan ID " + planId + ": " + e.getMessage());
      }
    }
    // planService.actualizarPlan(plan); // Si es necesario guardar el Plan
    // explícitamente después de añadir todas las asociaciones
    return resultados;
  }
}