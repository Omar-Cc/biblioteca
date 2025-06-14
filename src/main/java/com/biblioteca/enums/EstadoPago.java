package com.biblioteca.enums;

public enum EstadoPago {
  PENDIENTE("Pendiente"),
  EXITOSO("Exitoso"),
  FALLIDO("Fallido"),
  CANCELADO("Cancelado"),
  REEMBOLSADO("Reembolsado");

  private final String descripcion;

  EstadoPago(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }
}