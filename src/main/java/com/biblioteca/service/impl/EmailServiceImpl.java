package com.biblioteca.service.impl;

import com.biblioteca.config.EmailProperties;
import com.biblioteca.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.email.enabled", havingValue = "true", matchIfMissing = false)
public class EmailServiceImpl implements EmailService {

  private final EmailProperties emailConfig;
  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;

  private static final Pattern EMAIL_PATTERN = Pattern.compile(
      "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

  @Override
  public boolean enviarEmail(String destinatario, String asunto, String contenido) {
    if (!esEmailValido(destinatario)) {
      log.error("Email inválido: {}", destinatario);
      return false;
    }

    try {
      if (emailConfig.isDebug()) {
        simularEnvio(destinatario, asunto, contenido);
        return true;
      }

      SimpleMailMessage mensaje = new SimpleMailMessage();
      mensaje.setFrom(emailConfig.getFrom());
      mensaje.setTo(destinatario);
      mensaje.setSubject(asunto);
      mensaje.setText(contenido);

      mailSender.send(mensaje);
      log.info("Email enviado exitosamente a: {}", destinatario);
      return true;

    } catch (MailException e) {
      log.error("Error enviando email a {}: {}", destinatario, e.getMessage());
      return false;
    }
  }

  @Override
  public boolean enviarEmailHtml(String destinatario, String asunto, String contenidoHtml) {
    if (!esEmailValido(destinatario)) {
      log.error("Email inválido: {}", destinatario);
      return false;
    }

    try {
      if (emailConfig.isDebug()) {
        simularEnvioHtml(destinatario, asunto, contenidoHtml);
        return true;
      }

      MimeMessage mensaje = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

      configurarRemitente(helper);
      helper.setTo(destinatario);
      helper.setSubject(asunto);
      helper.setText(contenidoHtml, true);

      mailSender.send(mensaje);
      log.info("Email HTML enviado exitosamente a: {}", destinatario);
      return true;

    } catch (MessagingException | MailException e) {
      log.error("Error enviando email HTML a {}: {}", destinatario, e.getMessage());
      return false;
    }
  }

  @Override
  public boolean enviarEmailConPlantilla(String destinatario, String asunto, String plantilla,
      Map<String, Object> variables) {
    try {
      Context context = new Context();
      variables.forEach(context::setVariable);

      String contenidoHtml = templateEngine.process(plantilla, context);
      return enviarEmailHtml(destinatario, asunto, contenidoHtml);

    } catch (Exception e) {
      log.error("Error procesando plantilla {} para {}: {}", plantilla, destinatario, e.getMessage());
      return false;
    }
  }

  @Override
  public boolean enviarEmailMasivo(String[] destinatarios, String asunto, String contenido) {
    boolean todoExitoso = true;

    for (String destinatario : destinatarios) {
      if (!enviarEmail(destinatario, asunto, contenido)) {
        todoExitoso = false;
      }
    }

    return todoExitoso;
  }

  @Override
  public boolean enviarEmailConAdjuntos(String destinatario, String asunto, String contenido,
      Map<String, byte[]> adjuntos) {
    if (!esEmailValido(destinatario)) {
      return false;
    }

    try {
      if (emailConfig.isDebug()) {
        simularEnvioConAdjuntos(destinatario, asunto, contenido, adjuntos);
        return true;
      }

      MimeMessage mensaje = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

      configurarRemitente(helper);
      helper.setTo(destinatario);
      helper.setSubject(asunto);
      helper.setText(contenido);

      adjuntos.forEach((nombre, datos) -> {
        try {
          helper.addAttachment(nombre, () -> new java.io.ByteArrayInputStream(datos));
        } catch (MessagingException e) {
          log.error("Error agregando adjunto {}: {}", nombre, e.getMessage());
        }
      });

      mailSender.send(mensaje);
      log.info("Email con adjuntos enviado exitosamente a: {}", destinatario);
      return true;

    } catch (Exception e) {
      log.error("Error enviando email con adjuntos a {}: {}", destinatario, e.getMessage());
      return false;
    }
  }

  @Override
  public boolean enviarEmailConPlantillaYAdjuntos(String destinatario, String asunto, String plantilla,
      Map<String, Object> variables, Map<String, byte[]> adjuntos) {
    if (!esEmailValido(destinatario)) {
      log.error("Email inválido: {}", destinatario);
      return false;
    }

    try {
      // Procesar plantilla HTML
      Context context = new Context();
      variables.forEach(context::setVariable);
      String contenidoHtml = templateEngine.process(plantilla, context);

      if (emailConfig.isDebug()) {
        simularEnvioConPlantillaYAdjuntos(destinatario, asunto, contenidoHtml, adjuntos);
        return true;
      }

      // Crear mensaje con adjuntos
      MimeMessage mensaje = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

      configurarRemitente(helper);
      helper.setTo(destinatario);
      helper.setSubject(asunto);
      helper.setText(contenidoHtml, true); // true = contenido HTML

      // Agregar adjuntos
      if (adjuntos != null && !adjuntos.isEmpty()) {
        adjuntos.forEach((nombre, datos) -> {
          try {
            helper.addAttachment(nombre, () -> new java.io.ByteArrayInputStream(datos));
            log.debug("Adjunto agregado: {} ({} bytes)", nombre, datos.length);
          } catch (MessagingException e) {
            log.error("Error agregando adjunto {}: {}", nombre, e.getMessage());
          }
        });
      }

      mailSender.send(mensaje);
      log.info("Email con plantilla y adjuntos enviado exitosamente a: {} (adjuntos: {})",
          destinatario, adjuntos.size());
      return true;

    } catch (Exception e) {
      log.error("Error enviando email con plantilla y adjuntos a {}: {}", destinatario, e.getMessage());
      return false;
    }
  }

  @Override
  public boolean verificarServicioDisponible() {
    try {
      // Intentar crear un mensaje simple para verificar conectividad
      mailSender.createMimeMessage();
      return true;
    } catch (Exception e) {
      log.error("Servicio de email no disponible: {}", e.getMessage());
      return false;
    }
  }

  @Override
  public boolean esEmailValido(String email) {
    return email != null && EMAIL_PATTERN.matcher(email).matches();
  }

  private void simularEnvio(String destinatario, String asunto, String contenido) {
    log.info("=== EMAIL SIMULADO ===");
    log.info("De: {} <{}>", emailConfig.getFromName(), emailConfig.getFrom());
    log.info("Para: {}", destinatario);
    log.info("Asunto: {}", asunto);
    log.info("Contenido:\n{}", contenido);
    log.info("=====================");
  }

  private void simularEnvioHtml(String destinatario, String asunto, String contenidoHtml) {
    log.info("=== EMAIL HTML SIMULADO ===");
    log.info("Para: {}", destinatario);
    log.info("Asunto: {}", asunto);
    log.info("Contenido HTML:\n{}", contenidoHtml);
    log.info("===========================");
  }

  private void simularEnvioConAdjuntos(String destinatario, String asunto, String contenido,
      Map<String, byte[]> adjuntos) {
    log.info("=== EMAIL CON ADJUNTOS SIMULADO ===");
    log.info("De: {} <{}>", emailConfig.getFromName(), emailConfig.getFrom());
    log.info("Para: {}", destinatario);
    log.info("Asunto: {}", asunto);
    log.info("Contenido:\n{}", contenido);
    log.info("Adjuntos:");
    adjuntos.forEach((nombre, datos) -> {
      log.info("  - {} ({} bytes)", nombre, datos.length);
    });
    log.info("===================================");
  }

  private void simularEnvioConPlantillaYAdjuntos(String destinatario, String asunto, String contenidoHtml,
      Map<String, byte[]> adjuntos) {
    log.info("=== EMAIL CON PLANTILLA Y ADJUNTOS SIMULADO ===");
    log.info("De: {} <{}>", emailConfig.getFromName(), emailConfig.getFrom());
    log.info("Para: {}", destinatario);
    log.info("Asunto: {}", asunto);
    log.info("Contenido HTML:\n{}", contenidoHtml);
    log.info("Adjuntos:");
    adjuntos.forEach((nombre, datos) -> {
      log.info("  - {} ({} bytes)", nombre, datos.length);
    });
    log.info("===============================================");
  }

  /**
   * Configura el remitente del mensaje de forma segura
   */
  private void configurarRemitente(MimeMessageHelper helper) throws MessagingException {
    try {
      helper.setFrom(emailConfig.getFrom(), emailConfig.getFromName());
    } catch (UnsupportedEncodingException e) {
      // Fallback: usar solo el email sin nombre personalizado
      helper.setFrom(emailConfig.getFrom());
      log.warn("No se pudo establecer el nombre del remitente '{}': {}",
          emailConfig.getFromName(), e.getMessage());
    }
  }
}