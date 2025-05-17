package com.biblioteca.models;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(exclude = { "perfiles", "roles" })
@ToString(exclude = { "perfiles", "roles" })
public class Usuario {
  private Long id;
  private String username;
  private String password; // Hasheada
  private LocalDateTime ultimaActualizacionPassword;
  private String email;
  @Builder.Default
  private boolean activo = true;
  private LocalDateTime fechaRegistro;
  private LocalDateTime ultimaActividad;
  private Long planId;

  @Builder.Default
  private Set<Rol> roles = new HashSet<>();
  @Builder.Default
  private Set<Perfil> perfiles = new HashSet<>();
  // private Perfil perfilPrincipal; // Para desarrollo futuro según Bloque 2

  public void addPerfil(Perfil perfil) {
    this.perfiles.add(perfil);
    perfil.setUsuario(this);
  }

  public void removePerfil(Perfil perfil) {
    this.perfiles.remove(perfil);
    perfil.setUsuario(null);
  }
}