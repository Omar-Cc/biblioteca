package com.biblioteca.dto.comercial;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoRequestDTO {
    @NotNull(message = "El ID del perfil es obligatorio")
    private Long perfilId;
}