package com.biblioteca.repositories;

import com.biblioteca.models.acceso.InformacionPerfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InformacionPerfilRepository extends JpaRepository<InformacionPerfil, Long> {

  Optional<InformacionPerfil> findByPerfilId(Long perfilId);

  boolean existsByPerfilId(Long perfilId);

  void deleteByPerfilId(Long perfilId);
}