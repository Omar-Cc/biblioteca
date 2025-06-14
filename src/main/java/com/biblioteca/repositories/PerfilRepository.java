package com.biblioteca.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biblioteca.models.acceso.Perfil;
import com.biblioteca.models.acceso.Usuario;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {

  Optional<Perfil> findByIdAndUsuarioUsername(Long id, String username);

  List<Perfil> findByUsuarioUsername(String username);

  long countByUsuarioUsername(String username);

  boolean existsByUsuarioUsernameAndEsPerfilPrincipalTrue(String username);

  Optional<Perfil> findByUsuarioUsernameAndEsPerfilPrincipalTrue(String username);

  List<Perfil> findByUsuarioAndEsPerfilPrincipalTrue(Usuario usuario);

  // Para seleccionar un nuevo perfil principal si se elimina el actual
  Optional<Perfil> findFirstByUsuarioUsernameOrderByFechaCreacionAsc(String username);
}