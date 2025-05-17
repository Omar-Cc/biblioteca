package com.biblioteca.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ObraRequestDTO {
  private String titulo;
  private String descripcion;
  private int anioPublicacion;
  private String isbn;
  private Long editorialId;
  private List<String> idioma;
  private List<Long> autorIds;
  private List<String> autorRoles;
  private List<Long> generoIds;
}