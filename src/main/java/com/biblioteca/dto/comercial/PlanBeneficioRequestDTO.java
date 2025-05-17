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
public class PlanBeneficioRequestDTO {
    @NotNull(message = "El ID del plan es obligatorio")
    private Long planId;
    
    @NotNull(message = "El ID del beneficio es obligatorio")
    private Long beneficioId;
    
    private String valor;
    private boolean activo;
}