package com.biblioteca.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

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
  private LocalDate fechaCreacion;
  private LocalDate fechaActualizacion;
  private List<AutorResponseDTO> autores;
  private List<GeneroResponseDTO> generos;
}