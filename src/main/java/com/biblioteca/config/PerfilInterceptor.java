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
import java.util.regex.Pattern;

@Slf4j
@Component
public class PerfilInterceptor implements HandlerInterceptor {

  // Rutas que no necesitan verificación de perfil
  private static final List<String> EXCLUDED_PATHS = Arrays.asList(
      "/login", "/logout", "/registro",
      "/css/", "/js/", "/images/", "/webjars/",
      "/error", "/403", "/404", "/500",
      "/mi-cuenta/perfiles/seleccionar", "/mi-cuenta/perfiles/nuevo", "/mi-cuenta/perfiles/crear");

  // Patrones para rutas excluidas usando expresiones regulares
  private static final List<Pattern> EXCLUDED_PATTERNS = Arrays.asList(
    // Coincide con /mi-cuenta/perfiles/1/activar, /mi-cuenta/perfiles/2/activar, etc.
      Pattern.compile("/mi-cuenta/perfiles/\\d+/activar"), 
      Pattern.compile("/.well-known/.*"), // Excluir todas las rutas de Chrome DevTools
      Pattern.compile("/favicon\\.ico") // Excluir favicon
  );

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String requestPath = request.getRequestURI();

    // DEBUG: imprime la ruta para verificar
    log.debug("Path: {}", requestPath);

    // Verificar rutas excluidas específicas
    for (String path : EXCLUDED_PATHS) {
      if (requestPath.startsWith(path)) {
        log.debug("Ruta excluida por coincidencia exacta: {}", path);
        return true;
      }
    }

    // Verificar patrones excluidos (comodines)
    for (Pattern pattern : EXCLUDED_PATTERNS) {
      if (pattern.matcher(requestPath).matches()) {
        log.debug("Ruta excluida por patrón regex: {}", pattern.pattern());
        return true;
      }
    }

    // Verificar parámetro perfilActivado
    String perfilActivado = request.getParameter("perfilActivado");
    log.debug("Parámetro perfilActivado: {}", perfilActivado);

    if ("true".equals(perfilActivado) && requestPath.equals("/")) {
      log.debug("Permitiendo acceso con perfilActivado=true");
      return true;
    }

    // Verificar si el usuario está autenticado mediante Spring Security
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
      return true;
    }

    // Verificar si hay un perfil activo
    HttpSession session = request.getSession(false);
    if (session == null) {
      log.warn("Sesión no encontrada, redirigiendo a login");
      response.sendRedirect(request.getContextPath() + "/login");
      return false;
    }

    // Verificar bandera especial de perfil recién activado
    if (session.getAttribute("perfilRecienActivado") != null) {
      log.info("Permitiendo acceso porque se acaba de activar un perfil");
      session.removeAttribute("perfilRecienActivado");
      return true;
    }

    if (session.getAttribute("perfilActivoId") == null) {
      log.warn("Perfil no encontrado en sesión");
      log.warn("Perfil no encontrado en la sesión, redirigiendo a selección de perfil");
      response.sendRedirect(request.getContextPath() + "/mi-cuenta/perfiles/seleccionar");
      return false;
    }

    return true;
  }
}