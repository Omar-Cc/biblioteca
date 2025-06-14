package com.biblioteca.models.acceso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"usuarios"})
@ToString(exclude = {"usuarios"})
@Entity
@Table(name = "roles")
public class Rol {

    // ========== IDENTIFICACIÓN DEL ROL ==========
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String nombre; // Ej: "ROLE_USER", "ROLE_ADMIN", "ROLE_MODERATOR"
    
    @Column(length = 200)
    private String descripcion;

    // ========== CONFIGURACIÓN DEL ROL ==========
    @Builder.Default
    private Boolean activo = true;

    @Builder.Default
    private Boolean esRolSistema = false; // Roles que no se pueden eliminar

    @Column(length = 20)
    private String categoria; // "SISTEMA", "USUARIO", "STAFF", "ESPECIAL"

    @Builder.Default
    private Integer nivelJerarquia = 0; // Para ordenar roles por importancia

    // ========== LIMITACIONES Y PERMISOS GENERALES ==========
    @Builder.Default
    private Boolean puedeAdministrarUsuarios = false;

    @Builder.Default
    private Boolean puedeAdministrarContenido = false;

    @Builder.Default
    private Boolean puedeModerar = false;

    @Builder.Default
    private Boolean puedeAccederAdministracion = false;

    // ========== METADATOS ==========
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private String creadoPor; // Username de quien creó el rol

    // ========== RELACIONES ==========
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Usuario> usuarios = new HashSet<>();

    // ========== MÉTODOS DE CONVENIENCIA ==========
    public boolean esRolAdministrador() {
        return "ROLE_ADMIN".equals(this.nombre) || "ROLE_SUPER_ADMIN".equals(this.nombre);
    }

    public boolean esRolUsuario() {
        return "ROLE_USER".equals(this.nombre) || "ROLE_LECTOR".equals(this.nombre);
    }

    public boolean esRolStaff() {
        return esRolAdministrador() || "ROLE_MODERATOR".equals(this.nombre) || "ROLE_EMPLEADO".equals(this.nombre);
    }

    public boolean tienePermisosPara(String accion) {
        // Lógica para verificar permisos específicos
        return switch (accion.toUpperCase()) {
            case "ADMINISTRAR_USUARIOS" -> puedeAdministrarUsuarios;
            case "ADMINISTRAR_CONTENIDO" -> puedeAdministrarContenido;
            case "MODERAR" -> puedeModerar;
            case "ACCEDER_ADMIN" -> puedeAccederAdministracion;
            default -> false;
        };
    }

    public int getCantidadUsuarios() {
        return usuarios != null ? usuarios.size() : 0;
    }
}