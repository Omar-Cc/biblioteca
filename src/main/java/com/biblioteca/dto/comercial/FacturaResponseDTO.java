package com.biblioteca.dto.comercial;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaResponseDTO {
  private Long id;
  private String numeroFactura;
  private LocalDate fechaEmision;
  private Integer subtotal;
  private Integer impuestos;
  private Integer total;
  private String datosFacturacion;
  private Long ordenId;
}