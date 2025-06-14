package com.biblioteca.models.comercial;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.biblioteca.models.acceso.Usuario;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(exclude = { "historialPagos" })
@ToString(exclude = { "historialPagos" })
@Entity
@Table(name = "suscripciones")
public class Suscripcion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "plan_id")
  private Plan plan;

  private LocalDate fechaInicio;
  private LocalDate fechaRenovacion;
  private String estado; // Activa, Cancelada, Pendiente, etc.
  
  // Modalidad de pago específica elegida por el usuario (mensual/anual)
  private String modalidadPago; // "mensual" o "anual"

  private String estadoAnterior;
  private LocalDateTime fechaCambioEstado;
  private String motivoCambio;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "metodo_pago_id")
  private MetodoPago metodoPago;

  @OneToMany(mappedBy = "suscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private Set<HistorialPagoSuscripcion> historialPagos = new HashSet<>();

  public void addHistorialPago(HistorialPagoSuscripcion pago) {
    this.historialPagos.add(pago);
    pago.setSuscripcion(this);
  }

  public void removeHistorialPago(HistorialPagoSuscripcion pago) {
    this.historialPagos.remove(pago);
    pago.setSuscripcion(null);
  }
}