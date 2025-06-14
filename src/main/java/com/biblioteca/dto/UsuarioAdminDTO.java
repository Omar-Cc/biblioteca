package com.biblioteca.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAdminDTO {
  
  // ========== IDENTIFICACIÓN ==========
  private Long id;

  @NotEmpty(message = "El nombre de usuario no puede estar vacío.")
  @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres.")
  private String username;

  @NotEmpty(message = "El correo electrónico no puede estar vacío.")
  @Email(message = "Debe ser una dirección de correo electrónico válida.")
  @Size(max = 100, message = "El correo no debe exceder los 100 caracteres.")
  private String email;

  // ========== SEGURIDAD ==========
  @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres si se provee.")
  private String password; // Solo para creación/actualización

  @NotEmpty(message = "Debe seleccionar al menos un rol.")
  private Set<String> roles;

  // ========== ESTADO DE LA CUENTA ==========
  private Boolean activo;
  private Boolean emailVerificado;
  private Integer intentosFallidosLogin;
  private LocalDateTime fechaBloqueo;

  // ========== CONFIGURACIÓN GLOBAL ==========
  private String idiomaPreferido;
  private String zonaHoraria;
  private Boolean notificacionesGlobalesActivas;
  private Boolean recibirNewsletters;
  private Boolean recibirPromociones;

  // ========== PERÍODOS DE PRUEBA ==========
  private Boolean haUsadoPeriodoPrueba;
  private LocalDateTime fechaPrimerPeriodoPrueba;

  // ========== FECHAS ==========
  private LocalDateTime fechaRegistro;
  private LocalDateTime ultimaActividad;
  private LocalDateTime ultimaActualizacionPassword;

  // ========== INFORMACIÓN DE SUSCRIPCIÓN ==========
  private String planActual;
  private String estadoSuscripcion;
  private LocalDateTime fechaVencimientoSuscripcion;

  // ========== PERFILES ==========
  private Set<PerfilResponseDTO> perfiles;
  private Long perfilPrincipalId;

  // ========== ESTADÍSTICAS ==========
  private Integer totalPerfiles;
  private Integer sesionesActivas;
}