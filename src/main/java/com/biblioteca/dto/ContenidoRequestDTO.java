package com.biblioteca.dto;

import com.biblioteca.enums.FormatoFisico;
import com.biblioteca.enums.TipoContenido;
import com.biblioteca.enums.TipoLicenciaDigital;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ContenidoRequestDTO {
  // Campos de Contenido
  @NotNull
  private Long obraId;
  @NotNull
  private Long editorialId;
  private String portadaUrl;
  private String sinopsis;
  private Integer precio;
  private boolean enVenta;
  @NotNull
  private TipoContenido tipo; // Discriminador
  private boolean puedeSerPrestado;
  private String isbn;

  // Campos de ContenidoFisico
  private Integer stockDisponible;
  private Integer minStock;
  private String ubicacionFisica;
  private FormatoFisico formatoFisico;

  // Campos de ContenidoDigital
  private BigDecimal tamanioArchivo;
  private String formatoDigital;
  private Boolean permiteDescarga;
  private TipoLicenciaDigital tipoLicencia;
  private Integer licencias;

  // Campos de LibroFisico
  private Integer paginas;

  // Campos de PublicacionIlustradaFisica (Manga, Comic)
  private String ilustrador;
  private Integer volumen;

  // Campos de Manga
  private String sentidoLectura;

  // Campos de Comic
  private Boolean colorido;

  // Campos de RevistaPeriodica
  private String periodicidad;
  private String edicion;
  private String issn;

  // Campos de AudioLibro
  private String duracionAudioLibro;
  private String narrador;
  private String calidadAudio;

  // Campos de MaterialEducativo
  private String nivelEducativo;
  private String asignatura;
  private String recursosEducativos;

  // Campos de ContenidoMultimedia
  private String duracionMultimedia;
  private String calidadMultimedia;
  private String requisitosReproduccion;

  private SerieResponseDTO serie;
  private Integer ordenEnSerie;
}