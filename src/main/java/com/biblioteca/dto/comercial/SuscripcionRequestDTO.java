package com.biblioteca.dto.comercial;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuscripcionRequestDTO {
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;
    
    @NotNull(message = "El ID del plan es obligatorio")
    private Long planId;
    
    private LocalDate fechaInicio;
    private LocalDate fechaRenovacion;
    private String estado;
    
    @NotNull(message = "El ID del método de pago es obligatorio")
    private Long metodoPagoId;
}