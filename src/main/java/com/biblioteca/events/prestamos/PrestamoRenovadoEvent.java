package com.biblioteca.events.prestamos;

import org.springframework.context.ApplicationEvent;

import com.biblioteca.dto.validacion.prestamos.ValidacionRenovacionResult;
import com.biblioteca.models.actividades.Prestamo;

import lombok.Getter;

@Getter
public class PrestamoRenovadoEvent extends ApplicationEvent {

  private final Prestamo prestamo;
  private final ValidacionRenovacionResult validacionRenovacion;
  private final boolean esRenovacionAutomatica;

  public PrestamoRenovadoEvent(Object source, Prestamo prestamo, ValidacionRenovacionResult validacion) {
    super(source);
    this.prestamo = prestamo;
    this.validacionRenovacion = validacion;
    this.esRenovacionAutomatica = "automática".equalsIgnoreCase(validacion.getTipoRenovacion()) ||
        "automatica".equalsIgnoreCase(validacion.getTipoRenovacion());
  }
}