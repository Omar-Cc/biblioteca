package com.biblioteca.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class AutorResponseDTO {
  private Long id;
  private String nombre;
  private String biografia;
  private LocalDate fechaNacimiento;
  private String nacionalidad;
  private String foto;
}
