package com.biblioteca.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PerfilResponseDTO {
    private Long id;
    private Long usuarioId;
    private String nombreUsuario;
    private String nombreVisible;
    private String fotoPerfil;
    private String idiomaPreferido;
    private int limitePrestamosDesignado;
    private boolean esInfantil;
    private boolean activo;
    private boolean esPerfilPrincipal;
    private String fechaCreacion;
    private String fechaModificacion;
    private String ultimaActividad;
}