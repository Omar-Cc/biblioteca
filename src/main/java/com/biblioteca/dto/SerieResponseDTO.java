package com.biblioteca.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SerieResponseDTO {
  private Long id;
  private String nombre;
  private String descripcion;
  private int numeroVolumenes;
  private boolean completa;
  private Integer ordenEnSerie; // Posición del contenido dentro de la serie
}