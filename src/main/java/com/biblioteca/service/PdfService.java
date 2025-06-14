package com.biblioteca.service;

import java.util.Map;

public interface PdfService {

  /**
   * Genera PDF desde plantilla HTML
   */
  byte[] generarPdfDesdeTemplate(String template, Map<String, Object> variables);

  /**
   * Genera PDF de factura de orden
   */
  byte[] generarFacturaOrden(Long facturaId);

  /**
   * Genera PDF de factura de suscripción
   */
  byte[] generarFacturaSuscripcion(Long suscripcionId, String periodo);

  /**
   * Genera recibo de pago
   */
  byte[] generarReciboPago(Long pagoId);

  /**
   * Genera reporte de historial de pagos
   */
  byte[] generarHistorialPagos(Long usuarioId, String periodo);
}