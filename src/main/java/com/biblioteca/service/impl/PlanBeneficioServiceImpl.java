package com.biblioteca.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.biblioteca.dto.comercial.PlanBeneficioRequestDTO;
import com.biblioteca.dto.comercial.PlanBeneficioResponseDTO;
import com.biblioteca.mapper.comercial.PlanBeneficioMapper;
import com.biblioteca.models.comercial.Beneficio;
import com.biblioteca.models.comercial.Plan;
import com.biblioteca.models.comercial.PlanBeneficio;
import com.biblioteca.service.BeneficioService;
import com.biblioteca.service.PlanBeneficioService;
import com.biblioteca.service.PlanService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanBeneficioServiceImpl implements PlanBeneficioService {

  private final Map<String, PlanBeneficio> planBeneficios = new ConcurrentHashMap<>();
  private final PlanBeneficioMapper planBeneficioMapper;
  private final PlanService planService;
  private final BeneficioService beneficioService;
  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;

  // Método para generar clave compuesta
  private String generarClave(Long planId, Long beneficioId) {
    return planId + ":" + beneficioId;
  }

  @PostConstruct
  public void initPlanBeneficiosData() {
    try {
      InputStream inputStream = resourceLoader.getResource("classpath:data/plan-beneficios-data.json").getInputStream();
      List<PlanBeneficioRequestDTO> planBeneficiosDTOs = objectMapper.readValue(inputStream,
          new TypeReference<List<PlanBeneficioRequestDTO>>() {
          });
      planBeneficiosDTOs.forEach(this::asociarBeneficioAPlan);
      System.out.println(
          "Datos iniciales de Plan-Beneficios cargados desde JSON: " + planBeneficios.size() + " asociaciones.");
    } catch (Exception e) {
      System.err.println("Error al cargar datos iniciales de plan-beneficios desde JSON: " + e.getMessage());
      // No iniciamos con datos por defecto ya que dependemos de planes y beneficios
      // existentes
    }
  }

  @Override
  public PlanBeneficioResponseDTO asociarBeneficioAPlan(PlanBeneficioRequestDTO planBeneficioDTO) {
    // Obtener Plan y Beneficio
    Plan plan = planService.obtenerEntidadPlanPorId(planBeneficioDTO.getPlanId())
        .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado con ID: " + planBeneficioDTO.getPlanId()));

    Beneficio beneficio = beneficioService.obtenerEntidadBeneficioPorId(planBeneficioDTO.getBeneficioId())
        .orElseThrow(
            () -> new IllegalArgumentException("Beneficio no encontrado con ID: " + planBeneficioDTO.getBeneficioId()));

    PlanBeneficio planBeneficio = planBeneficioMapper.toEntity(planBeneficioDTO);
    planBeneficio.setPlan(plan);
    planBeneficio.setBeneficio(beneficio);

    // Mantener la relación bidireccional
    plan.addPlanBeneficio(planBeneficio);

    String clave = generarClave(plan.getId(), beneficio.getId());
    planBeneficios.put(clave, planBeneficio);

    return planBeneficioMapper.toResponseDTO(planBeneficio);
  }

  @Override
  public Optional<PlanBeneficioResponseDTO> obtenerAsociacionPorIds(Long planId, Long beneficioId) {
    return Optional.ofNullable(planBeneficios.get(generarClave(planId, beneficioId)))
        .map(planBeneficioMapper::toResponseDTO);
  }

  @Override
  public List<PlanBeneficioResponseDTO> obtenerBeneficiosPorPlan(Long planId) {
    return planBeneficios.values().stream()
        .filter(pb -> pb.getPlan().getId().equals(planId))
        .map(planBeneficioMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<PlanBeneficioResponseDTO> obtenerPlanesPorBeneficio(Long beneficioId) {
    return planBeneficios.values().stream()
        .filter(pb -> pb.getBeneficio().getId().equals(beneficioId))
        .map(planBeneficioMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<PlanBeneficioResponseDTO> actualizarAsociacion(Long planId, Long beneficioId,
      PlanBeneficioRequestDTO planBeneficioDTO) {
    String clave = generarClave(planId, beneficioId);
    return Optional.ofNullable(planBeneficios.get(clave))
        .map(planBeneficio -> {
          planBeneficio.setValor(planBeneficioDTO.getValor());
          planBeneficio.setActivo(planBeneficioDTO.isActivo());
          return planBeneficioMapper.toResponseDTO(planBeneficio);
        });
  }

  @Override
  public boolean eliminarAsociacion(Long planId, Long beneficioId) {
    String clave = generarClave(planId, beneficioId);
    PlanBeneficio planBeneficio = planBeneficios.get(clave);

    if (planBeneficio != null) {
      // Eliminar la relación bidireccional
      planBeneficio.getPlan().removePlanBeneficio(planBeneficio);
      planBeneficios.remove(clave);
      return true;
    }
    return false;
  }

  @Override
  public Optional<PlanBeneficio> obtenerEntidadAsociacionPorIds(Long planId, Long beneficioId) {
    return Optional.ofNullable(planBeneficios.get(generarClave(planId, beneficioId)));
  }

  @Override
  public boolean activarAsociacion(Long planId, Long beneficioId) {
    return obtenerEntidadAsociacionPorIds(planId, beneficioId)
        .map(planBeneficio -> {
          planBeneficio.setActivo(true);
          return true;
        })
        .orElse(false);
  }

  @Override
  public boolean desactivarAsociacion(Long planId, Long beneficioId) {
    return obtenerEntidadAsociacionPorIds(planId, beneficioId)
        .map(planBeneficio -> {
          planBeneficio.setActivo(false);
          return true;
        })
        .orElse(false);
  }

  @Override
  public List<PlanBeneficioResponseDTO> asociarVariosBeneficiosAPlan(Long planId,
      List<PlanBeneficioRequestDTO> beneficiosDTO) {
    List<PlanBeneficioResponseDTO> resultados = new ArrayList<>();

    // Verificar que el plan existe
    planService.obtenerEntidadPlanPorId(planId)
        .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado con ID: " + planId));

    // Asociar cada beneficio
    for (PlanBeneficioRequestDTO dto : beneficiosDTO) {
      dto.setPlanId(planId);
      try {
        PlanBeneficioResponseDTO resultado = asociarBeneficioAPlan(dto);
        resultados.add(resultado);
      } catch (Exception e) {
        // Registrar error y continuar con el siguiente
        System.err.println(
            "Error al asociar beneficio " + dto.getBeneficioId() + " al plan " + planId + ": " + e.getMessage());
      }
    }

    return resultados;
  }
}