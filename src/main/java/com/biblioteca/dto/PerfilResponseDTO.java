package com.biblioteca.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilResponseDTO {
    
    // ========== INFORMACIÓN BÁSICA ==========
    private Long id;
    private Long usuarioId;
    private String nombreUsuario;
    private String nombreVisible;
    private String descripcionPerfil;

    // ========== CONFIGURACIÓN VISUAL ==========
    private String fotoPerfil;
    private String temaPreferido;
    private String colorAcento;

    // ========== CONFIGURACIÓN BÁSICA ==========
    private String idiomaPreferido;
    private Integer limitePrestamosDesignado;

    // ========== ESTADO Y TIPO ==========
    private Boolean activo;
    private Boolean esPerfilPrincipal;
    private Boolean esInfantil;

    // ========== CONFIGURACIÓN DE PRIVACIDAD ==========
    private Boolean perfilPublico;
    private Boolean mostrarHistorialLectura;
    private Boolean permitirRecomendaciones;

    // ========== CONFIGURACIÓN DE CONTENIDO ==========
    private Boolean filtroContenidoAdulto;
    private String nivelRestriccion;

    // ========== CONFIGURACIÓN DE NOTIFICACIONES ==========
    private Boolean notificacionesPrestamos;
    private Boolean notificacionesVencimientos;
    private Boolean notificacionesRecomendaciones;
    private Boolean notificacionesEventos;
    private Boolean notificacionesNuevasAdquisiciones;
    private Boolean notificacionesGruposLectura;

    // ========== CONFIGURACIÓN AVANZADA ==========
    private String frecuenciaResumen;
    private Boolean recibirResumenActividad;
    private String horariosPreferidos;
    private String ubicacion;

    // ========== ESTADÍSTICAS BÁSICAS ==========
    private Integer vecesUsado;
    private String ultimoUso;

    // ========== ESTADÍSTICAS DE LECTURA ==========
    private Integer horasLecturaTotal;

    // ========== FECHAS ==========
    private String fechaCreacion;
    private String fechaModificacion;
    private String ultimaActividad;

    // ========== INFORMACIÓN EXTENDIDA (opcional) ==========
    private InformacionPerfilResponseDTO informacionExtendida;
}