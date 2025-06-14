package com.biblioteca.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InformacionPerfilResponseDTO {

  // ========== INFORMACIÓN PERSONAL ==========
  private String biografia;
  private String generosPreferidos;
  private String autoresFavoritos;
  private String temasInteres;

  // ========== NIVEL Y PROGRESO ==========
  private String nivelLectura;
  private String metasPersonales;
  private Integer metaLibrosMes;
  private Integer metaLibrosAnio;

  // ========== CONFIGURACIÓN DE EXPERIENCIA ==========
  private String formatoPreferido;
  private String idiomaLectura;
  private Boolean mostrarProgreso;
  private Boolean aceptarInvitacionesGrupos;

  // ========== ESTADÍSTICAS ACUMULADAS ==========
  private Integer totalLibrosLeidos;
  private Integer totalPrestamoRealizados;
  private Integer totalResenasEscritas;
  private Integer totalRecomendacionesHechas;
  private Integer puntuacionComunidad;
  private Integer tiempoLecturaMinutos;
  private Integer horasLecturaTotal;

  // ========== ESTADÍSTICAS PERIÓDICAS ==========
  private Integer librosLeidosMesActual;
  private Integer librosLeidosAnioActual;
  private Double progresoMetaAnual;
  private Double progresoMetaMensual;

  // ========== CONFIGURACIÓN DE RECOMENDACIONES ==========
  private Boolean algoritmoRecomendacionesActivo;
  private String tipoRecomendaciones;

  // ========== FECHAS ==========
  private String fechaCreacion;
  private String ultimaActualizacion;
  private String ultimaActividad;
  private String fechaUltimoReset;
}