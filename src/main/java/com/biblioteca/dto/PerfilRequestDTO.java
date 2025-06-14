package com.biblioteca.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PerfilRequestDTO {

    // ========== INFORMACIÓN BÁSICA ==========
    @NotEmpty(message = "El nombre visible no puede estar vacío.")
    @Size(min = 2, max = 100, message = "El nombre visible debe tener entre 2 y 100 caracteres.")
    private String nombreVisible;

    @Size(max = 500, message = "La descripción no debe exceder los 500 caracteres.")
    private String descripcionPerfil;

    // ========== CONFIGURACIÓN VISUAL ==========
    @Size(max = 500, message = "La URL de la foto no debe exceder los 500 caracteres.")
    private String fotoPerfil;

    @Size(max = 20, message = "El tema preferido no debe exceder los 20 caracteres.")
    private String temaPreferido; // "CLARO", "OSCURO", "AUTO"

    @Size(max = 10, message = "El color de acento no debe exceder los 10 caracteres.")
    private String colorAcento;

    // ========== CONFIGURACIÓN BÁSICA ==========
    @Size(max = 10, message = "El código de idioma no debe exceder los 10 caracteres.")
    private String idiomaPreferido; // "ES", "EN", "FR"

    @Min(value = 1, message = "El límite mínimo de préstamos es 1.")
    @Max(value = 10, message = "El límite máximo de préstamos es 10.")
    private Integer limitePrestamosDesignado = 2;

    // ========== TIPO DE PERFIL ==========
    private Boolean esPerfilPrincipal = false;
    private Boolean esInfantil = false;

    // ========== CONFIGURACIÓN DE PRIVACIDAD ==========
    private Boolean perfilPublico = false;
    private Boolean mostrarHistorialLectura = false;
    private Boolean permitirRecomendaciones = true;

    // ========== CONFIGURACIÓN DE CONTENIDO ==========
    private Boolean filtroContenidoAdulto = false;

    @Size(max = 20, message = "El nivel de restricción no debe exceder los 20 caracteres.")
    private String nivelRestriccion = "NINGUNO"; // "NINGUNO", "MODERADO", "ESTRICTO"

    // ========== CONFIGURACIÓN DE NOTIFICACIONES ==========
    private Boolean notificacionesPrestamos = true;
    private Boolean notificacionesVencimientos = true;
    private Boolean notificacionesRecomendaciones = true;
    private Boolean notificacionesEventos = false;
    private Boolean notificacionesNuevasAdquisiciones = false;
    private Boolean notificacionesGruposLectura = false;

    // ========== CONFIGURACIÓN AVANZADA DE NOTIFICACIONES ==========
    @Size(max = 20, message = "La frecuencia de resumen no debe exceder los 20 caracteres.")
    private String frecuenciaResumen = "SEMANAL"; // "DIARIO", "SEMANAL", "MENSUAL", "NUNCA"

    private Boolean recibirResumenActividad = true;

    @Size(max = 500, message = "Los horarios preferidos no deben exceder los 500 caracteres.")
    private String horariosPreferidos;

    // ========== INFORMACIÓN GEOGRÁFICA ==========
    @Size(max = 100, message = "La ubicación no debe exceder los 100 caracteres.")
    private String ubicacion;

    // Getters y setters explícitos para compatibilidad con mappers
    public Boolean getEsInfantil() {
        return esInfantil != null ? esInfantil : false;
    }

    public void setEsInfantil(Boolean esInfantil) {
        this.esInfantil = esInfantil != null ? esInfantil : false;
    }

    public Boolean getEsPerfilPrincipal() {
        return esPerfilPrincipal != null ? esPerfilPrincipal : false;
    }

    public void setEsPerfilPrincipal(Boolean esPerfilPrincipal) {
        this.esPerfilPrincipal = esPerfilPrincipal != null ? esPerfilPrincipal : false;
    }
}