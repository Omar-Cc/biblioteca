package com.biblioteca.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PerfilRequestDTO {
    @NotEmpty(message = "El nombre visible no puede estar vacío.")
    @Size(min = 2, max = 50, message = "El nombre visible debe tener entre 2 y 50 caracteres.")
    private String nombreVisible;

    private String fotoPerfil;

    @Size(max = 10, message = "El código de idioma no debe exceder los 10 caracteres.")
    private String idiomaPreferido; // Ej: "es-MX", "en-US"

    @NotNull(message = "Debe especificar el límite de préstamos.")
    private Integer limitePrestamosDesignado;

    @NotNull(message = "Debe indicar si el perfil es infantil.")
    private Boolean esInfantil;

    @NotNull(message = "Debe indicar si el perfil es el principal.")
    private Boolean esPerfilPrincipal;

}