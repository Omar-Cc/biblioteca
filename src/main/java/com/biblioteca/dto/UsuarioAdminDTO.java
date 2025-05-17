package com.biblioteca.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UsuarioAdminDTO {
  @NotEmpty(message = "El nombre de usuario no puede estar vacío.")
  @Size(min = 3, max = 20, message = "El nombre de usuario debe tener entre 3 y 20 caracteres.")
  private String username;

  @NotEmpty(message = "El correo electrónico no puede estar vacío.")
  @Email(message = "Debe ser una dirección de correo electrónico válida.")
  private String email;

  @NotEmpty(message = "La contraseña no puede estar vacía.")
  @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
  private String password;

  @NotEmpty(message = "Debe seleccionar al menos un rol.")
  private List<String> roles;
}