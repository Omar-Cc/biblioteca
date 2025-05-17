package com.biblioteca.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.biblioteca.dto.ObraPopularDTO;
import com.biblioteca.dto.ObraRequestDTO;
import com.biblioteca.dto.ObraResponseDTO;
import com.biblioteca.models.Obra;

public interface ObraService {
  /**
   * Crea una nueva obra en la biblioteca.
   *
   * @param obraRequestDTO Objeto que contiene los datos de la obra a crear.
   * @return Objeto que representa la obra creada.
   */
  ObraResponseDTO crearObra(ObraRequestDTO obraRequestDTO);

  /**
   * Obtiene una obra por su ID.
   *
   * @param id ID de la obra a buscar.
   * @return Objeto que representa la obra encontrada, o un Optional vacío si no
   *         se encuentra.
   */
  Optional<ObraResponseDTO> obtenerObraPorId(Long id);

  /**
   * Obtiene todas las obras de la biblioteca.
   *
   * @return Lista de objetos que representan todas las obras.
   */
  List<ObraResponseDTO> obtenerTodasLasObras();

  /**
   * Actualiza una obra existente en la biblioteca.
   *
   * @param id             ID de la obra a actualizar.
   * @param obraRequestDTO Objeto que contiene los nuevos datos de la obra.
   * @return Objeto que representa la obra actualizada, o un Optional vacío si no
   *         se encuentra.
   */
  Optional<ObraResponseDTO> actualizarObra(Long id, ObraRequestDTO obraRequestDTO);

  /**
   * Elimina una obra de la biblioteca.
   *
   * @param id                ID de la obra a eliminar.
   * @param estadoObra        Estado de la obra (true si está disponible, false si
   *                          está eliminada).
   * @param motivoEliminacion Motivo de la eliminación de la obra.
   * @return true si la obra fue eliminada exitosamente, false en caso contrario.
   */
  boolean eliminarObra(Long id, boolean estadoObra, String motivoEliminacion);

  /**
   * Obtiene la entidad Obra completa por su ID.
   *
   * @param id ID de la obra a buscar.
   * @return Optional que contiene la entidad Obra si se encuentra, o vacío si no.
   */
  Optional<Obra> obtenerEntidadObraPorId(Long id);

  // Métodos para el dashboard
  long contarObras();

  long contarObrasNuevasMes();

  long contarPrestamos();

  long contarPrestamosMes();

  Map<String, Integer> obtenerPrestamosPorMes();

  Map<String, Integer> obtenerObrasPorGenero();

  List<ObraPopularDTO> obtenerObrasPopulares(int limit);

}
