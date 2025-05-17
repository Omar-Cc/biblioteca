package com.biblioteca.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioPasswordDTO {
  @NotEmpty(message = "La contraseña actual no puede estar vacía")
  private String passwordActual;

  @NotEmpty(message = "La nueva contraseña no puede estar vacía")
  @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
  private String nuevaPassword;

  @NotEmpty(message = "Debe confirmar la nueva contraseña")
  private String confirmPassword;
}