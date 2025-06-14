package com.biblioteca.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {
  private boolean enabled = true;
  private boolean debug = false;
  private String from = "noreply@biblioteca.com";
  private String fromName = "Biblioteca Digital";

  // Plantillas de email
  private String templatePath = "templates/email/";
  private String logoUrl = "https://biblioteca.com/logo.png";

  // Configuración de reintento
  private int maxReintentos = 3;
  private long tiempoEsperaReintento = 5000; // 5 segundos

  // Configuración de lotes para envíos masivos
  private int tamanoLote = 50;
  private long tiempoEsperaEntreLotes = 1000; // 1 segundo

  // Emails administrativos
  private List<String> emailsAdmin = List.of("admin@biblioteca.com");
  private String emailSoporte = "soporte@biblioteca.com";

  // Configuración de tipos de notificación
  private boolean notificacionesSuscripcion = true;
  private boolean notificacionesPerfil = true;
  private boolean notificacionesSeguridad = true;

  // Configuración de seguridad
  private boolean verificarCertificadoSSL = true;
  private String certificadoSSLPath = "/path/to/certificado.p12";
  private String certificadoSSLPassword = "password";

  // Configuración de límites de envío
  private int limiteEnviosDiarios = 1000;
  private int limiteEnviosPorHora = 100;

  // Configuración de seguimiento de envíos
  private boolean seguimientoEnvios = true;
  
}