package com.biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.biblioteca.dto.AutorRequestDTO;
import com.biblioteca.dto.AutorResponseDTO;
import com.biblioteca.models.contenido.Autor;

public interface AutorService {
  AutorResponseDTO crearAutor(AutorRequestDTO autorRequestDTO);

  Optional<AutorResponseDTO> obtenerAutorPorId(Long id);

  List<AutorResponseDTO> obtenerTodosLosAutores();

  Optional<AutorResponseDTO> actualizarAutor(Long id, AutorRequestDTO autorRequestDTO);

  /**
   * Elimina un autor.
   * @param id ID del autor a eliminar.
   * @param estadoAutor Nuevo estado del autor.
   * @param motivoEliminacion Motivo de la eliminación/cambio de estado.
   * @return true si se eliminó (o actualizó estado), false si no se encontró el autor.
   */
  boolean eliminarAutor(Long id, boolean estadoAutor, String motivoEliminacion);

  /**
   * Obtiene la entidad Autor completa por su ID.
   *
   * @param id ID del autor a buscar.
   * @return Optional que contiene la entidad Autor si se encuentra, o vacío si no.
   */
  Optional<Autor> obtenerEntidadAutorPorId(Long id);
}