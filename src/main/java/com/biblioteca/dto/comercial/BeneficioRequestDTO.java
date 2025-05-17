package com.biblioteca.dto.comercial;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficioRequestDTO {
    private Long categoriaId;
    
    @NotBlank(message = "El nombre del beneficio es obligatorio")
    private String nombre;
    
    private String descripcion;
    private String icono;
    private String tipoDato;
    private boolean activo;
}