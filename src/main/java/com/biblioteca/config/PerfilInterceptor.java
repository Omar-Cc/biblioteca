package com.biblioteca.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class PerfilInterceptor implements HandlerInterceptor {

  // Rutas específicas excluidas SOLO para la gestión de perfiles
  private static final List<String> EXCLUDED_PATHS = Arrays.asList(
      "/mi-cuenta/perfiles/seleccionar",
      "/mi-cuenta/perfiles/nuevo",
      "/mi-cuenta/perfiles/crear");

  // Rutas técnicas que siempre deben ser excluidas
  private static final List<String> TECHNICAL_EXCLUDED_PATHS = Arrays.asList(
      "/css/", "/js/", "/images/", "/webjars/", "/favicon.ico");

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String requestPath = request.getRequestURI();

    log.debug("Interceptando ruta: {}", requestPath);

    // Excluir recursos estáticos siempre
    for (String technicalPath : TECHNICAL_EXCLUDED_PATHS) {
      if (requestPath.startsWith(technicalPath)) {
        log.debug("Recurso estático excluido: {}", requestPath);
        return true;
      }
    }

    // Verificar si el usuario está autenticado
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
      log.debug("Usuario no autenticado, permitiendo acceso: {}", requestPath);
      return true; // Usuario no autenticado, permitir acceso
    }

    log.debug("Usuario autenticado: {}, interceptando ruta: {}", auth.getName(), requestPath);

    // Verificar rutas específicamente excluidas (gestión de perfiles)
    for (String excludedPath : EXCLUDED_PATHS) {
      if (requestPath.startsWith(excludedPath)) {
        log.debug("Ruta excluida para gestión de perfiles: {}", excludedPath);
        return true;
      }
    }

    // Excluir específicamente las rutas de activación de perfil
    if (requestPath.matches("^/mi-cuenta/perfiles/\\d+/activar$")) {
      log.debug("Ruta de activación de perfil excluida: {}", requestPath);
      return true;
    }

    // Para usuarios autenticados, TODAS las demás rutas requieren perfil activo
    log.debug("Usuario autenticado accediendo a ruta que requiere perfil: {}", requestPath);

    // Verificar parámetro perfilActivado
    String perfilActivado = request.getParameter("perfilActivado");
    if ("true".equals(perfilActivado)) {
      log.debug("Permitiendo acceso con perfilActivado=true");
      return true;
    }

    // Verificar sesión y perfil activo
    HttpSession session = request.getSession(false);
    if (session == null) {
      log.warn("Sesión no encontrada para usuario autenticado, redirigiendo a login");
      response.sendRedirect(request.getContextPath() + "/login");
      return false;
    }

    // Verificar bandera especial de perfil recién activado
    if (session.getAttribute("perfilRecienActivado") != null) {
      log.info("Permitiendo acceso porque se acaba de activar un perfil");
      session.removeAttribute("perfilRecienActivado");
      return true;
    }

    // Verificar si hay un perfil activo
    Object perfilActivoId = session.getAttribute("perfilActivoId");
    if (perfilActivoId == null) {
      log.warn("Perfil no encontrado, redirigiendo a selección de perfil desde: {}", requestPath);
      
      // IMPORTANTE: Solo guardar la URL actual si no hay una ya guardada
      // para no sobrescribir la URL original que viene del login
      if (!isAuthOrProfileRelatedUrl(requestPath) && 
          session.getAttribute("URL_AFTER_PROFILE_SELECTION") == null) {
        String currentUrl = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
          currentUrl += "?" + queryString;
        }
        session.setAttribute("URL_AFTER_PROFILE_SELECTION", currentUrl);
        log.info("Guardando URL para después de selección de perfil: {}", currentUrl);
      }
      
      response.sendRedirect(request.getContextPath() + "/mi-cuenta/perfiles/seleccionar");
      return false;
    }

    log.debug("Perfil activo encontrado (ID: {}), permitiendo acceso a: {}", perfilActivoId, requestPath);
    return true;
  }

  private boolean isAuthOrProfileRelatedUrl(String path) {
    return path.equals("/") || 
           path.equals("/login") || 
           path.equals("/registro") || 
           path.startsWith("/mi-cuenta/perfiles/") ||
           path.equals("/logout");
  }
}