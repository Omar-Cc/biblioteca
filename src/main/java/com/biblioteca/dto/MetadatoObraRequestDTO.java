package com.biblioteca.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MetadatoObraRequestDTO {
  private String clave;
  private String valor;
  private String categoria;
}