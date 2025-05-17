package com.biblioteca.dto.comercial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanBeneficioResponseDTO {
    private PlanResponseDTO plan;
    private BeneficioResponseDTO beneficio;
    private String valor;
    private boolean activo;

}