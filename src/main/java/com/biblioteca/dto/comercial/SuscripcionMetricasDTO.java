package com.biblioteca.dto.comercial;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuscripcionMetricasDTO {
  private long totalSuscripcionesActivas;
  private double ingresosMensual;
  private double tasaConversionPrueba;
  private String planMasPopular;
  private double churnRate;
  private long totalUsuarios;
  private long usuariosConSuscripcionActiva;
}