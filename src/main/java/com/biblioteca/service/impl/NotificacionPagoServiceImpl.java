package com.biblioteca.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.biblioteca.models.comercial.Pago;
import com.biblioteca.service.EmailService;
import com.biblioteca.service.NotificacionPagoService;
import com.biblioteca.service.PdfService;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class NotificacionPagoServiceImpl implements NotificacionPagoService {

  private final EmailService emailService;
  private final PdfService pdfService;

  public NotificacionPagoServiceImpl(
      EmailService emailService,
      @Lazy PdfService pdfService) {
    this.emailService = emailService;
    this.pdfService = pdfService;
  }

  @Override
  public void enviarNotificacionPagoExitoso(Pago pago) {
    log.info("Enviando notificación de pago exitoso para pago ID: {}", pago.getId());

    try {
      String destinatario = obtenerEmailDestinatario(pago);
      if (destinatario == null) {
        log.warn("No se pudo obtener email para notificación de pago {}", pago.getId());
        return;
      }

      Map<String, Object> variables = prepararVariablesPago(pago);
      variables.put("estado", "EXITOSO");

      // Generar PDF del recibo
      byte[] reciboPdf = pdfService.generarReciboPago(pago.getId());

      // Preparar adjuntos
      Map<String, byte[]> adjuntos = new HashMap<>();
      adjuntos.put("recibo-pago-" + pago.getId() + ".pdf", reciboPdf);

      boolean enviado = emailService.enviarEmailConPlantillaYAdjuntos(
          destinatario,
          "Confirmación de pago - Biblioteca Digital",
          "email/pagos/pago-exitoso",
          variables,
          adjuntos);

      if (!enviado) {
        log.error("Error enviando notificación de pago exitoso a: {}", destinatario);
      }

    } catch (Exception e) {
      log.error("Error enviando notificación de pago exitoso para pago {}: {}", pago.getId(), e.getMessage());
    }
  }

  @Override
  public void enviarNotificacionPagoFallido(Pago pago) {
    log.info("Enviando notificación de pago fallido para pago ID: {}", pago.getId());

    try {
      String destinatario = obtenerEmailDestinatario(pago);
      if (destinatario == null) {
        return;
      }

      Map<String, Object> variables = prepararVariablesPago(pago);
      variables.put("estado", "FALLIDO");
      variables.put("motivoRechazo", pago.getMotivoRechazo());

      boolean enviado = emailService.enviarEmailConPlantilla(
          destinatario,
          "Problema con tu pago - Biblioteca Digital",
          "email/pagos/pago-fallido",
          variables);

      if (!enviado) {
        log.error("Error enviando notificación de pago fallido a: {}", destinatario);
      }

    } catch (Exception e) {
      log.error("Error enviando notificación de pago fallido para pago {}: {}", pago.getId(), e.getMessage());
    }
  }

  @Override
  public void enviarNotificacionPagoPendiente(Pago pago) {
    log.info("Enviando notificación de pago pendiente para pago ID: {}", pago.getId());

    try {
      String destinatario = obtenerEmailDestinatario(pago);
      if (destinatario == null) {
        return;
      }

      Map<String, Object> variables = prepararVariablesPago(pago);
      variables.put("estado", "PENDIENTE");

      boolean enviado = emailService.enviarEmailConPlantilla(
          destinatario,
          "Tu pago está siendo procesado - Biblioteca Digital",
          "email/pagos/pago-pendiente",
          variables);

      if (!enviado) {
        log.error("Error enviando notificación de pago pendiente a: {}", destinatario);
      }

    } catch (Exception e) {
      log.error("Error enviando notificación de pago pendiente para pago {}: {}", pago.getId(), e.getMessage());
    }
  }

  @Override
  public void enviarNotificacionPagoEnProceso(Pago pago) {
    log.info("Enviando notificación de pago en proceso para pago ID: {}", pago.getId());

    try {
      String destinatario = obtenerEmailDestinatario(pago);
      if (destinatario == null) {
        return;
      }

      Map<String, Object> variables = prepararVariablesPago(pago);
      variables.put("estado", "EN_PROCESO");

      boolean enviado = emailService.enviarEmailConPlantilla(
          destinatario,
          "Procesando tu pago - Biblioteca Digital",
          "email/pagos/pago-proceso",
          variables);

      if (!enviado) {
        log.error("Error enviando notificación de pago en proceso a: {}", destinatario);
      }

    } catch (Exception e) {
      log.error("Error enviando notificación de pago en proceso para pago {}: {}", pago.getId(), e.getMessage());
    }
  }

  @Override
  public void enviarNotificacionReembolso(Pago pago) {
    log.info("Enviando notificación de reembolso para pago ID: {}", pago.getId());

    try {
      String destinatario = obtenerEmailDestinatario(pago);
      if (destinatario == null) {
        return;
      }

      Map<String, Object> variables = prepararVariablesPago(pago);
      variables.put("estado", "REEMBOLSADO");

      boolean enviado = emailService.enviarEmailConPlantilla(
          destinatario,
          "Reembolso procesado - Biblioteca Digital",
          "email/pagos/reembolso",
          variables);

      if (!enviado) {
        log.error("Error enviando notificación de reembolso a: {}", destinatario);
      }

    } catch (Exception e) {
      log.error("Error enviando notificación de reembolso para pago {}: {}", pago.getId(), e.getMessage());
    }
  }

  private String obtenerEmailDestinatario(Pago pago) {
    if (pago.getOrden() != null && pago.getOrden().getPerfil() != null
        && pago.getOrden().getPerfil().getUsuario() != null) {
      return pago.getOrden().getPerfil().getUsuario().getEmail();
    }

    if (pago.getSuscripcion() != null && pago.getSuscripcion().getUsuario() != null) {
      return pago.getSuscripcion().getUsuario().getEmail();
    }

    return null;
  }

  private String obtenerNombreCliente(Pago pago) {
    try {
      if (pago == null) {
        return "Cliente";
      }

      // Verificar si es un pago de orden
      if (pago.getOrden() != null && pago.getOrden().getPerfil() != null) {
        String nombreVisible = pago.getOrden().getPerfil().getNombreVisible();
        return nombreVisible != null ? nombreVisible : "Cliente";
      }

      // Verificar si es un pago de suscripción
      if (pago.getSuscripcion() != null && pago.getSuscripcion().getUsuario() != null) {
        if (pago.getSuscripcion().getUsuario().getLector() != null) {
          String nombreCompleto = pago.getSuscripcion().getUsuario().getLector().getNombreCompleto();
          return nombreCompleto != null ? nombreCompleto : "Cliente";
        }
      }

      return "Cliente";
    } catch (Exception e) {
      log.warn("Error obteniendo nombre del cliente para pago {}: {}",
          pago != null ? pago.getId() : "null", e.getMessage());
      return "Cliente";
    }
  }

  private Map<String, Object> prepararVariablesPago(Pago pago) {
    Map<String, Object> variables = new HashMap<>();

    try {
      variables.put("pago", pago);
      variables.put("nombreCliente", obtenerNombreCliente(pago));
      variables.put("fechaPago", pago.getFechaPago() != null ? pago.getFechaPago() : "No disponible");
      variables.put("monto", pago.getMonto() != null ? pago.getMonto() / 100.0 : 0.0);
      String metodoPago = "No especificado";
      if (pago.getMetodoPago() != null && pago.getMetodoPago().getTipo() != null) {
        metodoPago = pago.getMetodoPago().getTipo();
      }
      variables.put("metodoPago", metodoPago);
      variables.put("referenciaPago", pago.getReferenciaPago() != null ? pago.getReferenciaPago() : "Sin referencia");

    } catch (Exception e) {
      log.warn("Error preparando variables para pago {}: {}",
          pago != null ? pago.getId() : "null", e.getMessage());
      variables.putIfAbsent("nombreCliente", "Cliente");
      variables.putIfAbsent("fechaPago", "No disponible");
      variables.putIfAbsent("monto", 0.0);
      variables.putIfAbsent("metodoPago", "No especificado");
      variables.putIfAbsent("referenciaPago", "Sin referencia");
    }

    return variables;
  }

  @Override
  public void enviarNotificacionPagoRechazado(Pago pago) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'enviarNotificacionPagoRechazado'");
  }

  @Override
  public void enviarNotificacionPagoParcial(Pago pago) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'enviarNotificacionPagoParcial'");
  }

  @Override
  public void enviarNotificacionPagoCompleto(Pago pago) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'enviarNotificacionPagoCompleto'");
  }

  @Override
  public void enviarNotificacionPagoConDescuento(Pago pago) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'enviarNotificacionPagoConDescuento'");
  }
}