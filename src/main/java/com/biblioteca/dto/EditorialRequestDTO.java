package com.biblioteca.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EditorialRequestDTO {
  private String nombre;
  private String pais;
  private String website;
}