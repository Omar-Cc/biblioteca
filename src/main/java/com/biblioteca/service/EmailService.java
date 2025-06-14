package com.biblioteca.service;

import java.util.Map;

public interface EmailService {

  /**
   * Envía un email simple
   */
  boolean enviarEmail(String destinatario, String asunto, String contenido);

  /**
   * Envía un email con contenido HTML
   */
  boolean enviarEmailHtml(String destinatario, String asunto, String contenidoHtml);

  /**
   * Envía un email con plantilla
   */
  boolean enviarEmailConPlantilla(String destinatario, String asunto, String plantilla, Map<String, Object> variables);

  /**
   * Envía email a múltiples destinatarios
   */
  boolean enviarEmailMasivo(String[] destinatarios, String asunto, String contenido);

  /**
   * Envía email con archivos adjuntos
   */
  boolean enviarEmailConAdjuntos(String destinatario, String asunto, String contenido, Map<String, byte[]> adjuntos);

  /**
   * Envía email con plantilla HTML y archivos adjuntos
   */
  boolean enviarEmailConPlantillaYAdjuntos(String destinatario, String asunto, String plantilla,
      Map<String, Object> variables, Map<String, byte[]> adjuntos);

  /**
   * Verifica si el servicio de email está disponible
   */
  boolean verificarServicioDisponible();

  /**
   * Valida formato de email
   */
  boolean esEmailValido(String email);
}