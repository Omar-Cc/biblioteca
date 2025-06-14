package com.biblioteca.enums;

import lombok.Getter;

@Getter
public enum TipoLicenciaDigital {
  USUARIO_UNICO("Usuario Único", "Licencia para un solo usuario"),
  MULTIUSUARIO("Multiusuario", "Licencia para múltiples usuarios"),
  PERPETUA("Perpetua", "Licencia permanente"),
  SUSCRIPCION("Suscripción", "Licencia por tiempo determinado"),
  POR_USO("Por Uso", "Licencia por cada uso"),
  LIMITADA_PRESTAMOS("Limitada por Préstamos", "Número limitado de préstamos"),
  ACCESO_ABIERTO("Acceso Abierto", "Acceso libre y gratuito"),
  INSTITUCIONAL("Institucional", "Licencia para institución"),
  ACADEMICA("Académica", "Licencia para uso académico");

  private final String nombre;
  private final String descripcion;

  TipoLicenciaDigital(String nombre, String descripcion) {
    this.nombre = nombre;
    this.descripcion = descripcion;
  }

  public boolean permiteMultiplesUsuarios() {
    return this == MULTIUSUARIO ||
        this == INSTITUCIONAL ||
        this == ACADEMICA ||
        this == ACCESO_ABIERTO;
  }

  public boolean esGratuito() {
    return this == ACCESO_ABIERTO;
  }
}