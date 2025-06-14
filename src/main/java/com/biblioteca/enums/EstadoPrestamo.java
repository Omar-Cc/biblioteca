package com.biblioteca.enums;

/**
 * Enum que representa los diferentes estados de un préstamo en la biblioteca.
 */
public enum EstadoPrestamo {
  ACTIVO("Préstamo activo"),
  DEVUELTO("Devuelto"),
  VENCIDO("Vencido"),
  RENOVADO("Renovado"),
  PERDIDO("Extraviado"),
  CANCELADO("Cancelado");

  private final String descripcion;

  EstadoPrestamo(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }
}
