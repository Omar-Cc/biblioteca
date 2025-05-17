package com.biblioteca.service.impl;

import com.biblioteca.models.Rol;
import com.biblioteca.service.RolService;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;

@Service
public class RolServiceImpl implements RolService {
  private final Map<Long, Rol> roles = new ConcurrentHashMap<>();
  private final Map<String, Rol> rolesPorNombre = new ConcurrentHashMap<>();
  private final AtomicLong rolIdCounter = new AtomicLong(0); // Iniciar en 0 para el primer ID ser 1

  public static final String ROLE_USER = "ROLE_USER";
  public static final String ROLE_ADMIN = "ROLE_ADMIN";
  public static final String ROLE_LIBRARIAN = "ROLE_LIBRARIAN";
  public static final String ROLE_MEMBER = "ROLE_MEMBER";
  public static final String ROLE_GUEST = "ROLE_GUEST";
  public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
  public static final String ROLE_MODERATOR = "ROLE_MODERATOR";
  public static final String ROLE_EDITOR = "ROLE_EDITOR";
  public static final String ROLE_VIEWER = "ROLE_VIEWER";
  public static final String ROLE_CONTRIBUTOR = "ROLE_CONTRIBUTOR";
  public static final String ROLE_EMPLOYEE = "ROLE_EMPLOYEE";

  @Override
  @PostConstruct
  public void inicializarRoles() {
    if (buscarPorNombre(ROLE_USER).isEmpty()) {
      guardarRol(Rol.builder().nombre(ROLE_USER).build());
    }
    if (buscarPorNombre(ROLE_ADMIN).isEmpty()) {
      guardarRol(Rol.builder().nombre(ROLE_ADMIN).build());
    }
    if (buscarPorNombre(ROLE_LIBRARIAN).isEmpty()) {
      guardarRol(Rol.builder().nombre(ROLE_LIBRARIAN).build());
    }
    if (buscarPorNombre(ROLE_MEMBER).isEmpty()) {
      guardarRol(Rol.builder().nombre(ROLE_MEMBER).build());
    }
    if (buscarPorNombre(ROLE_GUEST).isEmpty()) {
      guardarRol(Rol.builder().nombre(ROLE_GUEST).build());
    }
    if (buscarPorNombre(ROLE_SUPER_ADMIN).isEmpty()) {
      guardarRol(Rol.builder().nombre(ROLE_SUPER_ADMIN).build());
    }
    if (buscarPorNombre(ROLE_MODERATOR).isEmpty()) {
      guardarRol(Rol.builder().nombre(ROLE_MODERATOR).build());
    }
    if (buscarPorNombre(ROLE_EDITOR).isEmpty()) {
      guardarRol(Rol.builder().nombre(ROLE_EDITOR).build());
    }
    if (buscarPorNombre(ROLE_VIEWER).isEmpty()) {
      guardarRol(Rol.builder().nombre(ROLE_VIEWER).build());
    }
    if (buscarPorNombre(ROLE_CONTRIBUTOR).isEmpty()) {
      guardarRol(Rol.builder().nombre(ROLE_CONTRIBUTOR).build());
    }
    if (buscarPorNombre(ROLE_EMPLOYEE).isEmpty()) {
      guardarRol(Rol.builder().nombre(ROLE_EMPLOYEE).build());
    }
    System.out.println("Roles inicializados: " + rolesPorNombre.keySet());
  }

  @Override
  public Rol guardarRol(Rol rol) {
    if (rol.getId() == null) {
      rol.setId(rolIdCounter.incrementAndGet());
    }
    roles.put(rol.getId(), rol);
    rolesPorNombre.put(rol.getNombre(), rol);
    return rol;
  }

  @Override
  public Optional<Rol> buscarPorNombre(String nombre) {
    return Optional.ofNullable(rolesPorNombre.get(nombre));
  }

  @Override
  public List<Rol> obtenerTodosLosRoles() {
    return new ArrayList<>(roles.values());
  }
}