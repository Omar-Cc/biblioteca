package com.biblioteca.dto;

import com.biblioteca.enums.TipoRelacionObra;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RelacionObraResponseDTO {
  private Long obraOrigenId;
  private Long obraRelacionadaId;
  private String tituloObraRelacionada;
  private TipoRelacionObra tipoRelacion;
  private Integer orden;
}