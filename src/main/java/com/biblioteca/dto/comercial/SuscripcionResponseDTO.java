package com.biblioteca.dto.comercial;

import java.math.BigDecimal;
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
  private String modalidadPago; // "mensual" o "anual"
  private Long metodoPagoId;

    // Información adicional útil para mostrar en la vista
  private String usuarioNombre;
  private String emailUsuario;
  private String planNombre;
  private String metodoPagoNombre;
  private Integer precio; // Precio del plan según la modalidad
  private Integer precioMensual; // Precio mensual del plan en centavos
  private Integer precioAnual; // Precio anual del plan en centavos (si aplica)
  
  // Propiedades calculadas para mostrar progreso de suscripción
  private Integer diasTranscurridos;
  private Integer diasTotales;
  private Integer diasRestantes;
  private Integer diasParaRenovacion;
  
  // Información adicional del usuario
  private String nombreCompleto;
  private String telefonoUsuario;
  
  // Información adicional del plan
  private String descripcionPlan;
  private Integer limiteLibros;
  private Integer diasPrueba;
  
  // Estados y fechas adicionales
  private String estadoAnterior;
  private String motivoCambio;

  @Builder.Default
  private Set<HistorialPagoSuscripcionResponseDTO> historialPagos = new HashSet<>();
}