package com.biblioteca.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UsuarioDataDTO {
    @NotEmpty(message = "El correo electrónico no puede estar vacío")
    @Email(message = "Debe ser una dirección de correo electrónico válida")
    private String email;
}