package com.biblioteca.models.comercial;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.biblioteca.models.Usuario;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(exclude = { "historialPagos" })
@ToString(exclude = { "historialPagos" })
public class Suscripcion {
  private Long id;
  private Usuario usuario;
  private Plan plan;
  private LocalDate fechaInicio;
  private LocalDate fechaRenovacion;
  private String estado; // Activa, Cancelada, Pendiente, etc.
  private MetodoPago metodoPago;

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