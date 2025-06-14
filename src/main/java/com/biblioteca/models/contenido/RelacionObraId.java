package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelacionObraId implements Serializable {
  private static final long serialVersionUID = 1L;
  private Long obraOrigenId;
  private Long obraRelacionadaId;
}