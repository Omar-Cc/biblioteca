package com.biblioteca.service.impl;

import com.biblioteca.dto.PerfilRequestDTO;
import com.biblioteca.dto.PerfilResponseDTO;
import com.biblioteca.mapper.PerfilMapper;
import com.biblioteca.models.Perfil;
import com.biblioteca.models.Usuario;
import com.biblioteca.service.PerfilService;
import com.biblioteca.service.UsuarioService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.Map;

@Service
public class PerfilServiceImpl implements PerfilService {

  private final Map<Long, Perfil> perfiles = new ConcurrentHashMap<>();
  private final AtomicLong perfilIdCounter = new AtomicLong(0);
  private final PerfilMapper perfilMapper;
  private final UsuarioService usuarioService;

  public PerfilServiceImpl(PerfilMapper perfilMapper, UsuarioService usuarioService) {
    this.perfilMapper = perfilMapper;
    this.usuarioService = usuarioService;
  }

  @Override
  public PerfilResponseDTO crearPerfil(PerfilRequestDTO perfilDto, String username) {
    Usuario usuario = usuarioService.buscarPorUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

    // Lógica para el primer perfil como principal
    if (contarPerfilesPorUsuario(username) == 0) {
      perfilDto.setEsPerfilPrincipal(true);
    } else if (perfilDto.getEsPerfilPrincipal()) {
      // Si se marca como principal y ya existen otros, desmarcar los demás
      asegurarUnPerfilPrincipal(username, null); // null porque aún no tiene ID
    }

    Perfil perfil = perfilMapper.perfilRequestDTOToPerfil(perfilDto, usuario);
    perfil.setId(perfilIdCounter.incrementAndGet());

    // Inicializar fechas al crear el perfil
    LocalDate ahora = LocalDate.now();
    perfil.setFechaCreacion(ahora);
    perfil.setFechaModificacion(ahora);
    perfil.setUltimaActividad(LocalDateTime.now());

    perfiles.put(perfil.getId(), perfil);
    usuario.addPerfil(perfil); // Mantener la relación bidireccional

    if (perfil.isEsPerfilPrincipal()) { // Si este nuevo es principal, asegurar que los otros no lo sean
      asegurarUnPerfilPrincipal(username, perfil.getId());
    }

    return perfilMapper.perfilToPerfilResponseDTO(perfil);
  }

  @Override
  public Optional<PerfilResponseDTO> obtenerPerfilPorIdYUsuario(Long id, String username) {
    return perfiles.values().stream()
        .filter(p -> p.getId().equals(id) && p.getUsuario().getUsername().equals(username))
        .map(perfilMapper::perfilToPerfilResponseDTO)
        .findFirst();
  }

  @Override
  public List<PerfilResponseDTO> obtenerPerfilesPorUsuario(String username) {
    return perfiles.values().stream()
        .filter(p -> p.getUsuario().getUsername().equals(username))
        .map(perfilMapper::perfilToPerfilResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<Perfil> obtenerPerfilPorId(Long id) {
    return Optional.ofNullable(perfiles.get(id));
  }

  @Override
  public Optional<PerfilResponseDTO> actualizarPerfil(Long id, PerfilRequestDTO perfilDto, String username) {
    Optional<Perfil> perfilOpt = perfiles.values().stream()
        .filter(p -> p.getId().equals(id) && p.getUsuario().getUsername().equals(username))
        .findFirst();

    if (perfilOpt.isPresent()) {
      Perfil perfil = perfilOpt.get();

      // Si se está marcando este perfil como principal, desmarcar los otros
      if (perfilDto.getEsPerfilPrincipal() && !perfil.isEsPerfilPrincipal()) {
        asegurarUnPerfilPrincipal(username, id);
      } else if (!perfilDto.getEsPerfilPrincipal() && perfil.isEsPerfilPrincipal()
          && contarPerfilesPorUsuario(username) == 1) {
        // No permitir desmarcar el único perfil como principal
        throw new IllegalArgumentException("No se puede desmarcar el único perfil como no principal.");
      }

      perfilMapper.updatePerfilFromDto(perfilDto, perfil);
      // Asegurar que el cambio de esPerfilPrincipal se refleje
      perfil.setEsPerfilPrincipal(perfilDto.getEsPerfilPrincipal());

      // Actualizar la fecha de modificación
      perfil.setFechaModificacion(LocalDate.now());

      perfiles.put(id, perfil);
      return Optional.of(perfilMapper.perfilToPerfilResponseDTO(perfil));
    }
    return Optional.empty();
  }

  @Override
  public boolean eliminarPerfil(Long id, String username) {
    Optional<Perfil> perfilOpt = perfiles.values().stream()
        .filter(p -> p.getId().equals(id) && p.getUsuario().getUsername().equals(username))
        .findFirst();

    if (perfilOpt.isPresent()) {
      Perfil perfilAEliminar = perfilOpt.get();
      if (perfilAEliminar.isEsPerfilPrincipal() && contarPerfilesPorUsuario(username) <= 1) {
        throw new IllegalArgumentException("No se puede eliminar el único perfil principal.");
      }

      Usuario usuario = perfilAEliminar.getUsuario();
      if (usuario != null) {
        usuario.removePerfil(perfilAEliminar);
      }
      perfiles.remove(id);

      // Si se eliminó el perfil principal y quedan otros, asignar uno nuevo como
      // principal
      if (perfilAEliminar.isEsPerfilPrincipal() && contarPerfilesPorUsuario(username) > 0) {
        perfiles.values().stream()
            .filter(p -> p.getUsuario().getUsername().equals(username))
            .findFirst()
            .ifPresent(p -> p.setEsPerfilPrincipal(true));
      }
      return true;
    }
    return false;
  }

  @Override
  public long contarPerfilesPorUsuario(String username) {
    return perfiles.values().stream()
        .filter(p -> p.getUsuario().getUsername().equals(username))
        .count();
  }

  @Override
  public void asegurarUnPerfilPrincipal(String username, Long perfilIdActualComoPrincipal) {
    perfiles.values().stream()
        .filter(p -> p.getUsuario().getUsername().equals(username))
        .forEach(p -> {
          // Al crear el primero, o si se desmarca uno sin nuevo principal explícito
          if (perfilIdActualComoPrincipal == null) {
            p.setEsPerfilPrincipal(false); // Desmarcar todos si no hay uno específico
          } else {
            p.setEsPerfilPrincipal(p.getId().equals(perfilIdActualComoPrincipal));
          }
        });
  }

  @Override
  public boolean existePerfilPrincipal(String username) {
    return perfiles.values().stream()
        .filter(p -> p.getUsuario().getUsername().equals(username))
        .anyMatch(Perfil::isEsPerfilPrincipal);
  }

  @Override
  public Optional<PerfilResponseDTO> obtenerPerfilPrincipal(String username) {
    return this.obtenerPerfilesPorUsuario(username).stream()
        .filter(PerfilResponseDTO::isEsPerfilPrincipal)
        .findFirst();
  }

  // Método adicional para actualizar la fecha de última actividad de un perfil
  @Override
  public void actualizarFechaUltimoActividad(Long id) {
    perfiles.computeIfPresent(id, (key, perfil) -> {
      perfil.setUltimaActividad(LocalDateTime.now());
      return perfil;
    });
  }
}