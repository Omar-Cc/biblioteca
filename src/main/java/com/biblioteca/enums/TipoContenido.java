package com.biblioteca.enums;

public enum TipoContenido {
  LIBRO_FISICO("Libro físico"),
  LIBRO_DIGITAL("Libro digital"),
  AUDIO_LIBRO("Audiolibro"),
  REVISTA_FISICA("Revista física"),
  REVISTA_DIGITAL("Revista digital"),
  COMIC_FISICO("Cómic físico"),
  COMIC_DIGITAL("Cómic digital"),
  MANGA_FISICO("Manga físico"),
  MANGA_DIGITAL("Manga digital"),
  MATERIAL_EDUCATIVO_DIGITAL("Material educativo digital"),
  CONTENIDO_MULTIMEDIA_DIGITAL("Contenido multimedia digital");

  private final String descripcion;

  TipoContenido(String descripcion) {
    this.descripcion = descripcion;
  }

  TipoContenido() {
    this.descripcion = name().replace('_', ' ').toLowerCase();
  }

  public String getDescripcion() {
    return descripcion;
  }
}