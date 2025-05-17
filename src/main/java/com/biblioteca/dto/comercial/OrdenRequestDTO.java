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
public class OrdenRequestDTO {
    @NotNull(message = "El ID del perfil es obligatorio")
    private Long perfilId;
    
    private Long carritoId; // Puede ser null si se crea una orden directamente
}