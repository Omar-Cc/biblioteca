package com.biblioteca.dto.comercial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String descripcionCorta;
    private Integer precioMensual;
    private Integer precioAnual;
    private String periodoFacturacion;
    private Integer diasPrueba;
    private boolean activo;
    
}