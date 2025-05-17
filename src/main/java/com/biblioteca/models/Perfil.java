package com.biblioteca.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"usuario"})
@ToString(exclude = {"usuario"})
public class Perfil {
    private Long id;
    private Usuario usuario; // Vinculado al Usuario
    private String nombreVisible;
    private String fotoPerfil; // URL o path a la imagen
    private String idiomaPreferido;
    private int limitePrestamosDesignado;
    private boolean esInfantil;
    private boolean activo = true;
    private boolean esPerfilPrincipal;
    private LocalDate fechaCreacion;
    private LocalDate fechaModificacion;
    private LocalDateTime ultimaActividad;
}