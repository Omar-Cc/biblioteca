package com.biblioteca.service;

import com.biblioteca.models.comercial.Pago;

public interface NotificacionPagoService {

  void enviarNotificacionPagoExitoso(Pago pago);

  void enviarNotificacionPagoFallido(Pago pago);

  void enviarNotificacionPagoPendiente(Pago pago);

  void enviarNotificacionPagoEnProceso(Pago pago);

  void enviarNotificacionReembolso(Pago pago);

  void enviarNotificacionPagoRechazado(Pago pago);

  void enviarNotificacionPagoParcial(Pago pago);

  void enviarNotificacionPagoCompleto(Pago pago);

  void enviarNotificacionPagoConDescuento(Pago pago);
}