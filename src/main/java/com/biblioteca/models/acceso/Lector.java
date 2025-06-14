package com.biblioteca.models.acceso;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lectores")
public class Lector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    /* ========== INFORMACIÓN PERSONAL DEL TITULAR ========== */
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(length = 500)
    private String direccion;

    @Column(length = 20)
    private String telefono;

    @Temporal(TemporalType.DATE)
    private Date fechaNacimiento;

    // ========== INFORMACIÓN ADMINISTRATIVA (nivel cuenta) ==========
    @Builder.Default
    private Integer multasPendientes = 0;

    @Column(length = 20)
    @Builder.Default
    private String estadoCuenta = "ACTIVO";

    private LocalDateTime fechaSuspension;
    private String motivoSuspension;

    // ========== INFORMACIÓN DE CONTACTO ADICIONAL ==========
    @Column(length = 100)
    private String contactoEmergenciaNombre;

    @Column(length = 20)
    private String contactoEmergenciaTelefono;

    @Column(length = 50)
    private String contactoEmergenciaRelacion;

    // ========== CONFIGURACIÓN GLOBAL DE NOTIFICACIONES (nivel cuenta) ==========
    @Builder.Default
    private Boolean notificacionesHabilitadas = true;

    @Builder.Default
    private Boolean notificacionesEmail = true;

    @Builder.Default
    private Boolean notificacionesSMS = false;

    @Builder.Default
    private Boolean notificacionesPush = true;

    // ========== CONFIGURACIÓN DE MARKETING (nivel cuenta) ==========
    @Builder.Default
    private Boolean recibirNewsletters = false;

    @Builder.Default
    private Boolean recibirPromociones = false;

    // ========== METADATOS DEL TITULAR ==========
    private LocalDateTime fechaCreacionPerfil;
    private LocalDateTime ultimaActualizacion;
    private LocalDateTime ultimoAcceso;

    @Builder.Default
    private Boolean perfilCompleto = false;

    @Builder.Default
    private Boolean terminosAceptados = false;

    private LocalDateTime fechaAceptacionTerminos;

    // ========== INFORMACIÓN PARA MEJORA DE SERVICIO ==========
    @Column(length = 500)
    private String comentariosAdicionales;

    @Builder.Default
    private Boolean participarEncuestas = true;

    @Builder.Default
    private Boolean datosAnonimosInvestigacion = true;

    // ========== MÉTODOS DE CONVENIENCIA ==========
    public String getNombreCompleto() {
        return this.nombre + " " + this.apellido;
    }

    public boolean tieneMultasPendientes() {
        return this.multasPendientes != null && this.multasPendientes > 0;
    }

    public boolean esCuentaActiva() {
        return "ACTIVO".equals(this.estadoCuenta);
    }

    public boolean puedeUsarServicios() {
        return esCuentaActiva() && !tieneMultasPendientes();
    }

    public void agregarMulta(Integer monto) {
        this.multasPendientes = (this.multasPendientes == null ? 0 : this.multasPendientes) + monto;
    }

    public void pagarMulta(Integer monto) {
        if (this.multasPendientes != null && this.multasPendientes >= monto) {
            this.multasPendientes -= monto;
        }
    }
}