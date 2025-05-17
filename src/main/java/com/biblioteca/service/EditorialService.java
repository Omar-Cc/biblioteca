package com.biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.biblioteca.dto.EditorialRequestDTO;
import com.biblioteca.dto.EditorialResponseDTO;
import com.biblioteca.models.Editorial;

public interface EditorialService {
  EditorialResponseDTO crearEditorial(EditorialRequestDTO editorialRequestDTO);

  Optional<EditorialResponseDTO> obtenerEditorialPorId(Long id);

  List<EditorialResponseDTO> obtenerTodasLasEditoriales();

  Optional<EditorialResponseDTO> actualizarEditorial(Long id, EditorialRequestDTO editorialRequestDTO);

  boolean eliminarEditorial(Long id);

  Optional<Editorial> obtenerEntidadEditorialPorId(Long id);
}
