package com.biblioteca.dto.validacion.prestamos;

import com.biblioteca.enums.EstadoPrestamo;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionPrestamoResult {

  // ========== VALIDACIÓN GENERAL ==========
  private Boolean esValido;
  private List<String> errores;
  private List<String> advertencias;

  // ========== INFORMACIÓN DEL PRÉSTAMO ==========
  private Long prestamoId;
  private Long perfilId;
  private Long contenidoId;
  private EstadoPrestamo estadoActual;
  private EstadoPrestamo estadoSugerido;

  // ========== FECHAS ==========
  private LocalDate fechaPrestamo;
  private LocalDate fechaDevolucionPrevista;
  private LocalDate fechaDevolucionSugerida;

  // ========== VALIDACIONES ESPECÍFICAS ==========
  private ValidacionLimitesResult validacionLimites;
  private ValidacionRenovacionResult validacionRenovacion;

  // ========== INFORMACIÓN CONTEXTUAL ==========
  private String contextoOperacion; // "CREAR", "RENOVAR", "DEVOLVER", etc.
  private Boolean requiereConfirmacionUsuario;
  private String mensajeConfirmacion;

  // ========== MÉTODOS HELPER ==========

  public Boolean tieneErrores() {
    return errores != null && !errores.isEmpty();
  }

  public Boolean tieneAdvertencias() {
    return advertencias != null && !advertencias.isEmpty();
  }

  public void agregarError(String error) {
    if (errores == null) {
      errores = new ArrayList<>();
    }
    errores.add(error);
    esValido = false;
  }

  public void agregarAdvertencia(String advertencia) {
    if (advertencias == null) {
      advertencias = new ArrayList<>();
    }
    advertencias.add(advertencia);
  }

  // ========== MÉTODOS ADICIONALES PARA MANEJO DE NULOS ==========

  public Boolean isEsValidoSafe() {
    return Boolean.TRUE.equals(esValido);
  }

  public Boolean isRequiereConfirmacionUsuarioSafe() {
    return Boolean.TRUE.equals(requiereConfirmacionUsuario);
  }

  public List<String> getErroresSafe() {
    return errores != null ? errores : new ArrayList<>();
  }

  public List<String> getAdvertenciasSafe() {
    return advertencias != null ? advertencias : new ArrayList<>();
  }

  // ========== MÉTODOS DE ANÁLISIS ==========

  public Integer getTotalErrores() {
    return getErroresSafe().size();
  }

  public Integer getTotalAdvertencias() {
    return getAdvertenciasSafe().size();
  }

  public Boolean esOperacionSegura() {
    return Boolean.TRUE.equals(esValido) && !tieneErrores();
  }

  public Boolean requiereRevision() {
    return tieneAdvertencias() || Boolean.TRUE.equals(requiereConfirmacionUsuario);
  }

  public String getNivelValidacion() {
    if (!Boolean.TRUE.equals(esValido) || tieneErrores()) {
      return "ERROR";
    }
    if (tieneAdvertencias()) {
      return "ADVERTENCIA";
    }
    if (Boolean.TRUE.equals(requiereConfirmacionUsuario)) {
      return "CONFIRMACION";
    }
    return "APROBADO";
  }

  // ========== MÉTODOS DE VALIDACIÓN ESPECÍFICA ==========

  public Boolean puedeRealizarOperacion() {
    return Boolean.TRUE.equals(esValido) &&
        !tieneErrores() &&
        (validacionLimites == null || Boolean.TRUE.equals(validacionLimites.isPuedePrestarSafe()));
  }

  public Boolean necesitaUpgradePlan() {
    return (validacionLimites != null && Boolean.TRUE.equals(validacionLimites.isNecesitaUpgradePlanSafe())) ||
        (validacionRenovacion != null && Boolean.TRUE.equals(validacionRenovacion.isNecesitaUpgradePlanSafe()));
  }

  public Boolean puedeAumentarLimitePerfil() {
    return validacionLimites != null && Boolean.TRUE.equals(validacionLimites.isPuedeAumentarLimitePerfilSafe());
  }

  // ========== MÉTODOS DE INFORMACIÓN CONTEXTUAL ==========

  public Boolean esOperacionCreacion() {
    return "CREAR".equalsIgnoreCase(contextoOperacion);
  }

  public Boolean esOperacionRenovacion() {
    return "RENOVAR".equalsIgnoreCase(contextoOperacion);
  }

  public Boolean esOperacionDevolucion() {
    return "DEVOLVER".equalsIgnoreCase(contextoOperacion);
  }

  public Boolean esOperacionCancelacion() {
    return "CANCELAR".equalsIgnoreCase(contextoOperacion);
  }

  // ========== MÉTODOS DE RESUMEN ==========

  public String getResumenValidacion() {
    StringBuilder resumen = new StringBuilder();

    resumen.append("Operación: ").append(contextoOperacion != null ? contextoOperacion : "DESCONOCIDA");
    resumen.append(" - Estado: ").append(getNivelValidacion());

    if (tieneErrores()) {
      resumen.append(" - Errores: ").append(getTotalErrores());
    }

    if (tieneAdvertencias()) {
      resumen.append(" - Advertencias: ").append(getTotalAdvertencias());
    }

    return resumen.toString();
  }

  public String getMensajeCompleto() {
    StringBuilder mensaje = new StringBuilder();

    if (mensajeConfirmacion != null) {
      mensaje.append(mensajeConfirmacion);
    }

    if (validacionLimites != null && validacionLimites.getMensaje() != null) {
      if (mensaje.length() > 0)
        mensaje.append(" ");
      mensaje.append(validacionLimites.getMensaje());
    }

    if (validacionRenovacion != null && validacionRenovacion.getMensaje() != null) {
      if (mensaje.length() > 0)
        mensaje.append(" ");
      mensaje.append(validacionRenovacion.getMensaje());
    }

    return mensaje.toString();
  }

  // ========== MÉTODOS DE FACTORY PARA CASOS COMUNES ==========

  public static ValidacionPrestamoResult exito(String contexto) {
    return ValidacionPrestamoResult.builder()
        .esValido(true)
        .contextoOperacion(contexto)
        .requiereConfirmacionUsuario(false)
        .errores(new ArrayList<>())
        .advertencias(new ArrayList<>())
        .build();
  }

  public static ValidacionPrestamoResult error(String contexto, String mensajeError) {
    ValidacionPrestamoResult result = ValidacionPrestamoResult.builder()
        .esValido(false)
        .contextoOperacion(contexto)
        .requiereConfirmacionUsuario(false)
        .errores(new ArrayList<>())
        .advertencias(new ArrayList<>())
        .build();

    result.agregarError(mensajeError);
    return result;
  }

  public static ValidacionPrestamoResult advertencia(String contexto, String mensajeAdvertencia) {
    ValidacionPrestamoResult result = ValidacionPrestamoResult.builder()
        .esValido(true)
        .contextoOperacion(contexto)
        .requiereConfirmacionUsuario(false)
        .errores(new ArrayList<>())
        .advertencias(new ArrayList<>())
        .build();

    result.agregarAdvertencia(mensajeAdvertencia);
    return result;
  }

  public static ValidacionPrestamoResult confirmacion(String contexto, String mensajeConfirmacion) {
    return ValidacionPrestamoResult.builder()
        .esValido(true)
        .contextoOperacion(contexto)
        .requiereConfirmacionUsuario(true)
        .mensajeConfirmacion(mensajeConfirmacion)
        .errores(new ArrayList<>())
        .advertencias(new ArrayList<>())
        .build();
  }
}