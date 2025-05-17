package com.biblioteca.dto;

import java.time.LocalDate;
import java.util.Date;
import lombok.Data;

@Data
public class LectorResponseDTO {
  // Campos de identificación (de Usuario)
  private Long id;
  private String username;
  private String email;
  private boolean activo;

  // Campos específicos de Lector
  private String nombre;
  private String apellido;
  private String direccion;
  private String telefono;
  private Date fechaNacimiento;
  private Integer multasPendientes;
  private String estadoCuenta;

  // Metadatos útiles
  private Date fechaRegistro;
  private LocalDate ultimaActividad;
}