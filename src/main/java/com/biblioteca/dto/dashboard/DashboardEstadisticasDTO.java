package com.biblioteca.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardEstadisticasDTO {
  // Estadísticas generales
  private Long totalUsuarios;
  private Long nuevosUsuariosMes;
  private Long totalObras;
  private Long nuevasObrasMes;
  private Long totalPrestamos;
  private Long nuevosPrestamos;
  private Long totalGeneros;

  // Datos para gráficos
  private List<TendenciaPrestamoDTO> tendenciaPrestamos;
  private List<GeneroPopularidadDTO> obrasPorGenero;
  private List<LibroPopularDTO> librosPopulares;
  private List<ActividadRecienteDTO> actividadesRecientes;
  private Map<String, EstadisticaEstadoDTO> estadisticasPorEstado;

  private List<Integer> prestamosPorMes;
  private Map<String, Integer> obrasPorGeneroJS;

  // Metadatos
  private String fechaActualizacion;
  private String periodoTendencia;

  private List<ActividadRecienteDTO> actividades;
  private List<ObraPopularDTO> obrasPopulares;

  // MÉTODO PARA SINCRONIZAR DATOS
  public void sincronizarDatos() {
    this.actividades = this.actividadesRecientes;

    // Convertir LibroPopularDTO a ObraPopularDTO
    if (this.librosPopulares != null) {
      this.obrasPopulares = this.librosPopulares.stream()
          .map(libro -> ObraPopularDTO.builder()
              .id(libro.getContenidoId())
              .titulo(libro.getTitulo())
              .autor(libro.getAutores())
              .prestamos(libro.getTotalPrestamos())
              .portadaUrl(libro.getPortadaUrl())
              .badge(libro.getBadge())
              .build())
          .toList();
    }

    // Validaciones adicionales para evitar errores en el template
    if (this.actividades == null) {
      this.actividades = new ArrayList<>();
    }
    if (this.obrasPopulares == null) {
      this.obrasPopulares = new ArrayList<>();
    }
  }
}