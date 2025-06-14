package com.biblioteca.service;

import org.springframework.stereotype.Service;
import com.biblioteca.dto.comercial.OrdenRequestDTO;
import com.biblioteca.exceptions.OperacionNoPermitidaException;
import com.biblioteca.models.acceso.Perfil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio dedicado a las validaciones de negocio específicas para órdenes
 * Mantiene separada la lógica de validación de la lógica de procesamiento
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrdenValidationService {
    
    private final PerfilService perfilService;
    private final OrdenService ordenService;
    
    // Límites configurables
    private static final int MAX_ORDENES_PENDIENTES = 5;
    private static final double MAX_MONTO_DIARIO = 2000.0; // En soles
    
    /**
     * Validación completa antes de crear una orden
     */
    public void validarCreacionOrden(Long perfilId, OrdenRequestDTO ordenDTO) {
        log.debug("Validando creación de orden para perfil: {}", perfilId);
        
        // Validar perfil
        validarPerfil(perfilId);
        
        // Validar límites de usuario
        validarLimitesUsuario(perfilId);
        
        // Validar datos de la orden
        validarDatosOrden(ordenDTO);
        
        log.debug("Validación de orden completada exitosamente");
    }
    
    /**
     * Validar que el perfil existe y está activo
     */
    private void validarPerfil(Long perfilId) {
        Perfil perfil = perfilService.obtenerEntidadPerfilPorId(perfilId)
            .orElseThrow(() -> new OperacionNoPermitidaException(
                "Perfil no encontrado: " + perfilId));
                
        if (!perfil.getActivo()) {
            throw new OperacionNoPermitidaException(
                "El perfil no está activo: " + perfilId);
        }
    }
    
    /**
     * Validar límites de negocio por usuario
     */
    private void validarLimitesUsuario(Long perfilId) {
        // Verificar límites de órdenes pendientes
        long ordenesPendientes = ordenService.obtenerOrdenesPorPerfil(perfilId)
            .stream()
            .filter(orden -> "Pendiente".equals(orden.getEstadoOrden()))
            .count();
            
        if (ordenesPendientes >= MAX_ORDENES_PENDIENTES) {
            throw new OperacionNoPermitidaException(
                "Has alcanzado el límite máximo de órdenes pendientes: " + MAX_ORDENES_PENDIENTES);
        }
        
        // Aquí se pueden agregar más validaciones:
        // - Límites de compra diaria/mensual
        // - Verificación de método de pago válido
        // - Restricciones por tipo de contenido
        // - Validaciones de ubicación geográfica
    }
    
    /**
     * Validar datos básicos de la orden
     */
    private void validarDatosOrden(OrdenRequestDTO ordenDTO) {
        if (ordenDTO == null) {
            throw new OperacionNoPermitidaException("Los datos de la orden son requeridos");
        }
        
        // Validaciones específicas según necesidades del negocio
        // Por ejemplo: validar dirección, método de pago, etc.
    }
    
    /**
     * Validar si se puede modificar una orden
     */
    public void validarModificacionOrden(String estadoActual, String operacion) {
        switch (estadoActual) {
            case "Pendiente":
                // Se puede hacer cualquier operación
                break;
            case "Procesando":
                if (!"cancelar".equals(operacion)) {
                    throw new OperacionNoPermitidaException(
                        "Solo se puede cancelar una orden en procesamiento");
                }
                break;
            case "Completada":
            case "Cancelada":
                throw new OperacionNoPermitidaException(
                    "No se puede modificar una orden en estado: " + estadoActual);
            default:
                throw new OperacionNoPermitidaException(
                    "Estado de orden no reconocido: " + estadoActual);
        }
    }
}
