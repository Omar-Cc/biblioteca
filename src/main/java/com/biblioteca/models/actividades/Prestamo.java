package com.biblioteca.models.actividades;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.biblioteca.enums.EstadoPrestamo;
import com.biblioteca.models.acceso.Perfil;
import com.biblioteca.models.contenido.Contenido;
/* import com.biblioteca.models.gestion.Sucursal; */

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "prestamos")
public class Prestamo implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contenido_id", nullable = false)
  @NotNull
  private Contenido contenido;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "perfil_id", nullable = false)
  @NotNull
  private Perfil perfil;

  /*
   * @ManyToOne(fetch = FetchType.LAZY)
   * 
   * @JoinColumn(name = "sucursal_id")
   * private Sucursal sucursal;
   */

  @Column(name = "fecha_prestamo", nullable = false)
  @NotNull
  private LocalDate fechaPrestamo;

  @Column(name = "fecha_devolucion_prevista", nullable = false)
  @NotNull
  private LocalDate fechaDevolucionPrevista;

  @Column(name = "fecha_devolucion_real")
  private LocalDate fechaDevolucionReal;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado", nullable = false)
  @NotNull
  private EstadoPrestamo estado;

  @Column(name = "observaciones")
  private String observaciones;

  @Column(name = "fecha_creacion", nullable = false, updatable = false)
  private LocalDateTime fechaCreacion;

  @Column(name = "fecha_actualizacion")
  private LocalDateTime fechaActualizacion;

  @PrePersist
  protected void onCreate() {
    fechaCreacion = LocalDateTime.now();
    fechaActualizacion = LocalDateTime.now();
    if (estado == null) {
      estado = EstadoPrestamo.ACTIVO;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    fechaActualizacion = LocalDateTime.now();
  }

  // Métodos de conveniencia
  public boolean isVencido() {
    return estado == EstadoPrestamo.ACTIVO &&
        LocalDate.now().isAfter(fechaDevolucionPrevista);
  }

  public boolean isDevuelto() {
    return estado == EstadoPrestamo.DEVUELTO;
  }

  public long getDiasRetraso() {
    if (!isVencido()) {
      return 0;
    }
    return LocalDate.now().toEpochDay() - fechaDevolucionPrevista.toEpochDay();
  }

  // Método adicional para calcular días restantes
  public long getDiasRestantes() {
    if (estado != EstadoPrestamo.ACTIVO) {
      return 0;
    }
    return fechaDevolucionPrevista.toEpochDay() - LocalDate.now().toEpochDay();
  }

  // Método para verificar si está próximo a vencer
  public boolean isProximoAVencer(int diasAnticipacion) {
    return estado == EstadoPrestamo.ACTIVO &&
        getDiasRestantes() <= diasAnticipacion &&
        getDiasRestantes() > 0;
  }
}