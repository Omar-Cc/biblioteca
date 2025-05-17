package com.biblioteca.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.biblioteca.dto.LectorRequestDTO;
import com.biblioteca.dto.LectorResponseDTO;
import com.biblioteca.mapper.LectorMapper;
import com.biblioteca.models.Lector;
import com.biblioteca.models.Usuario;
import com.biblioteca.service.LectorService;
import com.biblioteca.service.UsuarioService;

@Service
public class LectorServiceImpl implements LectorService {

  private final Map<String, Lector> lectoresPorUsername = new ConcurrentHashMap<>();
  private final UsuarioService usuarioService;
  private final LectorMapper lectorMapper;
  private final ZoneId limaZone;

  public LectorServiceImpl(UsuarioService usuarioService, LectorMapper lectorMapper) {
    this.usuarioService = usuarioService;
    this.lectorMapper = lectorMapper;
    this.limaZone = ZoneId.of("America/Lima");
  }

  @Override
  public boolean tienePerfilLector(String username) {
    return lectoresPorUsername.containsKey(username);
  }

  @Override
  public LectorResponseDTO crearPerfilLector(String username, LectorRequestDTO lectorDTO) {
    // Verificar si ya existe un perfil de lector
    if (tienePerfilLector(username)) {
      throw new IllegalArgumentException("Ya existe un perfil de lector para el usuario: " + username);
    }

    // Obtener el usuario base
    Usuario usuario = usuarioService.buscarPorUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

    // Crear nuevo lector con datos del usuario y el DTO
    Lector lector = lectorMapper.usuarioYDtoToLector(usuario, lectorDTO);

    // Actualizar la fecha de última actividad
    lector.setUltimaActividad(LocalDateTime.now(limaZone));

    // Guardar en el mapa
    lectoresPorUsername.put(username, lector);

    // Actualizar el repositorio de usuarios también (ya que Lector es un Usuario)
    actualizarUsuario(lector);

    return lectorMapper.lectorToLectorResponseDTO(lector);
  }

  @Override
  public LectorResponseDTO actualizarPerfilLector(String username, LectorRequestDTO lectorDTO) {
    Lector lector = obtenerEntidadLectorPorUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("No existe un perfil de lector para el usuario: " + username));

    // Actualizar los campos del lector con los del DTO
    lectorMapper.updateLectorFromDto(lectorDTO, lector);

    // Actualizar fecha de última actividad
    lector.setUltimaActividad(LocalDateTime.now(limaZone));

    // Guardar los cambios
    lectoresPorUsername.put(username, lector);

    // Actualizar el repositorio de usuarios
    actualizarUsuario(lector);

    return lectorMapper.lectorToLectorResponseDTO(lector);
  }

  @Override
  public Optional<LectorResponseDTO> obtenerPerfilLectorPorUsername(String username) {
    return obtenerEntidadLectorPorUsername(username)
        .map(lectorMapper::lectorToLectorResponseDTO);
  }

  @Override
  public Optional<Lector> obtenerEntidadLectorPorUsername(String username) {
    return Optional.ofNullable(lectoresPorUsername.get(username));
  }

  @Override
  public List<LectorResponseDTO> listarTodosLosLectores() {
    return lectoresPorUsername.values().stream()
        .map(lectorMapper::lectorToLectorResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public LectorResponseDTO cambiarEstadoLector(String username, String nuevoEstado) {
    Lector lector = obtenerEntidadLectorPorUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("No existe un perfil de lector para el usuario: " + username));

    // Validar el nuevo estado
    if (!esEstadoValido(nuevoEstado)) {
      throw new IllegalArgumentException("Estado de cuenta no válido: " + nuevoEstado);
    }

    lector.setEstadoCuenta(nuevoEstado);
    lector.setUltimaActividad(LocalDateTime.now(limaZone));

    // Actualizar repositorios
    lectoresPorUsername.put(username, lector);
    actualizarUsuario(lector);

    return lectorMapper.lectorToLectorResponseDTO(lector);
  }

  @Override
  public LectorResponseDTO actualizarMultasPendientes(String username, Integer nuevoValor) {
    Lector lector = obtenerEntidadLectorPorUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("No existe un perfil de lector para el usuario: " + username));

    if (nuevoValor < 0) {
      throw new IllegalArgumentException("El valor de multas pendientes no puede ser negativo");
    }

    lector.setMultasPendientes(nuevoValor);
    lector.setUltimaActividad(LocalDateTime.now(limaZone));

    // Actualizar automáticamente el estado si hay multas
    if (nuevoValor > 0 && "ACTIVO".equals(lector.getEstadoCuenta())) {
      lector.setEstadoCuenta("MULTADO");
    } else if (nuevoValor == 0 && "MULTADO".equals(lector.getEstadoCuenta())) {
      lector.setEstadoCuenta("ACTIVO");
    }

    // Actualizar repositorios
    lectoresPorUsername.put(username, lector);
    actualizarUsuario(lector);

    return lectorMapper.lectorToLectorResponseDTO(lector);
  }

  @Override
  public List<LectorResponseDTO> buscarLectoresPorApellido(String apellido) {
    return lectoresPorUsername.values().stream()
        .filter(lector -> lector.getApellido().toLowerCase().contains(apellido.toLowerCase()))
        .map(lectorMapper::lectorToLectorResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public boolean eliminarPerfilLector(String username) {
    if (lectoresPorUsername.remove(username) != null) {
      // Se deberia también "degradar" el Usuario a no-Lector
      return true;
    }
    return false;
  }

  private void actualizarUsuario(Lector lector) {
    
    usuarioService.actualizarLector(lector);
  }

  // Validador de estados
  private boolean esEstadoValido(String estado) {
    return List.of("ACTIVO", "SUSPENDIDO", "MULTADO", "INACTIVO").contains(estado);
  }
}