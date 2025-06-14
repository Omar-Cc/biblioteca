package com.biblioteca.models.acceso;

import java.time.LocalDateTime;

import com.biblioteca.enums.EstadoSesion;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "usuario", "perfil" })
@ToString(exclude = { "usuario", "perfil" })
@Table(name = "sesiones_usuario")
public class SesionUsuario {

  // ========== IDENTIFICACIÓN DE LA SESIÓN ==========
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "token", nullable = false, unique = true, length = 255)
  private String token;

  // ========== RELACIONES ==========
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "perfil_id")
  private Perfil perfil; // Perfil activo en esta sesión

  // ========== INFORMACIÓN DEL DISPOSITIVO Y CONEXIÓN ==========
  @Column(length = 500)
  private String dispositivoInfo; // Info del dispositivo

  @Column(length = 200)
  private String navegador; // Info del navegador

  @Column(length = 45)
  private String ipAddress; // IP de conexión (IPv4/IPv6)

  @Column(length = 100)
  private String ubicacionGeografica; // Ciudad/País

  @Column(length = 100)
  private String sistemaOperativo; // Windows, Mac, Android, iOS

  // ========== FECHAS Y TIEMPOS DE LA SESIÓN ==========
  private LocalDateTime fechaInicio;
  private LocalDateTime fechaUltimaActividad;
  private LocalDateTime fechaExpiracion;
  private LocalDateTime fechaCierre;

  // ========== ESTADO Y CONFIGURACIÓN DE LA SESIÓN ==========
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private EstadoSesion estado = EstadoSesion.ACTIVA;

  @Builder.Default
  private Boolean recordarSesion = false;

  @Builder.Default
  private Boolean esSesionMovil = false;

  @Builder.Default
  private Boolean sesionSegura = false; // HTTPS

  // ========== ESTADÍSTICAS DE LA SESIÓN ==========
  @Builder.Default
  private Integer tiempoActivoMinutos = 0; // Tiempo activo en la sesión

  @Builder.Default
  private Integer paginasVistas = 0; // Páginas/pantallas visitadas

  private LocalDateTime ultimaInteraccion; // Última interacción del usuario

  // ========== MÉTODOS DE VALIDACIÓN ==========
  public boolean esValida() {
    return estado == EstadoSesion.ACTIVA &&
        fechaExpiracion != null &&
        fechaExpiracion.isAfter(LocalDateTime.now());
  }

  public boolean estaExpirada() {
    return fechaExpiracion != null && fechaExpiracion.isBefore(LocalDateTime.now());
  }

  public boolean esActiva() {
    return estado == EstadoSesion.ACTIVA;
  }

  // ========== MÉTODOS DE GESTIÓN DE LA SESIÓN ==========
  public void terminarSesion() {
    this.fechaCierre = LocalDateTime.now();
    this.estado = EstadoSesion.CERRADA_MANUAL;
  }

  public void expirarSesion() {
    this.fechaCierre = LocalDateTime.now();
    this.estado = EstadoSesion.EXPIRADA;
  }

  public void suspenderSesion() {
    this.estado = EstadoSesion.SUSPENDIDA;
  }

  public void invalidarSesion() {
    this.fechaCierre = LocalDateTime.now();
    this.estado = EstadoSesion.INVALIDADA;
  }

  // ========== MÉTODOS DE ESTADÍSTICAS ==========
  public void registrarActividad() {
    this.fechaUltimaActividad = LocalDateTime.now();
    this.ultimaInteraccion = LocalDateTime.now();
    this.paginasVistas = (this.paginasVistas == null ? 0 : this.paginasVistas) + 1;
  }

  public void actualizarTiempoActivo() {
    if (fechaInicio != null && fechaUltimaActividad != null) {
      this.tiempoActivoMinutos = (int) java.time.Duration.between(fechaInicio, fechaUltimaActividad).toMinutes();
    }
  }

  public boolean esSesionLarga() {
    return tiempoActivoMinutos != null && tiempoActivoMinutos > 60; // Más de 1 hora
  }

  // ========== MÉTODOS DE INFORMACIÓN ==========
  public String getTipoDispositivo() {
    if (esSesionMovil) {
      return "MÓVIL";
    } else if (sistemaOperativo != null &&
        (sistemaOperativo.contains("Windows") || sistemaOperativo.contains("Mac")
            || sistemaOperativo.contains("Linux"))) {
      return "ESCRITORIO";
    } else {
      return "DESCONOCIDO";
    }
  }

  public boolean esSesionReciente() {
    return fechaInicio != null && fechaInicio.isAfter(LocalDateTime.now().minusHours(24));
  }
}