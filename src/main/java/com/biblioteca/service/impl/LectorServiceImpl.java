package com.biblioteca.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.LectorRequestDTO;
import com.biblioteca.dto.LectorResponseDTO;
import com.biblioteca.exceptions.RecursoNoEncontradoException;
import com.biblioteca.mapper.LectorMapper;
import com.biblioteca.models.acceso.Lector;
import com.biblioteca.models.acceso.Usuario;
import com.biblioteca.repositories.LectorRepository;
import com.biblioteca.service.LectorService;
import com.biblioteca.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LectorServiceImpl implements LectorService {

  private final UsuarioService usuarioService;
  private final LectorRepository lectorRepository;
  private final LectorMapper lectorMapper;
  private final ZoneId limaZone = ZoneId.of("America/Lima");

  @Override
  @Transactional(readOnly = true)
  public boolean tienePerfilLector(String username) {
    // Asumiendo que Lector tiene una relación con Usuario y podemos buscar por el
    // username del Usuario
    return lectorRepository.existsByUsuarioUsername(username);
  }

  @Override
  @Transactional
  public LectorResponseDTO crearPerfilLector(String username, LectorRequestDTO lectorDTO) {
    if (tienePerfilLector(username)) {
      throw new IllegalArgumentException("Ya existe un perfil de lector para el usuario: " + username);
    }

    Usuario usuario = usuarioService.buscarPorUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

    Lector lector = lectorMapper.usuarioYDtoToLector(usuario, lectorDTO);
    lector.setUsuario(usuario); // Asegurar que la relación esté establecida
    usuario.setUltimaActividad(LocalDateTime.now(limaZone));
    // El ID del lector será generado por la base de datos

    Lector lectorGuardado = lectorRepository.save(lector);
    // La actualización del tipo de usuario en la entidad Usuario (si es necesario)
    // podría manejarse aquí o dentro de UsuarioService si Lector no es una subclase
    // directa.
    // Por ejemplo, si Usuario tiene un campo 'tipo' o 'rol'.
    // usuarioService.marcarComoLector(username); // Ejemplo de método en
    // UsuarioService

    return lectorMapper.lectorToLectorResponseDTO(lectorGuardado);
  }

  @Override
  @Transactional
  public LectorResponseDTO actualizarPerfilLector(String username, LectorRequestDTO lectorDTO) {
    Lector lector = lectorRepository.findByUsuarioUsername(username)
        .orElseThrow(
            () -> new RecursoNoEncontradoException("No existe un perfil de lector para el usuario: " + username));

    lectorMapper.updateLectorFromDto(lectorDTO, lector);
    lector.getUsuario().setUltimaActividad(LocalDateTime.now(limaZone));

    Lector lectorActualizado = lectorRepository.save(lector);
    return lectorMapper.lectorToLectorResponseDTO(lectorActualizado);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<LectorResponseDTO> obtenerPerfilLectorPorUsername(String username) {
    return lectorRepository.findByUsuarioUsername(username)
        .map(lectorMapper::lectorToLectorResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Lector> obtenerEntidadLectorPorUsername(String username) {
    return lectorRepository.findByUsuarioUsername(username);
  }

  @Override
  @Transactional(readOnly = true)
  public List<LectorResponseDTO> listarTodosLosLectores() {
    return lectorRepository.findAll().stream()
        .map(lectorMapper::lectorToLectorResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public LectorResponseDTO cambiarEstadoLector(String username, String nuevoEstado) {
    Lector lector = lectorRepository.findByUsuarioUsername(username)
        .orElseThrow(
            () -> new RecursoNoEncontradoException("No existe un perfil de lector para el usuario: " + username));

    if (!esEstadoValido(nuevoEstado)) {
      throw new IllegalArgumentException("Estado de cuenta no válido: " + nuevoEstado);
    }

    lector.setEstadoCuenta(nuevoEstado);
    lector.getUsuario().setUltimaActividad(LocalDateTime.now(limaZone));

    Lector lectorActualizado = lectorRepository.save(lector);
    return lectorMapper.lectorToLectorResponseDTO(lectorActualizado);
  }

  @Override
  @Transactional
  public LectorResponseDTO actualizarMultasPendientes(String username, Integer nuevoValor) {
    Lector lector = lectorRepository.findByUsuarioUsername(username)
        .orElseThrow(
            () -> new RecursoNoEncontradoException("No existe un perfil de lector para el usuario: " + username));

    if (nuevoValor < 0) {
      throw new IllegalArgumentException("El valor de multas pendientes no puede ser negativo");
    }

    lector.setMultasPendientes(nuevoValor);
    lector.getUsuario().setUltimaActividad(LocalDateTime.now(limaZone));

    if (nuevoValor > 0 && "ACTIVO".equals(lector.getEstadoCuenta())) {
      lector.setEstadoCuenta("MULTADO");
    } else if (nuevoValor == 0 && "MULTADO".equals(lector.getEstadoCuenta())) {
      lector.setEstadoCuenta("ACTIVO");
    }

    Lector lectorActualizado = lectorRepository.save(lector);
    return lectorMapper.lectorToLectorResponseDTO(lectorActualizado);
  }

  @Override
  @Transactional(readOnly = true)
  public List<LectorResponseDTO> buscarLectoresPorApellido(String apellido) {
    // Asumiendo que Lector tiene una relación con Usuario y Usuario tiene el campo
    // apellido.
    // La consulta real dependerá de cómo esté definida la entidad Lector y su
    // relación.
    // Si Lector hereda de Usuario, esto podría ser más directo.
    // Si Lector tiene un campo 'usuario', la consulta sería por 'usuario.apellido'.
    return lectorRepository.findByApellidoContainingIgnoreCase(apellido).stream()
        .map(lectorMapper::lectorToLectorResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public boolean eliminarPerfilLector(String username) {
    Optional<Lector> lectorOpt = lectorRepository.findByUsuarioUsername(username);
    if (lectorOpt.isPresent()) {
      lectorRepository.delete(lectorOpt.get());
      // Aquí podría ir lógica para "degradar" al Usuario si es necesario,
      // por ejemplo, cambiar un rol o tipo en la entidad Usuario.
      // usuarioService.removerRolLector(username); // Ejemplo
      return true;
    }
    return false;
  }

  private boolean esEstadoValido(String estado) {
    return List.of("ACTIVO", "SUSPENDIDO", "MULTADO", "INACTIVO").contains(estado);
  }
}