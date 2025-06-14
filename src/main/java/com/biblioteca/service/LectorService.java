package com.biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.biblioteca.dto.LectorRequestDTO;
import com.biblioteca.dto.LectorResponseDTO;
import com.biblioteca.models.acceso.Lector;

public interface LectorService {

  // Verificar si un usuario ya tiene perfil de lector configurado
  boolean tienePerfilLector(String username);

  // Crear perfil de lector
  LectorResponseDTO crearPerfilLector(String username, LectorRequestDTO lectorDTO);

  // Actualizar perfil de lector
  LectorResponseDTO actualizarPerfilLector(String username, LectorRequestDTO lectorDTO);

  // Obtener perfil de lector por username
  Optional<LectorResponseDTO> obtenerPerfilLectorPorUsername(String username);

  // Obtener entidad Lector directamente (para uso interno principalmente)
  Optional<Lector> obtenerEntidadLectorPorUsername(String username);

  // Listar todos los lectores (admin)
  List<LectorResponseDTO> listarTodosLosLectores();

  // Cambiar estado de lector (suspender/activar)
  LectorResponseDTO cambiarEstadoLector(String username, String nuevoEstado);

  // Actualizar multas pendientes
  LectorResponseDTO actualizarMultasPendientes(String username, Integer nuevoValor);

  // Buscar lectores por criterios específicos (pueden ser varios métodos)
  List<LectorResponseDTO> buscarLectoresPorApellido(String apellido);

  // Eliminar perfil de lector (raro, pero posible en algunos casos)
  boolean eliminarPerfilLector(String username);
}