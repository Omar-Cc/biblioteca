package com.biblioteca.config;

import com.biblioteca.service.SessionManagementService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

  private final SessionManagementService sessionManagementService;

  public CustomLogoutSuccessHandler(SessionManagementService sessionManagementService) {
    this.sessionManagementService = sessionManagementService;
    setDefaultTargetUrl("/login?logout=true");
  }

  @Override
  public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    if (authentication != null) {
      String username = authentication.getName();
      HttpSession session = request.getSession(false);

      if (session != null) {
        // Eliminar la sesión explícitamente desde aquí también
        sessionManagementService.invalidateSession(username, session.getId());
      }
    }

    super.onLogoutSuccess(request, response, authentication);
  }
}