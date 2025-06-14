package com.biblioteca.enums;

import lombok.Getter;

@Getter
public enum TipoUsuario {
  LECTOR("Lector", "Usuario regular que lee y consume contenido"),
  EMPLEADO("Empleado", "Personal de la biblioteca"),
  ADMINISTRADOR("Administrador", "Administrador del sistema"),
  MODERADOR("Moderador", "Moderador de contenido y comunidad"),
  AUTOR("Autor", "Autor registrado en la plataforma"),
  EDITOR("Editor", "Editor o publisher");

  private final String nombre;
  private final String descripcion;

  TipoUsuario(String nombre, String descripcion) {
    this.nombre = nombre;
    this.descripcion = descripcion;
  }

  public boolean esStaff() {
    return this == EMPLEADO || this == ADMINISTRADOR || this == MODERADOR;
  }

  public boolean puedeModerar() {
    return this == ADMINISTRADOR || this == MODERADOR;
  }
}