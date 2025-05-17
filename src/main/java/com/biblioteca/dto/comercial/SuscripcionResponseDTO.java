package com.biblioteca.dto.comercial;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuscripcionResponseDTO {
  private Long id;
  private Long usuarioId;
  private Long planId;
  private LocalDate fechaInicio;
  private LocalDate fechaRenovacion;
  private String estado;
  private Long metodoPagoId;

  // Información adicional útil
  private String usuarioNombre;
  private String planNombre;
  private String metodoPagoNombre;

  @Builder.Default
  private Set<HistorialPagoSuscripcionResponseDTO> historialPagos = new HashSet<>();
}