package com.biblioteca.dto.comercial;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
  private String estado; // ✅ AGREGAR: Campo faltante usado en vistas
  private String datosFacturacion;
  private String motivoAnulacion; // ✅ AGREGAR: Para facturas anuladas

  // Datos de la orden relacionada
  private Long ordenId;
  private LocalDateTime fechaOrden; // ✅ AGREGAR: Fecha de la orden
  private String estadoOrden; // ✅ AGREGAR: Estado de la orden

  // Datos del cliente (para mostrar en vistas)
  private String clienteNombre; // ✅ AGREGAR: Nombre del cliente
  private String clienteEmail; // ✅ AGREGAR: Email del cliente
  private Long perfilId; // ✅ AGREGAR: ID del perfil para validaciones
}