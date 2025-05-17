package com.biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.biblioteca.dto.comercial.PlanBeneficioRequestDTO;
import com.biblioteca.dto.comercial.PlanBeneficioResponseDTO;
import com.biblioteca.models.comercial.PlanBeneficio;

public interface PlanBeneficioService {
	// Operaciones CRUD básicas
	PlanBeneficioResponseDTO asociarBeneficioAPlan(PlanBeneficioRequestDTO planBeneficioDTO);

	Optional<PlanBeneficioResponseDTO> obtenerAsociacionPorIds(Long planId, Long beneficioId);

	List<PlanBeneficioResponseDTO> obtenerBeneficiosPorPlan(Long planId);

	List<PlanBeneficioResponseDTO> obtenerPlanesPorBeneficio(Long beneficioId);

	Optional<PlanBeneficioResponseDTO> actualizarAsociacion(Long planId, Long beneficioId,
			PlanBeneficioRequestDTO planBeneficioDTO);

	boolean eliminarAsociacion(Long planId, Long beneficioId);

	// Para uso interno principalmente
	Optional<PlanBeneficio> obtenerEntidadAsociacionPorIds(Long planId, Long beneficioId);

	// Operaciones específicas
	boolean activarAsociacion(Long planId, Long beneficioId);

	boolean desactivarAsociacion(Long planId, Long beneficioId);

	// Operaciones en lote
	List<PlanBeneficioResponseDTO> asociarVariosBeneficiosAPlan(Long planId,
			List<PlanBeneficioRequestDTO> beneficiosDTO);
}