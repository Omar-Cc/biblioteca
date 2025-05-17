package com.biblioteca.config;

import com.biblioteca.service.SessionManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class LogoutSuccessListener implements ApplicationListener<LogoutSuccessEvent> {

  private final SessionManagementService sessionManagementService;

  public LogoutSuccessListener(SessionManagementService sessionManagementService) {
    this.sessionManagementService = sessionManagementService;
  }

  @Override
  public void onApplicationEvent(LogoutSuccessEvent event) {
    Authentication authentication = event.getAuthentication();
    if (authentication != null) {
      String username = authentication.getName();

      // Obtener la sesión actual
      HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
          .getRequest();
      HttpSession session = request.getSession(false);

      if (session != null) {
        // Eliminar solo la sesión actual
        sessionManagementService.invalidateSession(username, session.getId());
      }
    }
  }
}