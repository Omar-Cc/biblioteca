package com.biblioteca.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Serie {
    private Long id;
    private String nombre;
    private String descripcion;
    private int numeroVolumenes;
    private boolean completa;
}