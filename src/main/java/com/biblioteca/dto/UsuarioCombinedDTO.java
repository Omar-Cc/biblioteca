package com.biblioteca.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UsuarioCombinedDTO {

  @NotEmpty(message = "El correo electrónico es obligatorio")
  @Email(message = "El formato del correo electrónico no es válido")
  private String email;

  @Valid
  private LectorRequestDTO lectorDTO;
  
}