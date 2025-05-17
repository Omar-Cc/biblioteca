package com.biblioteca.models;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SesionUsuario {
  private String id; // Podría ser el ID de sesión de Spring Security
  private Usuario usuario;
  private Perfil perfil;
  private String tokenSesion; // Opcional si se usan tokens JWT, etc.
  private Date fechaInicio;
  private Date fechaExpiracion;
}