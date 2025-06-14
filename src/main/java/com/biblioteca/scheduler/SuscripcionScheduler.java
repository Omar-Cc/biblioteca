package com.biblioteca.scheduler;

import com.biblioteca.config.SuscripcionProperties;
import com.biblioteca.dto.comercial.PagoRequestDTO;
import com.biblioteca.dto.comercial.PagoResponseDTO;
import com.biblioteca.models.comercial.Suscripcion;
import com.biblioteca.repositories.comercial.SuscripcionRepository;
import com.biblioteca.service.NotificacionSuscripcionService;
import com.biblioteca.service.PagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuscripcionScheduler {
  private final SuscripcionRepository suscripcionRepository;
  private final PagoService pagoService;
  private final NotificacionSuscripcionService notificacionService;
  private final SuscripcionProperties suscripcionConfig;

  /**
   * Procesa todas las suscripciones vencidas diariamente
   * Esto incluye planes mensuales, anuales y de prueba
   */
  @Scheduled(cron = "0 0 2 * * ?") // Ejecutar a las 2 AM diariamente
  @Transactional
  public void procesarSuscripcionesVencidas() {
    log.info("Iniciando procesamiento de suscripciones vencidas...");

    procesarSuscripcionesPrueba();
    procesarSuscripcionesActivasVencidas();
    procesarSuscripcionesPendientesParaActivar();
    procesarNotificacionesProximasRenovaciones();

    log.info("Procesamiento de suscripciones completado");
  }

  /**
   * Procesa suscripciones que están en período de gracia y han vencido
   */
  @Scheduled(cron = "0 0 4 * * ?") // Ejecutar a las 4 AM diariamente
  @Transactional
  public void procesarSuscripcionesPendientesVencidas() {
    log.info("Procesando suscripciones pendientes vencidas...");

    List<Suscripcion> pendientesVencidas = suscripcionRepository
        .findSuscripcionesPendientesVencidas(LocalDate.now());

    pendientesVencidas.forEach(suscripcion -> {
      log.info("Suspendiendo suscripción {} por falta de pago", suscripcion.getId());
      suscripcion.setEstado("VENCIDA");
      suscripcionRepository.save(suscripcion);
      notificacionService.enviarNotificacionSuscripcionVencida(suscripcion);
    });

    log.info("Procesadas {} suscripciones pendientes vencidas", pendientesVencidas.size());
  }

  /**
   * Envía notificaciones de recordatorio al mediodía
   */
  @Scheduled(cron = "0 0 12 * * ?") // Ejecutar a las 12 PM diariamente
  @Transactional(readOnly = true)
  public void enviarNotificacionesRecordatorio() {
    log.info("Enviando notificaciones de recordatorio...");
    procesarNotificacionesProximasRenovaciones();
  }

  /**
   * Procesa suscripciones activas que vencen HOY
   * Aplica para cualquier tipo de plan (mensual, anual)
   */
  private void procesarSuscripcionesActivasVencidas() {
    List<Suscripcion> suscripcionesVencidas = suscripcionRepository
        .findByEstadoAndFechaRenovacion("ACTIVA", LocalDate.now());

    log.info("Procesando {} suscripciones activas que vencen hoy", suscripcionesVencidas.size());

    suscripcionesVencidas.forEach(this::procesarRenovacionSuscripcion);
  }

  /**
   * Procesa la renovación de una suscripción individual
   */
  private void procesarRenovacionSuscripcion(Suscripcion suscripcion) {
    int intentos = 0;
    boolean exitoso = false;

    while (intentos < 3 && !exitoso) {
      try {
        log.info("Procesando renovación de suscripción {} (intento {}/3)",
            suscripcion.getId(), intentos + 1);

        // Si es plan gratuito, solo renovar fechas
        if (suscripcion.getPlan().getPrecioMensual() == 0) {
          suscripcion.setFechaRenovacion(calcularProximaRenovacion(suscripcion));
          suscripcionRepository.save(suscripcion);
          exitoso = true;
          return;
        }

        // Para planes de pago, intentar cobro
        PagoResponseDTO resultado = procesarPagoSuscripcion(suscripcion);

        if ("EXITOSO".equals(resultado.getEstado())) {
          suscripcion.setFechaRenovacion(calcularProximaRenovacion(suscripcion));

          // Verificar que la notificación se envíe correctamente
          try {
            notificacionService.enviarNotificacionRenovacionExitosa(suscripcion);
          } catch (Exception e) {
            log.warn("Error enviando notificación de renovación exitosa: {}", e.getMessage());
          }

          exitoso = true;
        } else {
          suscripcion.setEstado("PENDIENTE_PAGO");
          suscripcion.setFechaRenovacion(LocalDate.now().plusDays(suscripcionConfig.getPeriodoGraciaDias()));

          try {
            notificacionService.enviarNotificacionPagoFallido(suscripcion);
          } catch (Exception e) {
            log.warn("Error enviando notificación de pago fallido: {}", e.getMessage());
          }

          exitoso = true; // El proceso fue exitoso aunque el pago falló
        }

        suscripcionRepository.save(suscripcion);

      } catch (Exception e) {
        intentos++;
        log.error("Error procesando renovación para suscripción {} (intento {}): {}",
            suscripcion.getId(), intentos, e.getMessage());

        if (intentos >= 3) {
          suscripcion.setEstado("VENCIDA");
          suscripcion.setMotivoCambio("Error en procesamiento después de 3 intentos: " + e.getMessage());
          suscripcionRepository.save(suscripcion);

          try {
            notificacionService.enviarNotificacionSuscripcionVencida(suscripcion);
          } catch (Exception notifError) {
            log.error("Error crítico: No se pudo enviar notificación de vencimiento: {}",
                notifError.getMessage());
          }
        }
      }
    }
  }

  /**
   * Procesa suscripciones en período de prueba que han vencido
   */
  private void procesarSuscripcionesPrueba() {
    List<Suscripcion> suscripcionesPrueba = suscripcionRepository
        .findByEstadoAndFechaRenovacionBefore("PRUEBA", LocalDate.now());

    log.info("Procesando {} suscripciones de prueba vencidas", suscripcionesPrueba.size());

    suscripcionesPrueba.forEach(suscripcion -> {
      if (suscripcion.getPlan().getPrecioMensual() == 0) {
        // Plan básico permanece activo (aunque no debería tener período de prueba)
        suscripcion.setEstado("ACTIVA");
        suscripcionRepository.save(suscripcion);
        notificacionService.enviarNotificacionFinPeriodoPrueba(suscripcion);
        return;
      } // Intentar cobro automático para planes de pago
      try {
        PagoResponseDTO resultado = procesarPagoSuscripcion(suscripcion);

        if ("EXITOSO".equals(resultado.getEstado())) {
          suscripcion.setEstado("ACTIVA");
          suscripcion.setFechaRenovacion(calcularProximaRenovacion(suscripcion));
          notificacionService.enviarNotificacionRenovacionExitosa(suscripcion);
          log.info("Conversión exitosa de prueba a activa para suscripción {}", suscripcion.getId());
        } else {
          suscripcion.setEstado("PENDIENTE_PAGO");
          suscripcion.setFechaRenovacion(LocalDate.now().plusDays(suscripcionConfig.getPeriodoGraciaDias()));
          notificacionService.enviarNotificacionPagoFallido(suscripcion);
          log.warn("Fallo en conversión de prueba para suscripción {}", suscripcion.getId());
        }

        suscripcionRepository.save(suscripcion);

      } catch (Exception e) {
        log.error("Error procesando suscripción de prueba {}: {}", suscripcion.getId(), e.getMessage());
        suscripcion.setEstado("VENCIDA");
        suscripcionRepository.save(suscripcion);
        notificacionService.enviarNotificacionSuscripcionVencida(suscripcion);
      }
    });
  }

  /**
   * Procesa suscripciones pendientes que deben activarse HOY
   * Especialmente usado para downgrades diferidos
   */
  private void procesarSuscripcionesPendientesParaActivar() {
    List<Suscripcion> suscripcionesPendientes = suscripcionRepository
        .findByEstadoAndFechaRenovacion("PENDIENTE", LocalDate.now());

    log.info("Procesando {} suscripciones pendientes para activar", suscripcionesPendientes.size());

    suscripcionesPendientes.forEach(suscripcionPendiente -> {
      try {
        // Buscar y cancelar la suscripción anterior activa del mismo usuario
        Optional<Suscripcion> suscripcionActivaOpt = suscripcionRepository
            .findByUsuarioIdAndEstado(suscripcionPendiente.getUsuario().getId(), "ACTIVA");

        if (suscripcionActivaOpt.isPresent()) {
          Suscripcion suscripcionActiva = suscripcionActivaOpt.get();
          log.info("Cancelando suscripción activa {} para activar pendiente {}",
              suscripcionActiva.getId(), suscripcionPendiente.getId());
          suscripcionActiva.setEstado("CANCELADA");
          suscripcionActiva.setMotivoCambio("Reemplazada por downgrade programado");
          suscripcionRepository.save(suscripcionActiva);
        }

        // Activar la suscripción pendiente
        suscripcionPendiente.setEstado("ACTIVA");
        suscripcionPendiente.setMotivoCambio("Activada por programación de downgrade");
        suscripcionRepository.save(suscripcionPendiente);

        log.info("Suscripción pendiente {} activada exitosamente para usuario {}",
            suscripcionPendiente.getId(), suscripcionPendiente.getUsuario().getUsername());

        // Enviar notificación de activación usando una notificación existente
        notificacionService.enviarNotificacionRenovacionExitosa(suscripcionPendiente);

      } catch (Exception e) {
        log.error("Error activando suscripción pendiente {}: {}",
            suscripcionPendiente.getId(), e.getMessage());
        // Marcar como error para revisión manual
        suscripcionPendiente.setMotivoCambio("Error en activación: " + e.getMessage());
        suscripcionRepository.save(suscripcionPendiente);
      }
    });
  }

  /**
   * Envía notificaciones previas al vencimiento
   */
  private void procesarNotificacionesProximasRenovaciones() {
    for (Integer dias : suscripcionConfig.getDiasNotificacionPrevia()) {
      List<Suscripcion> proximasRenovaciones = suscripcionRepository
          .findByEstadoAndFechaRenovacionBetween("ACTIVA",
              LocalDate.now().plusDays(dias), LocalDate.now().plusDays(dias + 1));

      if (!proximasRenovaciones.isEmpty()) {
        log.info("Enviando {} notificaciones de vencimiento en {} días",
            proximasRenovaciones.size(), dias);
        proximasRenovaciones.forEach(s -> notificacionService.enviarNotificacionProximoVencimiento(s, dias));
      }
    }
  }

  /**
   * Procesa un pago de suscripción usando el servicio unificado de pagos
   */
  private PagoResponseDTO procesarPagoSuscripcion(Suscripcion suscripcion) {
    try {
      // Crear el DTO de pago para la suscripción
      PagoRequestDTO pagoRequest = PagoRequestDTO.builder()
          .suscripcionId(suscripcion.getId())
          .metodoPagoId(suscripcion.getMetodoPago().getId())
          .monto(calcularMontoSuscripcion(suscripcion))
          .referenciaPago("SUSCRIPCION_" + suscripcion.getId() + "_" + System.currentTimeMillis())
          // Campos para simulación
          .esSimulado(true)
          .simularFallo(false) // En producción esto podría ser configurable
          .build();

      return pagoService.registrarPago(pagoRequest);
    } catch (Exception e) {
      log.error("Error procesando pago para suscripción {}: {}", suscripcion.getId(), e.getMessage());
      // Crear una respuesta de fallo
      return PagoResponseDTO.builder()
          .estado("FALLIDO")
          .mensaje("Error en procesamiento: " + e.getMessage())
          .build();
    }
  }

  /**
   * Calcula el monto de la suscripción basado en la modalidad de pago
   */
  private Integer calcularMontoSuscripcion(Suscripcion suscripcion) {
    if ("ANUAL".equals(suscripcion.getModalidadPago())) {
      // Para pagos anuales, usar precio anual si existe, sino calcular 12 meses
      Integer precioAnual = suscripcion.getPlan().getPrecioAnual();
      if (precioAnual != null && precioAnual > 0) {
        return precioAnual;
      } else {
        return suscripcion.getPlan().getPrecioMensual() * 12;
      }
    } else {
      // Para pagos mensuales o por defecto
      return suscripcion.getPlan().getPrecioMensual();
    }
  }

  /**
   * Calcula la próxima fecha de renovación basada en la modalidad de pago elegida
   * por el usuario
   */
  private LocalDate calcularProximaRenovacion(Suscripcion suscripcion) {
    LocalDate fechaActual = LocalDate.now();

    // Para planes gratuitos, usar período de facturación del plan
    if (suscripcion.getPlan().getPrecioMensual() == 0) {
      String periodo = suscripcion.getPlan().getPeriodoFacturacion();
      if ("Gratuito".equals(periodo)) {
        return fechaActual.plusYears(1);
      } else {
        return fechaActual.plusMonths(1);
      }
    }

    // Para planes de pago, usar la modalidad de pago elegida por el usuario
    String modalidadPago = suscripcion.getModalidadPago();

    if (modalidadPago == null) {
      // Fallback para suscripciones existentes sin modalidad definida
      log.warn("Modalidad de pago no definida para suscripción {}. Usando período del plan por defecto.",
          suscripcion.getId());
      String periodoDefecto = suscripcion.getPlan().getPeriodoFacturacion();
      if ("ANUAL".equals(periodoDefecto)) {
        return fechaActual.plusYears(1);
      } else {
        return fechaActual.plusMonths(1);
      }
    }

    if ("MENSUAL".equals(modalidadPago)) {
      return fechaActual.plusMonths(1);
    } else if ("ANUAL".equals(modalidadPago)) {
      return fechaActual.plusYears(1);
    } else {
      log.warn("Modalidad de pago desconocida '{}' para suscripción {}. Usando mensual por defecto.",
          modalidadPago, suscripcion.getId());
      return fechaActual.plusMonths(1);
    }
  }
}