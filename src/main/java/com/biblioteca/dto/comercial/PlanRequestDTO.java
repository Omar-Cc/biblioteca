package com.biblioteca.dto.comercial;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanRequestDTO {
    @NotBlank(message = "El nombre del plan es obligatorio")
    private String nombre;

    private String descripcion;

    private String descripcionCorta;
    
    @NotNull(message = "El precio mensual es obligatorio")
    @Min(value = 0, message = "El precio mensual debe ser mayor o igual a 0")
    private Integer precioMensual;
    
    @NotNull(message = "El precio anual es obligatorio")
    @Min(value = 0, message = "El precio anual debe ser mayor o igual a 0")
    private Integer precioAnual;
    
    @NotNull(message = "Los días de prueba son obligatorios")
    @Min(value = 0, message = "Los días de prueba deben ser mayor o igual a 0")
    private Integer diasPrueba;

    private String periodoFacturacion;
    
    private boolean activo;
}