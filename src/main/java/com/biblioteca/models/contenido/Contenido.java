package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

import com.biblioteca.enums.TipoContenido;
import com.biblioteca.models.Editorial;
import com.biblioteca.models.Obra;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Contenido {
    private Long id;

    private Obra obra;

    private Editorial editorial;

    private String portadaUrl; // URL o path a la imagen

    private String sinopsis;

    private Integer precio; // En centavos

    private boolean enVenta;

    private TipoContenido tipo;

    private boolean puedeSerPrestado;

    private LocalDate fechaCreacion;
    
    private String isbn;

    @Builder.Default
    private boolean activo = true;
}
