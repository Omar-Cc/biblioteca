package com.biblioteca.models.comercial;

import com.biblioteca.models.contenido.Contenido;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class ItemCarrito {
    private Long id;
    private Carrito carrito;
    private Contenido contenido;
    private Integer cantidad;
    private Integer precio; // En centavos
    private Integer descuento; // En centavos
    
}