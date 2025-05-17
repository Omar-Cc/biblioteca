package com.biblioteca.models.comercial;

import java.util.HashSet;
import java.util.Set;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(exclude = { "planBeneficios" })
@ToString(exclude = { "planBeneficios" })
public class Plan {
    private Long id;
    private String nombre;
    private String descripcion;
    private String descripcionCorta;
    private Integer precioMensual; // En centavos
    private Integer precioAnual; // En centavos
    private Integer diasPrueba;
    private String periodoFacturacion;
    
    @Builder.Default
    private boolean activo = true;
    
    @Builder.Default
    private Set<PlanBeneficio> planBeneficios = new HashSet<>();
    
    public void addPlanBeneficio(PlanBeneficio planBeneficio) {
        this.planBeneficios.add(planBeneficio);
        planBeneficio.setPlan(this);
    }
    
    public void removePlanBeneficio(PlanBeneficio planBeneficio) {
        this.planBeneficios.remove(planBeneficio);
        planBeneficio.setPlan(null);
    }
}