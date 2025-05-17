package com.biblioteca.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EditorialResponseDTO {
  private Long id;
  private String nombre;
  private String pais;
  private String website;
}