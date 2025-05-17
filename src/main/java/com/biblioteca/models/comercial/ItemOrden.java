package com.biblioteca.models.comercial;

import com.biblioteca.models.contenido.Contenido;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class ItemOrden {
    private Long id;
    private Orden orden;
    private Contenido contenido;
    private Integer cantidad;
    private Integer precioAlComprar; // En centavos
    private Integer descuentoAplicado; // En centavos
    
}