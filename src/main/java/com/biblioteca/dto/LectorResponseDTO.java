package com.biblioteca.dto;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

@Data
public class LectorResponseDTO {
  // Campos específicos de Lector
  private String nombre;
  private String apellido;
  private String direccion;
  private String telefono;
  private Date fechaNacimiento;
  private Integer multasPendientes;
  private String estadoCuenta;

  // Campos del Usuario asociado
  private String username;
  private String email;

  // Metadatos útiles
  private Date fechaRegistro;
  private LocalDateTime ultimaActividad;
  private boolean activo;
  private Long id;
}