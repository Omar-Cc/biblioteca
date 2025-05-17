package com.biblioteca.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasDTO {
    // Estadísticas generales
    private long totalUsuarios;
    private long nuevosUsuariosMes;
    private long totalObras;
    private long nuevasObrasMes;
    private long totalPrestamos;
    private long nuevosPrestamos;
    private long totalGeneros;
    
    // Datos para gráficos
    private Map<String, Integer> prestamosPorMes;
    private Map<String, Integer> obrasPorGenero;
}