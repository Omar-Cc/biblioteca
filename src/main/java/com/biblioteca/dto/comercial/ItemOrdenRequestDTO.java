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
public class ItemOrdenRequestDTO {
    @NotNull(message = "El ID de la orden es obligatorio")
    private Long ordenId;
    
    @NotNull(message = "El ID del contenido es obligatorio")
    private Long contenidoId;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;
    
    @NotNull(message = "El precio al comprar es obligatorio")
    @Min(value = 0, message = "El precio debe ser mayor o igual a 0")
    private Integer precioAlComprar;
    
    @Min(value = 0, message = "El descuento debe ser mayor o igual a 0")
    private Integer descuentoAplicado;
}