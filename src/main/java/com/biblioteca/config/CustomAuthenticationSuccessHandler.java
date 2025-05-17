package com.biblioteca.config;

import com.biblioteca.service.SessionManagementService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

  private final SessionManagementService sessionManagementService;

  public CustomAuthenticationSuccessHandler(SessionManagementService sessionManagementService) {
    this.sessionManagementService = sessionManagementService;
    setDefaultTargetUrl("/mi-cuenta/perfiles/seleccionar");
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    String username = authentication.getName();
    String sessionId = request.getSession().getId();
    String deviceInfo = determineDeviceInfo(request.getHeader("User-Agent"));

    // Obtener IP
    String ipAddress = request.getRemoteAddr();

    // Determinar ubicación basada en IP ( MaxMind GeoIP o ipapi.com )
    String location = determineLocation(ipAddress);

    sessionManagementService.registerSession(username, sessionId, deviceInfo, ipAddress, location);

    super.onAuthenticationSuccess(request, response, authentication);
  }

  private String determineDeviceInfo(String userAgent) {
    if (userAgent == null)
      return "Dispositivo desconocido";

    if (userAgent.contains("Windows")) {
      return "Windows - " + (userAgent.contains("Chrome") ? "Chrome"
          : userAgent.contains("Firefox") ? "Firefox" : userAgent.contains("Edge") ? "Edge" : "Navegador desconocido");
    } else if (userAgent.contains("Mac")) {
      return "Mac - " + (userAgent.contains("Chrome") ? "Chrome"
          : userAgent.contains("Firefox") ? "Firefox"
              : userAgent.contains("Safari") ? "Safari" : "Navegador desconocido");
    } else if (userAgent.contains("Android")) {
      return "Android - " + (userAgent.contains("Chrome") ? "Chrome" : "Navegador móvil");
    } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
      return "iOS - " + (userAgent.contains("Safari") ? "Safari" : "Navegador móvil");
    }

    return "Dispositivo desconocido";
  }

  private String determineLocation(String ipAddress) {
    // Servicio de geolocalización basado en IP ( MaxMind GeoIP o ipapi.com )
    if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "127.0.0.1".equals(ipAddress)) {
      return "Local";
    } else if (ipAddress.startsWith("192.168.")) {
      return "Red local";
    } else if (ipAddress.startsWith("10.")) {
      return "Red privada";
    }

    // Para efectos de demostración, asignamos una ubicación ficticia
    return "Lima, Perú";
  }
}