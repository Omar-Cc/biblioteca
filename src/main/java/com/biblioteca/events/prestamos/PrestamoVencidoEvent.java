package com.biblioteca.events.prestamos;

import org.springframework.context.ApplicationEvent;

import com.biblioteca.models.actividades.Prestamo;

import lombok.Getter;

@Getter
public class PrestamoVencidoEvent extends ApplicationEvent {

  private final Prestamo prestamo;
  private final long diasVencido;

  public PrestamoVencidoEvent(Object source, Prestamo prestamo) {
    super(source);
    this.prestamo = prestamo;
    this.diasVencido = prestamo.getDiasRetraso();
  }
}