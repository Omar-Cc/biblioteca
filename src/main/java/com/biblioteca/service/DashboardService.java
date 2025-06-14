package com.biblioteca.service;

import com.biblioteca.dto.dashboard.DashboardEstadisticasDTO;

public interface DashboardService {

  /**
   * Obtiene todas las estadísticas del dashboard de forma completa
   */
  DashboardEstadisticasDTO obtenerDashboardCompleto();

  /**
   * Obtiene las estadísticas del dashboard con un período específico para la
   * tendencia
   */
  DashboardEstadisticasDTO obtenerDashboardConPeriodo(String periodo);

  /**
   * Refresca todos los datos del dashboard
   */
  DashboardEstadisticasDTO refrescarDashboard(String periodo);

  /**
   * Limpia la caché del dashboard
   */
  void limpiarCacheDashboard();
}