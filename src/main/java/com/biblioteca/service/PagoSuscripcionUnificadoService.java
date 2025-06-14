package com.biblioteca.service;

import com.biblioteca.dto.comercial.HistorialPagoSuscripcionResponseDTO;

public interface PagoSuscripcionUnificadoService {
    
    /**
     * Procesa un pago completo de suscripción creando tanto el registro en Pago
     * como en HistorialPagoSuscripcion vinculados entre sí
     */
    HistorialPagoSuscripcionResponseDTO procesarPagoSuscripcionCompleto(
        Long suscripcionId, Long metodoPagoId, Double monto, String periodo);
    
    /**
     * Simula una renovación automática completa
     */
    HistorialPagoSuscripcionResponseDTO simularRenovacionAutomaticaCompleta(Long suscripcionId);
    
    /**
     * Vincula un pago existente con un historial de suscripción
     */
    void vincularPagoExistenteConHistorial(Long pagoId, Long historialId);
}
