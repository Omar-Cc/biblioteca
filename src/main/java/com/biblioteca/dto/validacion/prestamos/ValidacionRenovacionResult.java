package com.biblioteca.dto.validacion.prestamos;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionRenovacionResult {

  // ========== ESTADO DE VALIDACIÓN ==========
  private Boolean puedeRenovar;
  private Boolean necesitaUpgradePlan;

  // ========== INFORMACIÓN DE RENOVACIÓN ==========
  private Integer diasExtensionPermitidos;
  private Integer diasExtensionSolicitados;
  private String tipoRenovacion;
  private Integer renovacionesUsadas;
  private Integer maxRenovaciones;

  // ========== INFORMACIÓN DE PLAN ==========
  private String planActual;
  private String planRecomendado;
  private Long planRecomendadoId;

  // ========== MENSAJES ==========
  private String mensaje;
  private String observaciones;
  private String razonRechazo;

  // ========== FECHAS ==========
  private java.time.LocalDate fechaVencimientoActual;
  private java.time.LocalDate nuevaFechaVencimiento;

  // ========== MÉTODOS HELPER ==========

  public Boolean esRenovacionParcial() {
    return Boolean.TRUE.equals(puedeRenovar) &&
        diasExtensionPermitidos != null &&
        diasExtensionSolicitados != null &&
        diasExtensionPermitidos < diasExtensionSolicitados;
  }

  public Boolean esRenovacionCompleta() {
    return Boolean.TRUE.equals(puedeRenovar) &&
        diasExtensionPermitidos != null &&
        diasExtensionSolicitados != null &&
        diasExtensionPermitidos >= diasExtensionSolicitados;
  }

  public Boolean agotaronRenovaciones() {
    return Boolean.FALSE.equals(puedeRenovar) &&
        renovacionesUsadas != null &&
        maxRenovaciones != null &&
        renovacionesUsadas >= maxRenovaciones;
  }

  public String getTipoResultado() {
    if (!Boolean.TRUE.equals(puedeRenovar)) {
      return "RECHAZADA";
    }
    if (Boolean.TRUE.equals(esRenovacionParcial())) {
      return "PARCIAL";
    }
    return "COMPLETA";
  }

  // ========== MÉTODOS ADICIONALES PARA MANEJO DE NULOS ==========

  public Boolean isPuedeRenovarSafe() {
    return Boolean.TRUE.equals(puedeRenovar);
  }

  public Boolean isNecesitaUpgradePlanSafe() {
    return Boolean.TRUE.equals(necesitaUpgradePlan);
  }

  public Integer getDiasExtensionPermitidosSafe() {
    return diasExtensionPermitidos != null ? diasExtensionPermitidos : 0;
  }

  public Integer getDiasExtensionSolicitadosSafe() {
    return diasExtensionSolicitados != null ? diasExtensionSolicitados : 0;
  }

  public Integer getRenovacionesUsadasSafe() {
    return renovacionesUsadas != null ? renovacionesUsadas : 0;
  }

  public Integer getMaxRenovacionesSafe() {
    return maxRenovaciones != null ? maxRenovaciones : 0;
  }
}