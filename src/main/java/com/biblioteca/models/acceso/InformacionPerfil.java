package com.biblioteca.models.acceso;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.biblioteca.enums.NivelLector;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "perfil" })
@ToString(exclude = { "perfil" })
@Entity
@Table(name = "informacion_perfiles")
public class InformacionPerfil {

  // ========== IDENTIFICACIÓN ==========
  @Id
  private Long id; // Mismo ID que Perfil

  @OneToOne
  @MapsId
  @JoinColumn(name = "id")
  private Perfil perfil;

  // ========== INFORMACIÓN PERSONAL DEL PERFIL ==========
  @Column(length = 1000)
  private String biografia; // Cada perfil puede tener su propia bio

  @Column(length = 1000)
  private String generosPreferidos; // Cada perfil sus géneros

  @Column(length = 1000)
  private String autoresFavoritos; // Cada perfil sus autores

  @Column(length = 1000)
  private String temasInteres; // Cada perfil sus temas

  // ========== NIVEL Y PROGRESO DE LECTURA ==========
  @Column(length = 20)
  @Builder.Default
  private NivelLector nivelLectura = NivelLector.PRINCIPIANTE; // Cada perfil su nivel

  @Column(length = 500)
  private String metasPersonales; // Cada perfil sus metas

  @Builder.Default
  private Integer metaLibrosMes = 0; // Meta por perfil

  @Builder.Default
  private Integer metaLibrosAnio = 0; // Meta por perfil

  // ========== CONFIGURACIÓN DE EXPERIENCIA DE LECTURA ==========
  @Column(length = 20)
  @Builder.Default
  private String formatoPreferido = "AMBOS"; // "FISICO", "DIGITAL", "AMBOS"

  @Column(length = 10)
  @Builder.Default
  private String idiomaLectura = "ES"; // Idioma preferido para lectura

  @Builder.Default
  private Boolean mostrarProgreso = true; // Mostrar progreso de lectura

  @Builder.Default
  private Boolean aceptarInvitacionesGrupos = true; // Acepta invitaciones a grupos

  // ========== ESTADÍSTICAS ACUMULADAS POR PERFIL ==========
  @Builder.Default
  private Integer totalLibrosLeidos = 0;

  @Builder.Default
  private Integer totalPrestamoRealizados = 0;

  @Builder.Default
  private Integer totalResenasEscritas = 0;

  @Builder.Default
  private Integer totalRecomendacionesHechas = 0;

  @Builder.Default
  private Integer puntuacionComunidad = 0;

  @Builder.Default
  private Integer tiempoLecturaMinutos = 0;

  // ========== ESTADÍSTICAS MENSUALES/ANUALES ==========
  @Builder.Default
  private Integer librosLeidosMesActual = 0;

  @Builder.Default
  private Integer librosLeidosAnioActual = 0;

  private LocalDateTime fechaUltimoReset; // Para resetear estadísticas mensuales

  // ========== CONFIGURACIÓN DE RECOMENDACIONES ==========
  @Builder.Default
  private Boolean algoritmoRecomendacionesActivo = true;

  @Column(length = 20)
  @Builder.Default
  private String tipoRecomendaciones = "PERSONALIZADO"; // "POPULAR", "PERSONALIZADO", "MIXTO"

  // ========== METADATOS Y FECHAS ==========
  private LocalDateTime fechaCreacion;
  private LocalDateTime ultimaActualizacion;
  private LocalDateTime ultimaActividad;

  // ========== MÉTODOS DE GESTIÓN DE ESTADÍSTICAS ==========
  public void incrementarLibrosLeidos() {
    this.totalLibrosLeidos = (this.totalLibrosLeidos == null ? 0 : this.totalLibrosLeidos) + 1;
    this.librosLeidosMesActual = (this.librosLeidosMesActual == null ? 0 : this.librosLeidosMesActual) + 1;
    this.librosLeidosAnioActual = (this.librosLeidosAnioActual == null ? 0 : this.librosLeidosAnioActual) + 1;
    this.ultimaActividad = LocalDateTime.now();
    actualizarNivelLectura();
  }

  public void incrementarPrestamo() {
    this.totalPrestamoRealizados = (this.totalPrestamoRealizados == null ? 0 : this.totalPrestamoRealizados) + 1;
    this.ultimaActividad = LocalDateTime.now();
  }

  public void incrementarResenas() {
    this.totalResenasEscritas = (this.totalResenasEscritas == null ? 0 : this.totalResenasEscritas) + 1;
    this.ultimaActividad = LocalDateTime.now();
  }

  public void agregarTiempoLectura(Integer minutos) {
    this.tiempoLecturaMinutos = (this.tiempoLecturaMinutos == null ? 0 : this.tiempoLecturaMinutos) + minutos;
  }

  // ========== MÉTODOS DE CÁLCULO DE PROGRESO ==========
  public Double getProgresoMetaAnual() {
    if (this.metaLibrosAnio == null || this.metaLibrosAnio == 0) {
      return 0.0;
    }
    return (double) this.librosLeidosAnioActual / this.metaLibrosAnio * 100;
  }

  public Double getProgresoMetaMensual() {
    if (this.metaLibrosMes == null || this.metaLibrosMes == 0) {
      return 0.0;
    }
    return (double) this.librosLeidosMesActual / this.metaLibrosMes * 100;
  }

  public Integer getHorasLecturaTotal() {
    return tiempoLecturaMinutos != null ? tiempoLecturaMinutos / 60 : 0;
  }

  public void actualizarUltimaActividad() {
    this.ultimaActividad = LocalDateTime.now();
    this.ultimaActualizacion = LocalDateTime.now();
  }

  // ========== MÉTODOS DE GESTIÓN DE NIVEL ==========
  private void actualizarNivelLectura() {
    // Lógica para actualizar el nivel basado en libros leídos
    if (totalLibrosLeidos >= 500) {
      this.nivelLectura = NivelLector.MAESTRO;
    } else if (totalLibrosLeidos >= 150) {
      this.nivelLectura = NivelLector.EXPERTO;
    } else if (totalLibrosLeidos >= 50) {
      this.nivelLectura = NivelLector.AVANZADO;
    } else if (totalLibrosLeidos >= 10) {
      this.nivelLectura = NivelLector.INTERMEDIO;
    } else {
      this.nivelLectura = NivelLector.PRINCIPIANTE;
    }
  }

  public void resetearEstadisticasMensuales() {
    this.librosLeidosMesActual = 0;
    this.fechaUltimoReset = LocalDateTime.now();
  }

  public void resetearEstadisticasAnuales() {
    this.librosLeidosAnioActual = 0;
    this.fechaUltimoReset = LocalDateTime.now();
  }
}