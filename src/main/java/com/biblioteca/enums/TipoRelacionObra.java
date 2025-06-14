package com.biblioteca.enums;

import lombok.Getter;

@Getter
public enum TipoRelacionObra {
  // Relaciones temporales/cronológicas
  SECUELA("Secuela", "Obra que continúa la historia después de otra", true),
  PRECUELA("Precuela", "Obra que cuenta eventos anteriores a otra", true),
  CONTINUACION("Continuación", "Obra que continúa directamente donde terminó otra", true),

  // Relaciones de expansión del universo
  SPIN_OFF("Spin-off", "Obra derivada que se centra en personajes secundarios", false),
  UNIVERSO_COMPARTIDO("Universo Compartido", "Obras que comparten el mismo mundo ficticio", false),
  SERIE_RELACIONADA("Serie Relacionada", "Parte de una serie o saga", false),

  // Relaciones de adaptación y transformación
  ADAPTACION("Adaptación", "Obra basada en otra de diferente medio", false),
  REMAKE("Remake", "Nueva versión de una obra existente", false),
  REBOOT("Reboot", "Reinicio de una serie o franquicia", false),

  // Relaciones de inspiración y referencia
  INSPIRADO_EN("Inspirado en", "Obra libremente basada en otra", false),
  HOMENAJE("Homenaje", "Obra creada para honrar a otra", false),
  PARODIA("Parodia", "Obra que imita con fines cómicos", false),

  // Relaciones editoriales
  EDICION_ESPECIAL("Edición Especial", "Versión especial de la obra original", false),
  EDICION_REVISADA("Edición Revisada", "Versión corregida o actualizada", false),
  COMPILACION("Compilación", "Colección de obras relacionadas", false),

  // Relaciones de traducción y localización
  TRADUCCION("Traducción", "Versión en otro idioma", false),
  ADAPTACION_CULTURAL("Adaptación Cultural", "Versión adaptada a otra cultura", false),

  // Relaciones académicas y profesionales
  COMENTARIO("Comentario", "Obra que analiza o comenta otra", false),
  CRITICA("Crítica", "Análisis crítico de la obra", false),
  ESTUDIO("Estudio", "Investigación académica sobre la obra", false),

  // Relaciones comerciales
  MERCHANDISING("Merchandising", "Producto comercial relacionado", false),
  CROSSOVER("Crossover", "Obra que combina elementos de múltiples universos", false);

  private final String nombre;
  private final String descripcion;
  private final boolean requiereOrdenCronologico;

  TipoRelacionObra(String nombre, String descripcion, boolean requiereOrdenCronologico) {
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.requiereOrdenCronologico = requiereOrdenCronologico;
  }

  /**
   * Determina si esta relación es bidireccional por naturaleza
   */
  public boolean esBidireccional() {
    return this == UNIVERSO_COMPARTIDO ||
        this == SERIE_RELACIONADA ||
        this == CROSSOVER;
  }

  /**
   * Obtiene el tipo de relación inversa si existe
   */
  public TipoRelacionObra getRelacionInversa() {
    return switch (this) {
      case SECUELA -> PRECUELA;
      case PRECUELA -> SECUELA;
      case ADAPTACION -> INSPIRADO_EN;
      case INSPIRADO_EN -> ADAPTACION;
      default -> null;
    };
  }

  /**
   * Determina si este tipo de relación permite múltiples obras relacionadas
   */
  public boolean permiteMultiplesRelaciones() {
    return this != REMAKE &&
        this != EDICION_ESPECIAL &&
        this != EDICION_REVISADA &&
        this != TRADUCCION;
  }
}