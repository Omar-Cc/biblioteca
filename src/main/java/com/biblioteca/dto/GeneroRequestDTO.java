package com.biblioteca.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GeneroRequestDTO {
  private String nombre;
  private String descripcion;
  private Long parentId;
  private Integer nivel;
}