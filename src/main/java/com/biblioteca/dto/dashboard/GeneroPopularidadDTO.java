package com.biblioteca.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneroPopularidadDTO {
    private String genero;
    private Long totalObras;
    private Long totalPrestamos;
    private Double popularidadPorcentaje;
    private String color; // Para gráficos
}