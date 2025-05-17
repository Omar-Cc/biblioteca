package com.biblioteca.dto.comercial;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarritoRequestDTO {
    @NotNull(message = "El ID del carrito es obligatorio")
    private Long carritoId;
    
    @NotNull(message = "El ID del contenido es obligatorio")
    private Long contenidoId;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;
}