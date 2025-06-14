package com.biblioteca.aspects;

import com.biblioteca.annotations.RequiereRecurso;
import com.biblioteca.middleware.PlanAuthorizationMiddleware;
import com.biblioteca.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RecursoAuthorizationAspect {

  private final PlanAuthorizationMiddleware authorizationMiddleware;
  private final UsuarioService usuarioService;

  @Around("@annotation(requiereRecurso)")
  public Object verificarAccesoRecurso(ProceedingJoinPoint joinPoint, RequiereRecurso requiereRecurso)
      throws Throwable {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !auth.isAuthenticated()) {
      throw new AccessDeniedException("Usuario no autenticado");
    }

    String username = auth.getName();
    Long usuarioId = usuarioService.buscarPorUsername(username)
        .map(usuario -> usuario.getId())
        .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));

    String[] recursosRequeridos = requiereRecurso.value();
    boolean requireAll = requiereRecurso.requireAll();

    boolean tieneAcceso = requireAll
        ? authorizationMiddleware.puedeAccederRecursos(usuarioId, recursosRequeridos)
        : java.util.Arrays.stream(recursosRequeridos)
            .anyMatch(recurso -> authorizationMiddleware.puedeAccederRecurso(usuarioId, recurso));

    if (!tieneAcceso) {
      log.warn("Acceso denegado para usuario {} a recursos: {}", username,
          java.util.Arrays.toString(recursosRequeridos));
      throw new AccessDeniedException("No tiene permisos para acceder a este recurso");
    }

    return joinPoint.proceed();
  }
}