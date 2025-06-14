package com.biblioteca.service.impl;

import com.biblioteca.dto.comercial.SuscripcionMetricasDTO;
import com.biblioteca.repositories.UsuarioRepository;
import com.biblioteca.repositories.comercial.SuscripcionRepository;
import com.biblioteca.service.MetricasSuscripcionService;
import com.biblioteca.service.UsuarioService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MetricasSuscripcionServiceImpl implements MetricasSuscripcionService {

  private final SuscripcionRepository suscripcionRepository;
  private final UsuarioRepository usuarioRepository;
  private final UsuarioService usuarioService;

  @Override
  @Transactional(readOnly = true)
  public SuscripcionMetricasDTO obtenerMetricas() {
    return SuscripcionMetricasDTO.builder()
        .totalSuscripcionesActivas(contarSuscripcionesActivas())
        .ingresosMensual(calcularIngresosMensual())
        .tasaConversionPrueba(calcularTasaConversionPrueba())
        .planMasPopular(obtenerPlanMasPopular())
        .churnRate(calcularChurnRate())
        .totalUsuarios(usuarioRepository.count())
        .usuariosConSuscripcionActiva(contarUsuariosConSuscripcionActiva())
        .build();
  }

  @Override
  public long contarSuscripcionesActivas() {
    return suscripcionRepository.countByEstado("ACTIVA");
  }

  @Override
  public double calcularIngresosMensual() {
    return suscripcionRepository.findByEstado("ACTIVA").stream()
        .mapToDouble(s -> s.getPlan().getPrecioMensual() / 100.0)
        .sum();
  }

  @Override
  public double calcularTasaConversionPrueba() {
    long totalPruebas = suscripcionRepository.countByEstado("PRUEBA");
    long conversionesExitosas = suscripcionRepository
        .countSuscripcionesPruebaConvertidas(); // Query personalizada

    return totalPruebas > 0 ? (conversionesExitosas * 100.0) / totalPruebas : 0.0;
  }

  @Override
  public String obtenerPlanMasPopular() {
    return suscripcionRepository.findPlanMasPopular()
        .orElse("Sin datos");
  }

  @Override
  public double calcularChurnRate() {
    // Implementar lógica de churn rate
    LocalDate fechaInicioMes = LocalDate.now().withDayOfMonth(1);
    long suscripcionesInicioMes = suscripcionRepository
        .countSuscripcionesActivasEnFecha(fechaInicioMes);
    long suscripcionesCanceladas = suscripcionRepository
        .countSuscripcionesCanceladasEnMes(fechaInicioMes, LocalDate.now());

    return suscripcionesInicioMes > 0 ? (suscripcionesCanceladas * 100.0) / suscripcionesInicioMes : 0.0;
  }

  private long contarUsuariosConSuscripcionActiva() {
    return usuarioService.contarUsuariosConSuscripcionActiva();
  }
}
