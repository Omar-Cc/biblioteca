package com.biblioteca.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticaEstadoDTO {
  private String estado;
  private Long total;
  private Double porcentaje;
}
