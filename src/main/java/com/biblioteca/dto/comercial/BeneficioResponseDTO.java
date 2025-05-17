package com.biblioteca.dto.comercial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficioResponseDTO {
    private Long id;
    private Long categoriaId;
    private String nombre;
    private String descripcion;
    private String icono;
    private String tipoDato;
    private boolean activo;
}