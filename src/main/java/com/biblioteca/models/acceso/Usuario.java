package com.biblioteca.models.acceso;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.biblioteca.models.comercial.Plan;
import com.biblioteca.models.comercial.Suscripcion;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(exclude = { "perfiles", "roles", "suscripciones", "sesiones", "lector" })
@ToString(exclude = { "perfiles", "roles", "suscripciones", "sesiones", "lector" })
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario {

  // ========== IDENTIFICACIÓN PRINCIPAL ==========
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false, length = 50)
  private String username;

  @Column(unique = true, nullable = false, length = 100)
  private String email;

  // ========== CREDENCIALES Y SEGURIDAD ==========
  @Column(nullable = false)
  private String password;

  private LocalDateTime ultimaActualizacionPassword;

  @Builder.Default
  private Integer intentosFallidosLogin = 0;

  private LocalDateTime fechaBloqueo;

  // ========== ESTADO DE LA CUENTA ==========
  @Builder.Default
  private Boolean activo = true;

  @Builder.Default
  private Boolean emailVerificado = false;

  // ========== HISTORIAL DE ACTIVIDAD ==========
  private LocalDateTime fechaRegistro;
  private LocalDateTime ultimaActividad;

  // ========== CONFIGURACIÓN DE PERÍODOS DE PRUEBA ==========
  @Column(name = "ha_usado_periodo_prueba")
  @Builder.Default
  private Boolean haUsadoPeriodoPrueba = false;

  @Column(name = "fecha_primer_periodo_prueba")
  private LocalDateTime fechaPrimerPeriodoPrueba;

  // ========== CONFIGURACIÓN GLOBAL DE LA CUENTA ==========
  @Column(length = 10)
  private String idiomaPreferido; // "ES", "EN", "FR"

  @Column(length = 50)
  private String zonaHoraria; // "America/Mexico_City"

  // ========== CONFIGURACIÓN DE NOTIFICACIONES GLOBALES ==========
  @Builder.Default
  private Boolean notificacionesGlobalesActivas = true;

  @Builder.Default
  private Boolean recibirNewsletters = false;

  @Builder.Default
  private Boolean recibirPromociones = false;

  // ========== RELACIONES ==========
  @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Lector lector;

  @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private Set<Suscripcion> suscripciones = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "rol_id"))
  @Builder.Default
  private Set<Rol> roles = new HashSet<>();

  @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private Set<Perfil> perfiles = new HashSet<>();

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "perfil_principal_id")
  private Perfil perfilPrincipal;

  @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private Set<SesionUsuario> sesiones = new HashSet<>();

  // ========== MÉTODOS DE GESTIÓN DE PERFILES ==========
  public void addPerfil(Perfil perfil) {
    this.perfiles.add(perfil);
    perfil.setUsuario(this);

    // Si es el primer perfil, hacerlo principal automáticamente
    if (this.perfilPrincipal == null) {
      this.perfilPrincipal = perfil;
      perfil.setEsPerfilPrincipal(true);
    }
  }

  public void removePerfil(Perfil perfil) {
    this.perfiles.remove(perfil);
    perfil.setUsuario(null);

    // Si se elimina el perfil principal, asignar otro
    if (this.perfilPrincipal != null && this.perfilPrincipal.equals(perfil)) {
      this.perfilPrincipal = this.perfiles.stream().findFirst().orElse(null);
    }
  }

  // ========== MÉTODOS DE GESTIÓN DE ROLES ==========
  public void addRol(Rol rol) {
    this.roles.add(rol);
  }

  public void removeRol(Rol rol) {
    this.roles.remove(rol);
  }

  public boolean tieneRol(String nombreRol) {
    return roles.stream().anyMatch(rol -> rol.getNombre().equals(nombreRol));
  }

  // ========== MÉTODOS DE GESTIÓN DE SESIONES ==========
  public void addSesion(SesionUsuario sesion) {
    this.sesiones.add(sesion);
    sesion.setUsuario(this);
  }

  public void removeSesion(SesionUsuario sesion) {
    this.sesiones.remove(sesion);
    sesion.setUsuario(null);
  }

  // ========== MÉTODOS DE GESTIÓN DE SUSCRIPCIONES ==========
  public void addSuscripcion(Suscripcion suscripcion) {
    this.suscripciones.add(suscripcion);
    suscripcion.setUsuario(this);
  }

  public void removeSuscripcion(Suscripcion suscripcion) {
    this.suscripciones.remove(suscripcion);
    suscripcion.setUsuario(null);
  }

  public Suscripcion getSuscripcionActiva() {
    return suscripciones.stream()
        .filter(s -> "ACTIVA".equals(s.getEstado()))
        .findFirst()
        .orElse(null);
  }

  public Plan getPlanActual() {
    Suscripcion suscripcionActiva = getSuscripcionActiva();
    return suscripcionActiva != null ? suscripcionActiva.getPlan() : null;
  }

  // ========== MÉTODOS DE CONVENIENCIA ==========
  public boolean estaActivo() {
    return activo && (fechaBloqueo == null || fechaBloqueo.isBefore(LocalDateTime.now()));
  }

  public boolean puedeIniciarSesion() {
    return estaActivo() && emailVerificado;
  }

  public void bloquearPorIntentosExcesivos() {
    this.fechaBloqueo = LocalDateTime.now().plusMinutes(15); // Bloqueo temporal de 15 minutos
    this.intentosFallidosLogin = 0;
  }

  public void registrarIntentoFallido() {
    this.intentosFallidosLogin = (this.intentosFallidosLogin == null ? 0 : this.intentosFallidosLogin) + 1;
    if (this.intentosFallidosLogin >= 5) {
      bloquearPorIntentosExcesivos();
    }
  }

  public void resetearIntentosLogin() {
    this.intentosFallidosLogin = 0;
    this.fechaBloqueo = null;
  }
}