package com.biblioteca.models.comercial;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class Beneficio {
    private Long id;
    private Long categoriaId;
    private String nombre;
    private String descripcion;
    private String icono;
    private String tipoDato;
    
    @Builder.Default
    private boolean activo = true;
}