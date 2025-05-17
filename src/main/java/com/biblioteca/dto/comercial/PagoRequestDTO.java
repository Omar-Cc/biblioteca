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
public class PagoRequestDTO {
    @NotNull(message = "El monto es obligatorio")
    @Min(value = 0, message = "El monto debe ser mayor o igual a 0")
    private Integer monto;
    
    private String referenciaPago;
    private String estado;
    
    @NotNull(message = "El ID de la orden es obligatorio")
    private Long ordenId;
    
    @NotNull(message = "El ID del método de pago es obligatorio")
    private Long metodoPagoId;
}