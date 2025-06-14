package com.biblioteca.config;

import com.biblioteca.service.SessionManagementService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final SessionManagementService sessionManagementService;

  public CustomAuthenticationSuccessHandler(SessionManagementService sessionManagementService) {
    this.sessionManagementService = sessionManagementService;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    try {
      String username = authentication.getName();
      String sessionId = request.getSession().getId();
      String deviceInfo = determineDeviceInfo(request.getHeader("User-Agent"));

      // Obtener IP
      String ipAddress = getClientIpAddress(request);

      // Determinar ubicación basada en IP
      String location = determineLocation(ipAddress);

      sessionManagementService.registerSession(username, sessionId, deviceInfo, ipAddress, location);

      // Guardar la URL solicitada para después de la selección de perfil
      String requestedUrl = getRequestedUrl(request);
      if (requestedUrl != null && !isAuthRelatedUrl(requestedUrl)) {
        request.getSession().setAttribute("URL_AFTER_PROFILE_SELECTION", requestedUrl);
        System.out.println("🔍 DEBUG - URL guardada para después de selección de perfil: " + requestedUrl);
      }

      // Siempre redirigir a selección de perfil después del login
      // El PerfilInterceptor se encargará de verificar si hay perfil activo
      System.out.println("🔍 DEBUG - Redirigiendo a selección de perfil después del login");
      response.sendRedirect(request.getContextPath() + "/mi-cuenta/perfiles/seleccionar");
      
    } catch (Exception e) {
      // Log del error pero continuar con la redirección
      e.printStackTrace();
      response.sendRedirect(request.getContextPath() + "/mi-cuenta/perfiles/seleccionar");
    }
  }

  private String getRequestedUrl(HttpServletRequest request) {
    // 1. Verificar si hay una URL guardada manualmente en la sesión
    String requestedUrl = (String) request.getSession().getAttribute("REQUESTED_URL");
    if (requestedUrl != null && !requestedUrl.isEmpty()) {
      System.out.println("🔍 DEBUG - URL desde sesión manual: " + requestedUrl);
      return requestedUrl;
    }

    // 2. Verificar Spring Security SavedRequest
    org.springframework.security.web.savedrequest.SavedRequest savedRequest = 
        (org.springframework.security.web.savedrequest.SavedRequest) request.getSession()
            .getAttribute("SPRING_SECURITY_SAVED_REQUEST");
    
    if (savedRequest != null) {
      String savedUrl = savedRequest.getRedirectUrl();
      System.out.println("🔍 DEBUG - URL desde Spring Security: " + savedUrl);
      return savedUrl;
    }

    return null;
  }

  private boolean isAuthRelatedUrl(String url) {
    if (url == null) return true;
    
    // Extraer solo el path de la URL
    try {
      java.net.URL urlObj = new java.net.URL(url);
      String path = urlObj.getPath();
      return path.equals("/") || 
             path.equals("/login") || 
             path.equals("/registro") || 
             path.startsWith("/mi-cuenta/perfiles/");
    } catch (Exception e) {
      return true;
    }
  }

  private String getClientIpAddress(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }

    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }

    return request.getRemoteAddr();
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
    // Servicio de geolocalización basado en IP
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