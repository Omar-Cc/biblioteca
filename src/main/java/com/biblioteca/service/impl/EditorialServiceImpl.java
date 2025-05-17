package com.biblioteca.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.biblioteca.dto.EditorialRequestDTO;
import com.biblioteca.dto.EditorialResponseDTO;
import com.biblioteca.mapper.EditorialMapper;
import com.biblioteca.models.Editorial;
import com.biblioteca.service.EditorialService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EditorialServiceImpl implements EditorialService {

  private final List<Editorial> editoriales = new ArrayList<>();
  private final AtomicLong editorialIdCounter = new AtomicLong();
  private final EditorialMapper editorialMapper;
  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;

  @PostConstruct
  public void initEditorialesData() {
    try {
      InputStream inputStream = resourceLoader.getResource("classpath:data/editoriales-data.json").getInputStream();
      List<EditorialRequestDTO> editorialDTOs = objectMapper.readValue(inputStream,
          new TypeReference<List<EditorialRequestDTO>>() {});
      editorialDTOs.forEach(this::crearEditorial);
      System.out.println("Datos iniciales de Editoriales cargados desde JSON: " + editoriales.size() + " editoriales.");
    } catch (Exception e) {
      System.err.println("Error al cargar datos iniciales de editoriales desde JSON: " + e.getMessage());
      e.printStackTrace();
    }
  }
  
  @Override
  public EditorialResponseDTO crearEditorial(EditorialRequestDTO dto) {
    Editorial editorial = new Editorial();
    editorial.setId(editorialIdCounter.incrementAndGet());
    editorial.setNombre(dto.getNombre());
    editorial.setPais(dto.getPais());
    editorial.setWebsite(dto.getWebsite());
    editoriales.add(editorial);
    return editorialMapper.editorialToEditorialResponseDTO(editorial);
  }

  @Override
  public Optional<EditorialResponseDTO> obtenerEditorialPorId(Long id) {
    return editoriales.stream()
        .filter(e -> e.getId().equals(id))
        .map(editorialMapper::editorialToEditorialResponseDTO)
        .findFirst();
  }

  @Override
  public List<EditorialResponseDTO> obtenerTodasLasEditoriales() {
    return editoriales.stream()
        .map(editorialMapper::editorialToEditorialResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<EditorialResponseDTO> actualizarEditorial(Long id, EditorialRequestDTO dto) {
    Optional<Editorial> editorialOpt = editoriales.stream().filter(e -> e.getId().equals(id)).findFirst();
    if (editorialOpt.isPresent()) {
      Editorial editorial = editorialOpt.get();
      editorialMapper.updateEditorialFromDto(dto, editorial);
      return Optional.of(editorialMapper.editorialToEditorialResponseDTO(editorial));
    }
    return Optional.empty();
  }

  @Override
  public boolean eliminarEditorial(Long id) {
    // Considerar impacto en obras si una editorial se elimina
    return editoriales.removeIf(e -> e.getId().equals(id));
  }

  @Override
  public Optional<Editorial> obtenerEntidadEditorialPorId(Long id) {
    return editoriales.stream()
        .filter(e -> e.getId().equals(id))
        .findFirst();
  }

}
