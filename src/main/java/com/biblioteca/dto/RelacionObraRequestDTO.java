package com.biblioteca.dto;

import com.biblioteca.enums.TipoRelacionObra;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RelacionObraRequestDTO {
  private Long obraRelacionadaId;
  private TipoRelacionObra tipoRelacion;
  private Integer orden;
}