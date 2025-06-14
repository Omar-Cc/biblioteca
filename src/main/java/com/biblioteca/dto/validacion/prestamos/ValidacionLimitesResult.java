package com.biblioteca.dto.validacion.prestamos;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionLimitesResult {

  // ========== ESTADO DE VALIDACIÓN ==========
  private Boolean puedePrestar;
  private Boolean puedeAumentarLimitePerfil;
  private Boolean necesitaUpgradePlan;

  // ========== INFORMACIÓN DE LÍMITES ==========
  private Integer limiteActual;
  private Integer limiteMaximo;
  private Integer prestamosActivos;

  // ========== INFORMACIÓN DE PLAN ==========
  private String planActual;
  private String planRecomendado;
  private Long planRecomendadoId;

  // ========== MENSAJES Y DESCRIPCIÓN ==========
  private String mensaje;
  private String descripcionDetallada;
  private String accionRecomendada;

  // ========== INFORMACIÓN ADICIONAL ==========
  private Boolean esPerfilInfantil;
  private String tipoUsuario;
  private Integer diasRestantesSuscripcion;

  // ========== MÉTODOS HELPER ==========

  public Boolean requiereAccion() {
    return !Boolean.TRUE.equals(puedePrestar);
  }

  public Boolean puedeResolverSinUpgrade() {
    return Boolean.TRUE.equals(puedeAumentarLimitePerfil) &&
        !Boolean.TRUE.equals(necesitaUpgradePlan);
  }

  public String getTipoAccionRecomendada() {
    if (Boolean.TRUE.equals(puedePrestar)) {
      return "NINGUNA";
    }
    if (Boolean.TRUE.equals(puedeAumentarLimitePerfil)) {
      return "AUMENTAR_LIMITE_PERFIL";
    }
    if (Boolean.TRUE.equals(necesitaUpgradePlan)) {
      return "UPGRADE_PLAN";
    }
    return "DEVOLVER_CONTENIDO";
  }

  // ========== MÉTODOS ADICIONALES PARA MANEJO DE NULOS ==========

  public Boolean isPuedePrestarSafe() {
    return Boolean.TRUE.equals(puedePrestar);
  }

  public Boolean isPuedeAumentarLimitePerfilSafe() {
    return Boolean.TRUE.equals(puedeAumentarLimitePerfil);
  }

  public Boolean isNecesitaUpgradePlanSafe() {
    return Boolean.TRUE.equals(necesitaUpgradePlan);
  }

  public Boolean isEsPerfilInfantilSafe() {
    return Boolean.TRUE.equals(esPerfilInfantil);
  }

  public Integer getLimiteActualSafe() {
    return limiteActual != null ? limiteActual : 0;
  }

  public Integer getLimiteMaximoSafe() {
    return limiteMaximo != null ? limiteMaximo : 0;
  }

  public Integer getPrestamosActivosSafe() {
    return prestamosActivos != null ? prestamosActivos : 0;
  }

  public Integer getDiasRestantesSuscripcionSafe() {
    return diasRestantesSuscripcion != null ? diasRestantesSuscripcion : 0;
  }

  // ========== MÉTODOS DE ANÁLISIS ==========

  public Integer getPrestamosDisponibles() {
    return Math.max(0, getLimiteActualSafe() - getPrestamosActivosSafe());
  }

  public Double getPorcentajeUsoLimite() {
    if (getLimiteActualSafe() == 0)
      return 0.0;
    return (double) getPrestamosActivosSafe() / getLimiteActualSafe() * 100.0;
  }

  public Boolean estaProximoAlLimite() {
    return getPorcentajeUsoLimite() >= 80.0;
  }

  public Boolean alcanzoPorcentajeLimite(double porcentaje) {
    return getPorcentajeUsoLimite() >= porcentaje;
  }
}