package com.biblioteca.service;

import com.biblioteca.dto.comercial.SuscripcionMetricasDTO;

public interface MetricasSuscripcionService {

  SuscripcionMetricasDTO obtenerMetricas();

  long contarSuscripcionesActivas();

  double calcularIngresosMensual();

  double calcularTasaConversionPrueba();

  String obtenerPlanMasPopular();

  double calcularChurnRate();
}