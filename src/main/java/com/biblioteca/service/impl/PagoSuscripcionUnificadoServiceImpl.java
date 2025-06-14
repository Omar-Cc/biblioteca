package com.biblioteca.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.comercial.HistorialPagoSuscripcionRequestDTO;
import com.biblioteca.dto.comercial.HistorialPagoSuscripcionResponseDTO;
import com.biblioteca.dto.comercial.PagoRequestDTO;
import com.biblioteca.dto.comercial.PagoResponseDTO;
import com.biblioteca.service.HistorialPagoSuscripcionService;
import com.biblioteca.service.PagoService;
import com.biblioteca.service.PagoSuscripcionUnificadoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoSuscripcionUnificadoServiceImpl implements PagoSuscripcionUnificadoService {

    private final PagoService pagoService;
    private final HistorialPagoSuscripcionService historialPagoService;

    @Override
    @Transactional
    public HistorialPagoSuscripcionResponseDTO procesarPagoSuscripcionCompleto(
            Long suscripcionId, Long metodoPagoId, Double monto, String periodo) {
        
        try {
            log.info("Procesando pago completo para suscripción {} - Monto: {}", suscripcionId, monto);
            
            // 1. Crear pago en tabla principal
            PagoRequestDTO pagoRequest = PagoRequestDTO.builder()
                .suscripcionId(suscripcionId)
                .metodoPagoId(metodoPagoId)
                .monto((int)(monto * 100)) // Convertir a centavos
                .periodo(periodo)
                .esSimulado(true)
                .simularFallo(false)
                .build();
            
            PagoResponseDTO pago = pagoService.registrarPago(pagoRequest);
            PagoResponseDTO pagoProcessed = pagoService.procesarPago(pago.getId());
            
            // 2. Crear entrada en historial de suscripción
            HistorialPagoSuscripcionRequestDTO historialRequest = HistorialPagoSuscripcionRequestDTO.builder()
                .suscripcionId(suscripcionId)
                .pagoId(pagoProcessed.getId()) // Vincular con el pago creado
                .monto((int)(monto * 100))
                .periodo(periodo)
                .estado("PAGADO")
                .build();
            
            HistorialPagoSuscripcionResponseDTO historial = historialPagoService.registrarPago(historialRequest);
            
            log.info("Pago completo procesado exitosamente. Historial ID: {}, Pago ID: {}", 
                historial.getId(), pagoProcessed.getId());
            
            return historial;
            
        } catch (Exception e) {
            log.error("Error procesando pago completo para suscripción {}: {}", suscripcionId, e.getMessage());
            throw new RuntimeException("Error procesando pago de suscripción", e);
        }
    }

    @Override
    @Transactional
    public HistorialPagoSuscripcionResponseDTO simularRenovacionAutomaticaCompleta(Long suscripcionId) {
        // Este método simula una renovación automática completa
        return procesarPagoSuscripcionCompleto(suscripcionId, 1L, 29.99, "Renovación Automática");
    }

    @Override
    @Transactional
    public void vincularPagoExistenteConHistorial(Long pagoId, Long historialId) {
        historialPagoService.vincularConPagoGeneral(historialId, pagoId);
    }
}
