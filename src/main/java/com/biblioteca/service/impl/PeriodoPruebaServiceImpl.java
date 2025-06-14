package com.biblioteca.service.impl;

import com.biblioteca.service.PeriodoPruebaService;
import com.biblioteca.service.UsuarioService;
import com.biblioteca.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PeriodoPruebaServiceImpl implements PeriodoPruebaService {

  private final UsuarioService usuarioService;
  private final PlanService planService;

  @Override
  @Transactional(readOnly = true)
  public boolean puedeUsarPeriodoPrueba(Long usuarioId, Long planId) {
    // Verificar que el plan tenga período de prueba
    var planOpt = planService.obtenerPlanPorId(planId);
    if (planOpt.isEmpty()) {
      return false;
    }

    var plan = planOpt.get();

    // Plan gratuito no tiene período de prueba
    if (plan.getPrecioMensual() == 0) {
      return false;
    }

    // Plan sin días de prueba
    if (plan.getDiasPrueba() == 0) {
      return false;
    }

    // Verificar si el usuario ya usó su período de prueba
    return !haUsadoPeriodoPrueba(usuarioId);
  }

  @Override
  @Transactional
  public void marcarPeriodoPruebaUsado(Long usuarioId) {
    usuarioService.buscarPorId(usuarioId).ifPresent(usuario -> {
      if (!usuario.getHaUsadoPeriodoPrueba()) {
        usuario.setHaUsadoPeriodoPrueba(true);
        usuario.setFechaPrimerPeriodoPrueba(LocalDateTime.now());
        usuarioService.actualizarUsuario(usuario);
        log.info("Período de prueba marcado como usado para usuario {}", usuario.getUsername());
      }
    });
  }

  @Override
  @Transactional(readOnly = true)
  public boolean haUsadoPeriodoPrueba(Long usuarioId) {
    return usuarioService.buscarPorId(usuarioId)
        .map(usuario -> usuario.getHaUsadoPeriodoPrueba())
        .orElse(false);
  }
}