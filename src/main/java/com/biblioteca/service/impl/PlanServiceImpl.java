package com.biblioteca.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.biblioteca.dto.comercial.PlanRequestDTO;
import com.biblioteca.dto.comercial.PlanResponseDTO;
import com.biblioteca.mapper.comercial.PlanMapper;
import com.biblioteca.models.comercial.Plan;
import com.biblioteca.service.PlanService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

  private final List<Plan> planes = new ArrayList<>();
  private final AtomicLong planIdCounter = new AtomicLong(0);
  private final PlanMapper planMapper;
  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;

  @PostConstruct
  public void initPlanesData() {
    try {
      InputStream inputStream = resourceLoader.getResource("classpath:data/planes-data.json").getInputStream();
      List<PlanRequestDTO> planesDTOs = objectMapper.readValue(inputStream,
          new TypeReference<List<PlanRequestDTO>>() {
          });
      planesDTOs.forEach(this::crearPlan);
      System.out.println("Datos iniciales de Planes cargados desde JSON: " + planes.size() + " planes.");
    } catch (Exception e) {
      System.err.println("Error al cargar datos iniciales de planes desde JSON: " + e.getMessage());
      e.printStackTrace();

    }
  }

  @Override
  public PlanResponseDTO crearPlan(PlanRequestDTO planDTO) {
    Plan plan = planMapper.toEntity(planDTO);
    plan.setId(planIdCounter.incrementAndGet());
    planes.add(plan);
    return planMapper.toResponseDTO(plan);
  }

  @Override
  public Optional<PlanResponseDTO> obtenerPlanPorId(Long id) {
    return planes.stream()
        .filter(p -> p.getId().equals(id))
        .findFirst()
        .map(planMapper::toResponseDTO);
  }

  @Override
  public List<PlanResponseDTO> obtenerTodosLosPlanes() {
    return planes.stream()
        .map(planMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<PlanResponseDTO> obtenerPlanesActivos() {
    return planes.stream()
        .filter(Plan::isActivo)
        .map(planMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<PlanResponseDTO> actualizarPlan(Long id, PlanRequestDTO planDTO) {
    return planes.stream()
        .filter(p -> p.getId().equals(id))
        .findFirst()
        .map(plan -> {
          planMapper.updateEntityFromDTO(planDTO, plan);
          return planMapper.toResponseDTO(plan);
        });
  }

  @Override
  public boolean eliminarPlan(Long id) {
    return planes.removeIf(p -> p.getId().equals(id));
  }

  @Override
  public Optional<Plan> obtenerEntidadPlanPorId(Long id) {
    return planes.stream()
        .filter(p -> p.getId().equals(id))
        .findFirst();
  }

  @Override
  public PlanResponseDTO aplicarDescuento(Long id, double porcentajeDescuento) {
    if (porcentajeDescuento < 0 || porcentajeDescuento > 100) {
      throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100");
    }

    Plan plan = obtenerEntidadPlanPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado con ID: " + id));

    double factor = 1 - (porcentajeDescuento / 100.0);

    // Aplicar descuento a los precios (redondeo a entero)
    plan.setPrecioMensual((int) Math.round(plan.getPrecioMensual() * factor));
    plan.setPrecioAnual((int) Math.round(plan.getPrecioAnual() * factor));

    return planMapper.toResponseDTO(plan);
  }

  @Override
  public boolean activarPlan(Long id) {
    return obtenerEntidadPlanPorId(id)
        .map(plan -> {
          plan.setActivo(true);
          return true;
        })
        .orElse(false);
  }

  @Override
  public boolean desactivarPlan(Long id) {
    return obtenerEntidadPlanPorId(id)
        .map(plan -> {
          plan.setActivo(false);
          return true;
        })
        .orElse(false);
  }
}