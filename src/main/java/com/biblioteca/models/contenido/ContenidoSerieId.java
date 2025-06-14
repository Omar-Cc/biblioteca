package com.biblioteca.models.contenido;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContenidoSerieId implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long serieId;
  private Long contenidoId;

}