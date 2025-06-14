package com.biblioteca.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObraPopularDTO {
    private Long id;
    private String titulo;
    private String autor;
    private Long prestamos;
    private String portadaUrl;
    private String badge;
}