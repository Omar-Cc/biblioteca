package com.biblioteca.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolResponseDTO {
    
    // ========== IDENTIFICACIÓN ==========
    private Long id;
    private String nombre;
    private String descripcion;

    // ========== CONFIGURACIÓN ==========
    private Boolean activo;
    private Boolean esRolSistema;
    private String categoria;
    private Integer nivelJerarquia;

    // ========== PERMISOS ==========
    private Boolean puedeAdministrarUsuarios;
    private Boolean puedeAdministrarContenido;
    private Boolean puedeModerar;
    private Boolean puedeAccederAdministracion;

    // ========== ESTADÍSTICAS ==========
    private Integer cantidadUsuarios;

    // ========== METADATOS ==========
    private String fechaCreacion;
    private String fechaModificacion;
    private String creadoPor;

    // ========== MÉTODOS DE CONVENIENCIA ==========
    private Boolean esRolAdministrador;
    private Boolean esRolUsuario;
    private Boolean esRolStaff;
}