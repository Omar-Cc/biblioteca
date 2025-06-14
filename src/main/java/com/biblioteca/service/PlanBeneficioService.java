package com.biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.biblioteca.dto.comercial.PlanBeneficioRequestDTO;
import com.biblioteca.dto.comercial.PlanBeneficioResponseDTO;
import com.biblioteca.models.comercial.PlanBeneficio;

public interface PlanBeneficioService {
	// Operaciones CRUD básicas
	PlanBeneficioResponseDTO asociarBeneficioAPlan(PlanBeneficioRequestDTO planBeneficioDTO);

	// Obtener por el ID propio de PlanBeneficio
	Optional<PlanBeneficioResponseDTO> obtenerAsociacionPorId(Long id);

	// Obtener por la combinación de Plan ID y Beneficio ID
	Optional<PlanBeneficioResponseDTO> obtenerAsociacionPorPlanIdYBeneficioId(Long planId, Long beneficioId);

	List<PlanBeneficioResponseDTO> obtenerBeneficiosPorPlanId(Long planId);

	List<PlanBeneficioResponseDTO> obtenerPlanesPorBeneficioId(Long beneficioId);

	// Actualizar usando el ID propio de PlanBeneficio
	Optional<PlanBeneficioResponseDTO> actualizarAsociacion(Long id, PlanBeneficioRequestDTO planBeneficioDTO);

	// Eliminar usando el ID propio de PlanBeneficio
	boolean eliminarAsociacion(Long id);

	// Eliminar usando la combinación de Plan ID y Beneficio ID
	boolean eliminarAsociacionPorPlanIdYBeneficioId(Long planId, Long beneficioId);

	// Para uso interno principalmente
	Optional<PlanBeneficio> obtenerEntidadAsociacionPorId(Long id);

	Optional<PlanBeneficio> obtenerEntidadAsociacionPorPlanIdYBeneficioId(Long planId, Long beneficioId);

	// Operaciones específicas
	// Estas operaciones pueden usar el ID de PlanBeneficio o la combinación planId,
	// beneficioId
	boolean activarAsociacion(Long id); // O activarAsociacion(Long planId, Long beneficioId)

	boolean desactivarAsociacion(Long id); // O desactivarAsociacion(Long planId, Long beneficioId)

	// Operaciones en lote
	List<PlanBeneficioResponseDTO> asociarVariosBeneficiosAPlan(Long planId,
			List<PlanBeneficioRequestDTO> beneficiosDTO);
}