package com.biblioteca.dto;

import com.biblioteca.enums.FormatoFisico;
import com.biblioteca.enums.TipoContenido;
import com.biblioteca.enums.TipoLicenciaDigital;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ContenidoResponseDTO {
  // Campos de Contenido
  private Long id;
  private ObraResponseDTO obra;
  private EditorialResponseDTO editorial;
  private String portadaUrl;
  private String sinopsis;
  private Integer precio;
  private boolean enVenta;
  private TipoContenido tipo;
  private boolean puedeSerPrestado;
  private LocalDate fechaCreacion;
  private String isbn; // Para libros
  private boolean activo;

  // Campos de ContenidoFisico
  private Integer stockDisponible;
  private Integer minStock;
  private String ubicacionFisica;
  private FormatoFisico formatoFisico;

  // Campos de ContenidoDigital
  private BigDecimal tamanioArchivo;
  private String formatoDigital; // e.g., PDF, EPUB
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
  private String issn; // Para publicaciones periódicas

  // Campos de AudioLibro
  private String duracionAudioLibro; // e.g., "PT2H30M" o Long en segundos/milisegundos
  private String narrador;
  private String calidadAudio;

  // Campos de MaterialEducativo
  private String nivelEducativo;
  private String asignatura;
  private String recursosEducativos;

  // Campos de ContenidoMultimedia
  private String duracionMultimedia; // e.g., "PT1H45M" o Long
  private String calidadMultimedia;
  private String requisitosReproduccion;

  // Campos de Series
  private SerieResponseDTO serie;
  
}