package com.biblioteca.service;

import com.biblioteca.models.comercial.Suscripcion;

public interface NotificacionSuscripcionService {

  void enviarNotificacionFinPeriodoPrueba(Suscripcion suscripcion);

  void enviarNotificacionProximoVencimiento(Suscripcion suscripcion, int diasRestantes);

  void enviarNotificacionSuscripcionVencida(Suscripcion suscripcion);

  void enviarNotificacionPagoFallido(Suscripcion suscripcion);

  void enviarNotificacionRenovacionExitosa(Suscripcion suscripcion);
}