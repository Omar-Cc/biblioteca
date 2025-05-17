package com.biblioteca.models.comercial;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class PlanBeneficio {
    private Plan plan;
    private Beneficio beneficio;
    private String valor;
    
    @Builder.Default
    private boolean activo = true;
}