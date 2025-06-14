package com.biblioteca.events.prestamos;

import org.springframework.context.ApplicationEvent;

import com.biblioteca.models.actividades.Prestamo;

import lombok.Getter;

@Getter
public class PrestamoDevueltoEvent extends ApplicationEvent {

  private final Prestamo prestamo;
  private final boolean fueDevueltoATiempo;
  private final long diasRetraso;
  private final long diasAnticipacion;

  public PrestamoDevueltoEvent(Object source, Prestamo prestamo) {
    super(source);
    this.prestamo = prestamo;
    this.fueDevueltoATiempo = prestamo.getFechaDevolucionReal() != null &&
        !prestamo.getFechaDevolucionReal().isAfter(prestamo.getFechaDevolucionPrevista());
    this.diasRetraso = prestamo.getDiasRetraso();
    this.diasAnticipacion = calcularDiasAnticipacion();
  }

  private long calcularDiasAnticipacion() {
    if (prestamo.getFechaDevolucionReal() != null &&
        prestamo.getFechaDevolucionReal().isBefore(prestamo.getFechaDevolucionPrevista())) {
      return prestamo.getFechaDevolucionPrevista().toEpochDay() -
          prestamo.getFechaDevolucionReal().toEpochDay();
    }
    return 0;
  }
}