package com.biblioteca.events.prestamos;

import org.springframework.context.ApplicationEvent;

import com.biblioteca.dto.validacion.prestamos.ValidacionLimitesResult;
import com.biblioteca.models.actividades.Prestamo;

import lombok.Getter;

@Getter
public class PrestamoRealizadoEvent extends ApplicationEvent {

  private final Prestamo prestamo;
  private final ValidacionLimitesResult validacionLimites;

  public PrestamoRealizadoEvent(Object source, Prestamo prestamo, ValidacionLimitesResult validacionLimites) {
    super(source);
    this.prestamo = prestamo;
    this.validacionLimites = validacionLimites;
  }
}