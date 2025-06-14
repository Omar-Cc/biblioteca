package com.biblioteca.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibroPopularDTO {
    private Long contenidoId;
    private String titulo;
    private String autores;
    private Long totalPrestamos;
    private Long prestamosActivos;
    private Long prestamosMes;
    private String portadaUrl;
    private Integer precio;
    private Integer anioPublicacion;
    private String badge; // "Top 1", "Trending", etc.
}