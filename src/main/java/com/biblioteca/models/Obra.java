package com.biblioteca.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Obra {
    private Long id;
    private String titulo;
    private String descripcion;
    private int anioPublicacion;
    private String isbn;
    private Editorial editorial;
    private List<String> idioma;
    private LocalDate fechaCreacion;
    private LocalDate fechaActualizacion;
}
