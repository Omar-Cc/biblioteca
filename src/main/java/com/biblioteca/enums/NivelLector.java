package com.biblioteca.enums;

import lombok.Getter;

@Getter
public enum NivelLector {
  PRINCIPIANTE("Principiante", "Menos de 10 libros leídos", 0, 10),
  INTERMEDIO("Intermedio", "Entre 10 y 50 libros leídos", 10, 50),
  AVANZADO("Avanzado", "Entre 50 y 150 libros leídos", 50, 150),
  EXPERTO("Experto", "Entre 150 y 500 libros leídos", 150, 500),
  MAESTRO("Maestro", "Más de 500 libros leídos", 500, Integer.MAX_VALUE);

  private final String nombre;
  private final String descripcion;
  private final int librosMinimos;
  private final int librosMaximos;

  NivelLector(String nombre, String descripcion, int librosMinimos, int librosMaximos) {
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.librosMinimos = librosMinimos;
    this.librosMaximos = librosMaximos;
  }

  public static NivelLector calcularNivel(int librosLeidos) {
    for (NivelLector nivel : values()) {
      if (librosLeidos >= nivel.librosMinimos && librosLeidos < nivel.librosMaximos) {
        return nivel;
      }
    }
    return MAESTRO;
  }

  public boolean puedeAccederContenidoAvanzado() {
    return this.ordinal() >= AVANZADO.ordinal();
  }
}