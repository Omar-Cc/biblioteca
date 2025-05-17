package com.biblioteca.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActividadRecienteDTO {
    private String titulo;
    private String descripcion;
    private String usuario;
    private LocalDateTime fecha;
    private String tipo; // PRÉSTAMO, DEVOLUCIÓN, REGISTRO, etc.
}