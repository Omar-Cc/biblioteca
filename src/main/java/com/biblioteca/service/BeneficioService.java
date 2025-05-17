package com.biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.biblioteca.dto.comercial.BeneficioRequestDTO;
import com.biblioteca.dto.comercial.BeneficioResponseDTO;
import com.biblioteca.models.comercial.Beneficio;

public interface BeneficioService {
  BeneficioResponseDTO crearBeneficio(BeneficioRequestDTO beneficioDTO);

  Optional<BeneficioResponseDTO> obtenerBeneficioPorId(Long id);

  List<BeneficioResponseDTO> obtenerTodosLosBeneficios();

  Optional<BeneficioResponseDTO> actualizarBeneficio(Long id, BeneficioRequestDTO beneficioDTO);

  boolean eliminarBeneficio(Long id);

  // Para uso interno principalmente
  Optional<Beneficio> obtenerEntidadBeneficioPorId(Long id);

  // Obtener beneficios por tipo
  List<BeneficioResponseDTO> obtenerBeneficiosPorTipo(String tipo);

  // Obtener beneficios activos
  List<BeneficioResponseDTO> obtenerBeneficiosActivos();
}