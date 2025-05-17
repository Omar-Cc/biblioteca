package com.biblioteca.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObraPopularDTO {
    private Long id;
    private String titulo;
    private String autor;
    private int prestamos;
}