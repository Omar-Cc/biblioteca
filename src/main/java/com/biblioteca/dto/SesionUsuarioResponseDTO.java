package com.biblioteca.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesionUsuarioResponseDTO {
    
    // ========== INFORMACIÓN BÁSICA ==========
    private Long id;
    private String token;
    private Long usuarioId;
    private String nombreUsuario;
    private Long perfilId;
    private String nombrePerfil;

    // ========== INFORMACIÓN DEL DISPOSITIVO ==========
    private String dispositivoInfo;
    private String navegador;
    private String sistemaOperativo;
    private String tipoDispositivo;

    // ========== INFORMACIÓN DE CONEXIÓN ==========
    private String ipAddress;
    private String ubicacionGeografica;
    private Boolean sesionSegura;

    // ========== ESTADO DE LA SESIÓN ==========
    private String estado;
    private Boolean recordarSesion;
    private Boolean esSesionMovil;

    // ========== FECHAS ==========
    private String fechaInicio;
    private String fechaUltimaActividad;
    private String fechaExpiracion;
    private String fechaCierre;
    private String ultimaInteraccion;

    // ========== ESTADÍSTICAS ==========
    private Integer tiempoActivoMinutos;
    private Integer tiempoActivoHoras;
    private Integer paginasVistas;
    private Boolean esSesionLarga;
    private Boolean esSesionReciente;

    // ========== MÉTODOS DE CONVENIENCIA ==========
    public String getTiempoActivoFormateado() {
        if (tiempoActivoMinutos == null) return "0 minutos";
        
        int horas = tiempoActivoMinutos / 60;
        int minutos = tiempoActivoMinutos % 60;
        
        if (horas > 0) {
            return horas + "h " + minutos + "m";
        } else {
            return minutos + " minutos";
        }
    }
}