package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContenidoSerie {
    private Long serieId; // FK a Serie
    private Long contenidoId; // FK a Contenido
    private int orden; // Orden del contenido dentro de la serie
}