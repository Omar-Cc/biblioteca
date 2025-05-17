package com.biblioteca.dto.comercial;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaRequestDTO {
  @NotBlank(message = "El número de factura es obligatorio")
  private String numeroFactura;

  @NotNull(message = "El subtotal es obligatorio")
  @Min(value = 0, message = "El subtotal debe ser mayor o igual a 0")
  private Integer subtotal;

  @NotNull(message = "Los impuestos son obligatorios")
  @Min(value = 0, message = "Los impuestos deben ser mayor o igual a 0")
  private Integer impuestos;

  @NotNull(message = "El total es obligatorio")
  @Min(value = 0, message = "El total debe ser mayor o igual a 0")
  private Integer total;

  private String datosFacturacion;

  @NotNull(message = "El ID de la orden es obligatorio")
  private Long ordenId;
}