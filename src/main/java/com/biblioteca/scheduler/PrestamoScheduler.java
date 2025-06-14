package com.biblioteca.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.service.PrestamoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrestamoScheduler {

  private final PrestamoService prestamoService;

  /**
   * Procesa vencimientos automáticos diariamente a las 3 AM
   */
  @Scheduled(cron = "0 0 3 * * ?")
  @Transactional
  public void procesarVencimientosAutomaticos() {
    log.info("Iniciando procesamiento automático de vencimientos de préstamos...");

    try {
      prestamoService.procesarVencimientosAutomaticos();
      log.info("Procesamiento de vencimientos completado exitosamente");
    } catch (Exception e) {
      log.error("Error en procesamiento automático de vencimientos: {}", e.getMessage(), e);
    }
  }

  /**
   * Envía notificaciones de recordatorio a las 10 AM
   */
  @Scheduled(cron = "0 0 10 * * ?")
  @Transactional(readOnly = true)
  public void enviarNotificacionesRecordatorio() {
    log.info("Enviando notificaciones de recordatorio de préstamos...");

    try {
      // Notificar préstamos que vencen en 1 día
      var proximosAVencer1 = prestamoService.obtenerPrestamosProximosAVencer(1);
      log.info("Préstamos que vencen en 1 día: {}", proximosAVencer1.size());

      // Notificar préstamos que vencen en 3 días
      var proximosAVencer3 = prestamoService.obtenerPrestamosProximosAVencer(3);
      log.info("Préstamos que vencen en 3 días: {}", proximosAVencer3.size());

      // TODO: Implementar envío de notificaciones

    } catch (Exception e) {
      log.error("Error enviando notificaciones de recordatorio: {}", e.getMessage(), e);
    }
  }
}