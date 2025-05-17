package com.biblioteca.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Genero {
    private Long id;
    private String nombre;
    private String descripcion;
    private Long parentId;
    private Integer nivel;
    @Builder.Default
    private List<Genero> subgeneros = new ArrayList<>(); 
}