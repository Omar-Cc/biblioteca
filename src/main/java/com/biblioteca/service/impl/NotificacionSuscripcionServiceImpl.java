package com.biblioteca.service.impl;

import org.springframework.stereotype.Service;

import com.biblioteca.models.comercial.Suscripcion;
import com.biblioteca.service.EmailService;
import com.biblioteca.service.NotificacionSuscripcionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionSuscripcionServiceImpl implements NotificacionSuscripcionService {

  private final EmailService emailService;

  @Override
  public void enviarNotificacionFinPeriodoPrueba(Suscripcion suscripcion) {
    log.info("Enviando notificación de fin de período de prueba para usuario: {}",
        suscripcion.getUsuario().getUsername());

    Map<String, Object> variables = new HashMap<>();
    variables.put("nombreUsuario", suscripcion.getUsuario().getUsername());
    variables.put("nombrePlan", suscripcion.getPlan().getNombre());
    variables.put("esGratuito", suscripcion.getPlan().getPrecioMensual() == 0);
    variables.put("precio", suscripcion.getPlan().getPrecioMensual());

    boolean enviado = emailService.enviarEmailConPlantilla(
        suscripcion.getUsuario().getEmail(),
        "Tu período de prueba ha terminado",
        "suscripcion/fin-periodo-prueba",
        variables);

    if (!enviado) {
      log.error("Error enviando notificación de fin de período de prueba a: {}",
          suscripcion.getUsuario().getEmail());
    }
  }

  @Override
  public void enviarNotificacionProximoVencimiento(Suscripcion suscripcion, int diasRestantes) {
    log.info("Enviando notificación de próximo vencimiento ({} días) para usuario: {}",
        diasRestantes, suscripcion.getUsuario().getUsername());

    Map<String, Object> variables = new HashMap<>();
    variables.put("nombreUsuario", suscripcion.getUsuario().getUsername());
    variables.put("nombrePlan", suscripcion.getPlan().getNombre());
    variables.put("diasRestantes", diasRestantes);
    variables.put("fechaRenovacion", suscripcion.getFechaRenovacion());
    variables.put("metodoPago", suscripcion.getMetodoPago().getTipo());

    String asunto = String.format("Tu suscripción vence en %d día%s",
        diasRestantes, diasRestantes == 1 ? "" : "s");

    boolean enviado = emailService.enviarEmailConPlantilla(
        suscripcion.getUsuario().getEmail(),
        asunto,
        "suscripcion/proximo-vencimiento",
        variables);

    if (!enviado) {
      log.error("Error enviando notificación de próximo vencimiento a: {}",
          suscripcion.getUsuario().getEmail());
    }
  }

  @Override
  public void enviarNotificacionSuscripcionVencida(Suscripcion suscripcion) {
    log.info("Enviando notificación de suscripción vencida para usuario: {}",
        suscripcion.getUsuario().getUsername());

    Map<String, Object> variables = new HashMap<>();
    variables.put("nombreUsuario", suscripcion.getUsuario().getUsername());
    variables.put("nombrePlan", suscripcion.getPlan().getNombre());

    boolean enviado = emailService.enviarEmailConPlantilla(
        suscripcion.getUsuario().getEmail(),
        "Tu suscripción ha vencido",
        "suscripcion/suscripcion-vencida",
        variables);

    if (!enviado) {
      log.error("Error enviando notificación de suscripción vencida a: {}",
          suscripcion.getUsuario().getEmail());
    }
  }

  @Override
  public void enviarNotificacionPagoFallido(Suscripcion suscripcion) {
    log.info("Enviando notificación de pago fallido para usuario: {}",
        suscripcion.getUsuario().getUsername());

    Map<String, Object> variables = new HashMap<>();
    variables.put("nombreUsuario", suscripcion.getUsuario().getUsername());
    variables.put("nombrePlan", suscripcion.getPlan().getNombre());
    variables.put("diasGracia", 3);

    boolean enviado = emailService.enviarEmailConPlantilla(
        suscripcion.getUsuario().getEmail(),
        "Problema con el pago de tu suscripción",
        "suscripcion/pago-fallido",
        variables);

    if (!enviado) {
      log.error("Error enviando notificación de pago fallido a: {}",
          suscripcion.getUsuario().getEmail());
    }
  }

  @Override
  public void enviarNotificacionRenovacionExitosa(Suscripcion suscripcion) {
    log.info("Enviando notificación de renovación exitosa para usuario: {}",
        suscripcion.getUsuario().getUsername());

    Map<String, Object> variables = new HashMap<>();
    variables.put("nombreUsuario", suscripcion.getUsuario().getUsername());
    variables.put("nombrePlan", suscripcion.getPlan().getNombre());
    variables.put("fechaRenovacion", suscripcion.getFechaRenovacion());
    variables.put("metodoPago", suscripcion.getMetodoPago().getTipo());

    boolean enviado = emailService.enviarEmailConPlantilla(
        suscripcion.getUsuario().getEmail(),
        "Tu suscripción ha sido renovada exitosamente",
        "suscripcion/renovacion-exitosa",
        variables);

    if (!enviado) {
      log.error("Error enviando notificación de renovación exitosa a: {}",
          suscripcion.getUsuario().getEmail());
    }
  }
}