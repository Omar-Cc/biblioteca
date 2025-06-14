package com.biblioteca.enums;

import lombok.Getter;

@Getter
public enum FormatoFisico {
  TAPA_DURA("Tapa Dura", "Libro con cubierta rígida"),
  TAPA_BLANDA("Tapa Blanda", "Libro con cubierta flexible"),
  RUSTICA("Rústica", "Encuadernación económica"),
  ENCUADERNADO("Encuadernado", "Encuadernación profesional"),
  GRAPA("Grapa", "Sujetado con grapas"),
  ESPIRAL("Espiral", "Encuadernación en espiral"),
  CARPETA("Carpeta", "En carpeta o folder"),
  OTRO("Otro", "Otro formato no especificado");

  private final String nombre;
  private final String descripcion;

  FormatoFisico(String nombre, String descripcion) {
    this.nombre = nombre;
    this.descripcion = descripcion;
  }
}