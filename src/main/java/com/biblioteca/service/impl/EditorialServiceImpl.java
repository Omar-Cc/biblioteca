package com.biblioteca.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.EditorialRequestDTO;
import com.biblioteca.dto.EditorialResponseDTO;
import com.biblioteca.mapper.EditorialMapper;
import com.biblioteca.models.contenido.Editorial;
import com.biblioteca.repositories.EditorialRepository;
import com.biblioteca.service.EditorialService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EditorialServiceImpl implements EditorialService {

  private final EditorialRepository editorialRepository;
  private final EditorialMapper editorialMapper;

  @Override
  @Transactional
  public EditorialResponseDTO crearEditorial(EditorialRequestDTO dto) {
    Editorial editorial = editorialMapper.editorialRequestDTOToEditorial(dto);
    Editorial editorialGuardada = editorialRepository.save(editorial);
    return editorialMapper.editorialToEditorialResponseDTO(editorialGuardada);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<EditorialResponseDTO> obtenerEditorialPorId(Long id) {
    return editorialRepository.findById(id)
        .map(editorialMapper::editorialToEditorialResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public List<EditorialResponseDTO> obtenerTodasLasEditoriales() {
    return editorialRepository.findAll().stream()
        .map(editorialMapper::editorialToEditorialResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Optional<EditorialResponseDTO> actualizarEditorial(Long id, EditorialRequestDTO dto) {
    Optional<Editorial> editorialOpt = editorialRepository.findById(id);
    if (editorialOpt.isPresent()) {
      Editorial editorial = editorialOpt.get();
      editorialMapper.updateEditorialFromDto(dto, editorial);
      Editorial editorialActualizada = editorialRepository.save(editorial);
      return Optional.of(editorialMapper.editorialToEditorialResponseDTO(editorialActualizada));
    }
    return Optional.empty();
  }

  @Override
  @Transactional
  public boolean eliminarEditorial(Long id) {
    // Considerar impacto en obras si una editorial se elimina.
    // Spring Data JPA lanzará una excepción si hay restricciones de clave externa.
    // Se podría añadir lógica aquí para verificar si la editorial está en uso antes
    // de eliminar.
    if (editorialRepository.existsById(id)) {
      editorialRepository.deleteById(id);
      return true;
    }
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Editorial> obtenerEntidadEditorialPorId(Long id) {
    return editorialRepository.findById(id);
  }

}
