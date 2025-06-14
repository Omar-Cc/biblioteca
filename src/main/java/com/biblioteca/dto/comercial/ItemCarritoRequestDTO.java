package com.biblioteca.dto.comercial;

import jakarta.validation.constraints.Max;
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
    
    // ✅ AGREGAR: Validaciones más específicas según mejoras sugeridas
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Max(value = 10, message = "Máximo 10 unidades por item")
    private Integer cantidad;
    
    // ✅ AGREGAR: Contexto de la acción
    @Builder.Default
    private String accion = "AGREGAR"; // AGREGAR, ACTUALIZAR, REMOVER
    
    private String origen; // WEB, MOBILE, API
    
    // ✅ AGREGAR: Información opcional
    private String notasEspeciales;
    
    @Builder.Default
    private Boolean esRegalo = false;
}