package com.biblioteca.models.comercial;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class MetodoPago {
    private Long id;
    private String nombre;
    private String descripcion;
    private boolean requiereAutorizacion;
    
    @Builder.Default
    private boolean activo = true;
}