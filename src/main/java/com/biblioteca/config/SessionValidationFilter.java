package com.biblioteca.config;

import com.biblioteca.service.SessionManagementService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SessionValidationFilter extends OncePerRequestFilter {

  private final SessionManagementService sessionManagementService;

  public SessionValidationFilter(SessionManagementService sessionManagementService) {
    this.sessionManagementService = sessionManagementService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);

    if (session != null) {
      String sessionId = session.getId();

      // Verificar si esta sesión ha sido revocada
      if (sessionManagementService.isSessionRevoked(sessionId)) {
        // Invalidar la sesión HTTP
        session.invalidate();

        // Eliminar de la lista de revocadas
        sessionManagementService.purgeRevokedSession(sessionId);

        // Redirigir al login con mensaje
        response.sendRedirect(request.getContextPath() + "/login?logout=true&session=expired");
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}