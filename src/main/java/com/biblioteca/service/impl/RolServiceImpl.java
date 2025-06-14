package com.biblioteca.service.impl;

import com.biblioteca.models.acceso.Rol;
import com.biblioteca.repositories.RolRepository;
import com.biblioteca.service.RolService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

  private final RolRepository rolRepository;

  public static final String ROLE_ADMIN = "ROLE_ADMIN";
  public static final String ROLE_LECTOR = "ROLE_LECTOR";
  public static final String ROLE_EMPLEADO = "ROLE_EMPLEADO";

  @Override
  @PostConstruct
  @Transactional
  public void inicializarRoles() {
    crearRolSiNoExiste(ROLE_ADMIN);
    crearRolSiNoExiste(ROLE_LECTOR);
    crearRolSiNoExiste(ROLE_EMPLEADO);
    System.out.println("Roles verificados/inicializados en la base de datos.");
  }

  private void crearRolSiNoExiste(String nombreRol) {
    if (rolRepository.findByNombre(nombreRol).isEmpty()) {
      Rol nuevoRol = Rol.builder().nombre(nombreRol).build();
      rolRepository.save(nuevoRol);
      System.out.println("Rol creado: " + nombreRol);
    }
  }

  @Override
  @Transactional
  public Rol guardarRol(Rol rol) {
    // Verificar si ya existe un rol con ese nombre para evitar duplicados si es
    // necesario
    // Aunque la inicialización ya lo maneja, este método podría ser llamado
    // externamente.
    Optional<Rol> existente = rolRepository.findByNombre(rol.getNombre());
    if (existente.isPresent() && (rol.getId() == null || !rol.getId().equals(existente.get().getId()))) {
      throw new IllegalArgumentException("Un rol con el nombre '" + rol.getNombre() + "' ya existe.");
    }
    // El ID será generado por la base de datos si es nuevo, o se usará el existente
    // si se actualiza.
    return rolRepository.save(rol);
  }

  @Override
  @Transactional(readOnly = true) // Las operaciones de solo lectura pueden marcarse así
  public Optional<Rol> buscarPorNombre(String nombre) {
    return rolRepository.findByNombre(nombre);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Rol> obtenerTodosLosRoles() {
    return rolRepository.findAll();
  }
}