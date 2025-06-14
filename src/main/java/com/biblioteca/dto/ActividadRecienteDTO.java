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
    private String usuario;
    private String descripcion;
    private LocalDateTime fecha;
    private String tipo; // PRÉSTAMO, DEVOLUCIÓN, REGISTRO, etc.

    public ActividadRecienteDTO(String usuario, LocalDateTime fecha, String tipo) {
        this.usuario = usuario;
        this.fecha = fecha;
        this.tipo = tipo;
    }
}