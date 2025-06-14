package com.biblioteca.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActividadRecienteDTO {
    private String tipoActividad;
    private String descripcion;
    private String usuario;
    private LocalDateTime fechaActividad;
    private String categoria;
    private String imagen;
    private String tiempoRelativo; // "hace 2 horas"
}