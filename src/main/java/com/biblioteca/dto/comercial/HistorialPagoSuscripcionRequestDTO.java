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
public class HistorialPagoSuscripcionRequestDTO {
    @NotNull(message = "El monto es obligatorio")
    @Min(value = 0, message = "El monto debe ser mayor o igual a 0")
    private Integer monto;
    
    private String periodo;
    private String estado;
    
    @NotNull(message = "El ID de la suscripción es obligatorio")
    private Long suscripcionId;
    
    private Long pagoId;
}