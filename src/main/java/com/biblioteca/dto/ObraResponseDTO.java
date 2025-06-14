package com.biblioteca.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class ObraResponseDTO {
  private Long id;
  private String titulo;
  private String descripcion;
  private int anioPublicacion;
  private String isbn;
  private EditorialResponseDTO editorial;
  private List<String> idioma;
  private Boolean disponible;
  private String motivoEliminacion;
  private LocalDate fechaCreacion;
  private LocalDate fechaActualizacion;
  private List<AutorResponseDTO> autores;
  private List<GeneroResponseDTO> generos;
  
  // Nuevos campos para versionado
  private String version;
  private Long obraOriginalId;
  private List<ObraResponseDTO> ediciones;
  
  // Metadatos agrupados por categoría
  private Map<String, Map<String, String>> metadatos;
  
  // Relaciones con otras obras
  private List<RelacionObraResponseDTO> obrasRelacionadas;
  private List<RelacionObraResponseDTO> referenciadaPor;
}