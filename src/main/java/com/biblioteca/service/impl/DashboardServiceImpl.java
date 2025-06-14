package com.biblioteca.service.impl;

import com.biblioteca.dto.dashboard.DashboardEstadisticasDTO;
import com.biblioteca.dto.dashboard.GeneroPopularidadDTO;
import com.biblioteca.repositories.DashboardRepository;
import com.biblioteca.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
// Remover @Transactional(readOnly = true) de la clase
public class DashboardServiceImpl implements DashboardService {

  private final DashboardRepository dashboardRepository;

  @Override
  @Cacheable(value = "dashboard-completo", key = "'dashboard-default'")
  @Transactional(readOnly = false) // Permitir escritura para procedimientos almacenados
  public DashboardEstadisticasDTO obtenerDashboardCompleto() {
    return obtenerDashboardConPeriodo("12M");
  }

  @Override
  @Cacheable(value = "dashboard-periodo", key = "#periodo")
  @Transactional(readOnly = false)
  public DashboardEstadisticasDTO obtenerDashboardConPeriodo(String periodo) {
    log.debug("Generando dashboard para período: {}", periodo);

    try {
      // Obtener estadísticas base
      DashboardEstadisticasDTO estadisticas = dashboardRepository.obtenerEstadisticasGenerales();

      // Completar con datos específicos del período
      estadisticas.setTendenciaPrestamos(dashboardRepository.obtenerTendenciaPrestamos(periodo));
      estadisticas.setObrasPorGenero(dashboardRepository.obtenerObrasPorGenero());
      estadisticas.setLibrosPopulares(dashboardRepository.obtenerLibrosPopulares(10));
      estadisticas.setActividadesRecientes(dashboardRepository.obtenerActividadesRecientes(5));
      estadisticas.setPeriodoTendencia(periodo);
      estadisticas.setFechaActualizacion(LocalDateTime.now().toString());

      // GENERAR DATOS PARA JAVASCRIPT
      generarDatosParaJS(estadisticas);

      // SINCRONIZAR DATOS PARA EL TEMPLATE
      estadisticas.sincronizarDatos();

      log.info("Dashboard generado exitosamente para período: {}", periodo);
      return estadisticas;

    } catch (Exception e) {
      log.error("Error generando dashboard para período {}: {}", periodo, e.getMessage());
      DashboardEstadisticasDTO estadisticasVacias = crearDashboardVacio(periodo);
      estadisticasVacias.sincronizarDatos();
      return estadisticasVacias;
    }
  }

  private void generarDatosParaJS(DashboardEstadisticasDTO estadisticas) {
    try {
      // Generar datos para gráfico de préstamos por mes
      if (estadisticas.getTendenciaPrestamos() != null) {
        List<Integer> prestamosPorMes = estadisticas.getTendenciaPrestamos()
            .stream()
            .map(t -> t.getTotalPrestamos() != null ? t.getTotalPrestamos().intValue() : 0)
            .collect(Collectors.toList());
        estadisticas.setPrestamosPorMes(prestamosPorMes);
      }

      // Generar datos para gráfico de géneros
      if (estadisticas.getObrasPorGenero() != null) {
        Map<String, Integer> obrasPorGeneroJS = estadisticas.getObrasPorGenero()
            .stream()
            .collect(Collectors.toMap(
                GeneroPopularidadDTO::getGenero,
                g -> g.getTotalObras() != null ? g.getTotalObras().intValue() : 0));
        estadisticas.setObrasPorGeneroJS(obrasPorGeneroJS);
      }

      log.debug("Datos para JS generados: préstamos={}, géneros={}",
          estadisticas.getPrestamosPorMes(),
          estadisticas.getObrasPorGeneroJS());

    } catch (Exception e) {
      log.error("Error generando datos para JS: {}", e.getMessage());
    }
  }

  @Override
  @CacheEvict(value = { "dashboard-completo", "dashboard-periodo" }, allEntries = true)
  public DashboardEstadisticasDTO refrescarDashboard(String periodo) {
    log.info("Refrescando dashboard para período: {}", periodo);
    limpiarCacheDashboard();
    return obtenerDashboardConPeriodo(periodo);
  }

  @Override
  @CacheEvict(value = { "dashboard-completo", "dashboard-periodo" }, allEntries = true)
  public void limpiarCacheDashboard() {
    log.info("Limpiando caché del dashboard");
  }

  private DashboardEstadisticasDTO crearDashboardVacio(String periodo) {
    return DashboardEstadisticasDTO.builder()
        .totalUsuarios(0L)
        .nuevosUsuariosMes(0L)
        .totalObras(0L)
        .nuevasObrasMes(0L)
        .totalPrestamos(0L)
        .nuevosPrestamos(0L)
        .totalGeneros(0L)
        .actividadesRecientes(new ArrayList<>())
        .librosPopulares(new ArrayList<>())
        .obrasPorGenero(new ArrayList<>())
        .tendenciaPrestamos(new ArrayList<>())
        .periodoTendencia(periodo)
        .fechaActualizacion(LocalDateTime.now().toString())
        .build();
  }
}