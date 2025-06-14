package com.biblioteca.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import com.biblioteca.dto.dashboard.ActividadRecienteDTO;
import com.biblioteca.dto.dashboard.DashboardEstadisticasDTO;
import com.biblioteca.dto.dashboard.GeneroPopularidadDTO;
import com.biblioteca.dto.dashboard.LibroPopularDTO;
import com.biblioteca.dto.dashboard.TendenciaPrestamoDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class DashboardRepository {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Obtiene las estadísticas generales del dashboard usando procedimiento
   * almacenado
   */
  public DashboardEstadisticasDTO obtenerEstadisticasGenerales() {
    try {
      StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_dashboard_estadisticas_generales");

      // Registrar parámetros de salida - CAMBIAR A BigInteger
      query.registerStoredProcedureParameter("total_usuarios", BigInteger.class, ParameterMode.OUT);
      query.registerStoredProcedureParameter("nuevos_usuarios_mes", BigInteger.class, ParameterMode.OUT);
      query.registerStoredProcedureParameter("total_obras", BigInteger.class, ParameterMode.OUT);
      query.registerStoredProcedureParameter("nuevas_obras_mes", BigInteger.class, ParameterMode.OUT);
      query.registerStoredProcedureParameter("total_prestamos_activos", BigInteger.class, ParameterMode.OUT);
      query.registerStoredProcedureParameter("nuevos_prestamos_mes", BigInteger.class, ParameterMode.OUT);
      query.registerStoredProcedureParameter("total_generos", BigInteger.class, ParameterMode.OUT);

      query.execute();

      // MÉTODO AUXILIAR PARA CONVERSIÓN SEGURA
      return DashboardEstadisticasDTO.builder()
          .totalUsuarios(convertirBigIntegerALong(query.getOutputParameterValue("total_usuarios")))
          .nuevosUsuariosMes(convertirBigIntegerALong(query.getOutputParameterValue("nuevos_usuarios_mes")))
          .totalObras(convertirBigIntegerALong(query.getOutputParameterValue("total_obras")))
          .nuevasObrasMes(convertirBigIntegerALong(query.getOutputParameterValue("nuevas_obras_mes")))
          .totalPrestamos(convertirBigIntegerALong(query.getOutputParameterValue("total_prestamos_activos")))
          .nuevosPrestamos(convertirBigIntegerALong(query.getOutputParameterValue("nuevos_prestamos_mes")))
          .totalGeneros(convertirBigIntegerALong(query.getOutputParameterValue("total_generos")))
          .fechaActualizacion(LocalDateTime.now().toString())
          .build();

    } catch (Exception e) {
      log.error("Error obteniendo estadísticas generales: {}", e.getMessage());
      return DashboardEstadisticasDTO.builder()
          .totalUsuarios(0L)
          .nuevosUsuariosMes(0L)
          .totalObras(0L)
          .nuevasObrasMes(0L)
          .totalPrestamos(0L)
          .nuevosPrestamos(0L)
          .totalGeneros(0L)
          .fechaActualizacion(LocalDateTime.now().toString())
          .build();
    }
  }

  /**
   * Obtiene la tendencia de préstamos por período
   */
  @SuppressWarnings("unchecked")
  public List<TendenciaPrestamoDTO> obtenerTendenciaPrestamos(String periodo) {
    try {
      StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_dashboard_tendencia_prestamos");
      query.registerStoredProcedureParameter("periodo", String.class, ParameterMode.IN);
      query.setParameter("periodo", periodo);

      query.execute();
      List<Object[]> results = query.getResultList();

      List<TendenciaPrestamoDTO> tendencias = new ArrayList<>();
      for (Object[] row : results) {
        tendencias.add(TendenciaPrestamoDTO.builder()
            .mes((String) row[0])
            .nombreMes((String) row[1])
            .anio(convertirAInteger(row[2]))
            .numeroMes(convertirAInteger(row[3]))
            .totalPrestamos(convertirALong(row[4]))
            .etiqueta(obtenerEtiquetaMes((String) row[1], convertirAInteger(row[2])))
            .build());
      }

      return tendencias;
    } catch (Exception e) {
      log.error("Error obteniendo tendencia de préstamos: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  /**
   * Obtiene la distribución de obras por género
   */
  @SuppressWarnings("unchecked")
  public List<GeneroPopularidadDTO> obtenerObrasPorGenero() {
    try {
      StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_dashboard_obras_por_genero");
      query.execute();

      List<Object[]> results = query.getResultList();
      List<GeneroPopularidadDTO> generos = new ArrayList<>();

      String[] colores = { "#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF", "#FF9F40", "#FF6384", "#36A2EB",
          "#FFCE56", "#4BC0C0" };

      for (int i = 0; i < results.size(); i++) {
        Object[] row = results.get(i);
        generos.add(GeneroPopularidadDTO.builder()
            .genero((String) row[0])
            .totalObras(convertirALong(row[1]))
            .totalPrestamos(convertirALong(row[2]))
            .popularidadPorcentaje(convertirADouble(row[3]))
            .color(colores[i % colores.length])
            .build());
      }

      return generos;
    } catch (Exception e) {
      log.error("Error obteniendo obras por género: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  /**
   * Obtiene los libros más populares
   */
  @SuppressWarnings("unchecked")
  public List<LibroPopularDTO> obtenerLibrosPopulares(int limite) {
    try {
      StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_dashboard_libros_populares");
      query.registerStoredProcedureParameter("limite", Integer.class, ParameterMode.IN);
      query.setParameter("limite", limite <= 0 ? 10 : limite);

      query.execute();
      List<Object[]> results = query.getResultList();

      List<LibroPopularDTO> libros = new ArrayList<>();
      for (int i = 0; i < results.size(); i++) {
        Object[] row = results.get(i);
        String badge = i == 0 ? "Top 1"
            : i < 3 ? "Top " + (i + 1) : convertirALong(row[6]) > 5 ? "Trending" : "";

        libros.add(LibroPopularDTO.builder()
            .contenidoId(convertirALong(row[0]))
            .titulo((String) row[1])
            .autores((String) row[2])
            .totalPrestamos(convertirALong(row[3]))
            .prestamosActivos(convertirALong(row[4]))
            .prestamosMes(convertirALong(row[5]))
            .portadaUrl((String) row[6])
            .precio(convertirAInteger(row[7]))
            .anioPublicacion(convertirAInteger(row[8]))
            .badge(badge)
            .build());
      }

      return libros;
    } catch (Exception e) {
      log.error("Error obteniendo libros populares: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  /**
   * Obtiene las actividades recientes
   */
  @SuppressWarnings("unchecked")
  public List<ActividadRecienteDTO> obtenerActividadesRecientes(int limite) {
    try {
      StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_dashboard_actividades_recientes");
      query.registerStoredProcedureParameter("limite", Integer.class, ParameterMode.IN);
      query.setParameter("limite", limite <= 0 ? 5 : limite);

      query.execute();
      List<Object[]> results = query.getResultList();

      List<ActividadRecienteDTO> actividades = new ArrayList<>();
      for (Object[] row : results) {
        LocalDateTime fechaActividad = ((Timestamp) row[3]).toLocalDateTime();

        actividades.add(ActividadRecienteDTO.builder()
            .tipoActividad((String) row[0])
            .descripcion((String) row[1])
            .usuario((String) row[2])
            .fechaActividad(fechaActividad)
            .categoria((String) row[4])
            .imagen((String) row[5])
            .tiempoRelativo(calcularTiempoRelativo(fechaActividad))
            .build());
      }

      return actividades;
    } catch (Exception e) {
      log.error("Error obteniendo actividades recientes: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  // MÉTODOS AUXILIARES PARA CONVERSIÓN SEGURA DE TIPOS

  private Long convertirBigIntegerALong(Object value) {
    if (value == null)
      return 0L;
    if (value instanceof BigInteger)
      return ((BigInteger) value).longValue();
    if (value instanceof Long)
      return (Long) value;
    if (value instanceof Integer)
      return ((Integer) value).longValue();
    if (value instanceof Number)
      return ((Number) value).longValue();
    return 0L;
  }

  private Long convertirALong(Object value) {
    if (value == null)
      return 0L;
    if (value instanceof BigInteger)
      return ((BigInteger) value).longValue();
    if (value instanceof Long)
      return (Long) value;
    if (value instanceof Integer)
      return ((Integer) value).longValue();
    if (value instanceof Number)
      return ((Number) value).longValue();
    return 0L;
  }

  private Integer convertirAInteger(Object value) {
    if (value == null)
      return 0;
    if (value instanceof BigInteger)
      return ((BigInteger) value).intValue();
    if (value instanceof Integer)
      return (Integer) value;
    if (value instanceof Long)
      return ((Long) value).intValue();
    if (value instanceof Number)
      return ((Number) value).intValue();
    return 0;
  }

  private Double convertirADouble(Object value) {
    if (value == null)
      return 0.0;
    if (value instanceof BigDecimal)
      return ((BigDecimal) value).doubleValue();
    if (value instanceof Double)
      return (Double) value;
    if (value instanceof Float)
      return ((Float) value).doubleValue();
    if (value instanceof Number)
      return ((Number) value).doubleValue();
    return 0.0;
  }

  // Métodos auxiliares existentes
  private String obtenerEtiquetaMes(String nombreMes, int anio) {
    if (nombreMes == null || nombreMes.length() < 3)
      return "N/A";
    return nombreMes.substring(0, 3) + " " + anio;
  }

  private String calcularTiempoRelativo(LocalDateTime fecha) {
    LocalDateTime ahora = LocalDateTime.now();
    long minutos = java.time.Duration.between(fecha, ahora).toMinutes();

    if (minutos < 60) {
      return "hace " + minutos + " min";
    } else if (minutos < 1440) { // 24 horas
      return "hace " + (minutos / 60) + " h";
    } else {
      return "hace " + (minutos / 1440) + " días";
    }
  }
}