package com.biblioteca.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TendenciaPrestamoDTO {
    private String mes;           // "2025-01"
    private String nombreMes;     // "Enero"
    private Integer anio;
    private Integer numeroMes;
    private Long totalPrestamos;
    
    // Para gráficos
    private String etiqueta;      // "Ene 2025"
}