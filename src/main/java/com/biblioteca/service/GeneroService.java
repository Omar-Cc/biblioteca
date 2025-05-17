package com.biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.biblioteca.dto.GeneroRequestDTO;
import com.biblioteca.dto.GeneroResponseDTO;
import com.biblioteca.models.Genero;

public interface GeneroService {
  GeneroResponseDTO crearGenero(GeneroRequestDTO generoRequestDTO);

  Optional<GeneroResponseDTO> obtenerGeneroPorId(Long id);

  List<GeneroResponseDTO> obtenerTodosLosGeneros();

  Optional<GeneroResponseDTO> actualizarGenero(Long id, GeneroRequestDTO generoRequestDTO);

  boolean eliminarGenero(Long id);

  Optional<Genero> obtenerEntidadGeneroPorId(Long id);

  List<GeneroResponseDTO> obtenerGenerosPrincipalesConSubgeneros();

  List<GeneroResponseDTO> obtenerGenerosPrincipalesSinSubgeneros();

  // Métodos para el dashboard
  long contarGeneros();

}
