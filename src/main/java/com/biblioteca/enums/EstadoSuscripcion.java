package com.biblioteca.enums;

public enum EstadoSuscripcion {
  ACTIVA("Activa"),
  PRUEBA("Período de Prueba"),
  PENDIENTE_PAGO("Pendiente de Pago"),
  PAUSADA("Pausada"),
  CANCELADA("Cancelada"),
  VENCIDA("Vencida"),
  SUSPENDIDA("Suspendida"), // Por falta de pago
  PENDIENTE_DOWNGRADE("Pendiente de Downgrade"),
  PENDIENTE_CANCELACION("Pendiente de Cancelación"),
  PENDIENTE_UPGRADE("Pendiente de Upgrade");

  private final String descripcion;

  EstadoSuscripcion(String descripcion) {
    this.descripcion = descripcion;
  }
}