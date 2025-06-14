package com.biblioteca.service;

import com.biblioteca.dto.validacion.prestamos.ValidacionLimitesResult;
import com.biblioteca.models.actividades.Prestamo;

public interface NotificacionPrestamoService {

  /**
   * Envía notificación cuando se realiza un préstamo exitoso
   */
  void enviarNotificacionPrestamoRealizado(Prestamo prestamo, ValidacionLimitesResult validacion);

  /**
   * Envía notificación cuando un préstamo está próximo a vencer
   */
  void enviarNotificacionProximoVencimiento(Prestamo prestamo, int diasRestantes);

  /**
   * Envía notificación cuando un préstamo ha vencido
   */
  void enviarNotificacionPrestamoVencido(Prestamo prestamo);

  /**
   * Envía notificación cuando se devuelve un préstamo
   */
  void enviarNotificacionPrestamoDevuelto(Prestamo prestamo);

  /**
   * Envía notificación cuando se renueva un préstamo
   */
  void enviarNotificacionPrestamoRenovado(Prestamo prestamo, int diasExtension);

  /**
   * Envía recomendación de upgrade de plan
   */
  void enviarRecomendacionUpgradePlan(Prestamo prestamo, ValidacionLimitesResult validacion);

  /**
   * Envía sugerencia de aumento de límite del perfil
   */
  void enviarSugerenciaAumentoLimitePerfil(Prestamo prestamo, ValidacionLimitesResult validacion);

  /**
   * Envía recordatorio de devolución próxima
   */
  void enviarRecordatorioDevoluccion(Prestamo prestamo, int diasRestantes);

  /**
   * Envía notificación de préstamo perdido
   */
  void enviarNotificacionPrestamoPerdido(Prestamo prestamo);
}