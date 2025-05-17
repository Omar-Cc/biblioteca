package com.biblioteca.dto;

import java.util.Date;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class LectorRequestDTO {
  @NotEmpty(message = "El nombre no puede estar vacío")
  private String nombre;

  @NotEmpty(message = "El apellido no puede estar vacío")
  private String apellido;

  @NotEmpty(message = "La dirección no puede estar vacía")
  private String direccion;

  @NotEmpty(message = "El teléfono no puede estar vacío")
  private String telefono;

  @NotNull(message = "La fecha de nacimiento es obligatoria")
  @Past(message = "La fecha de nacimiento debe ser en el pasado")
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private Date fechaNacimiento;
  
}