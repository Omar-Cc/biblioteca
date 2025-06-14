package com.biblioteca.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.GeneroRequestDTO;
import com.biblioteca.dto.GeneroResponseDTO;
import com.biblioteca.exceptions.RecursoNoEncontradoException;
import com.biblioteca.mapper.GeneroMapper;
import com.biblioteca.models.contenido.Genero;
import com.biblioteca.repositories.GeneroRepository;
import com.biblioteca.service.GeneroService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeneroServiceImpl implements GeneroService {

  private final GeneroRepository generoRepository;
  private final GeneroMapper generoMapper;

  @Override
  @Transactional
  public GeneroResponseDTO crearGenero(GeneroRequestDTO dto) {
    Genero genero = generoMapper.generoRequestDTOToGenero(dto);

    if (dto.getParentId() != null) {
      Genero padre = generoRepository.findById(dto.getParentId())
          .orElseThrow(
              () -> new RecursoNoEncontradoException("Género padre no encontrado con ID: " + dto.getParentId()));
      genero.setPadre(padre);
    }

    genero = generoRepository.save(genero);
    return generoMapper.generoToGeneroResponseDTO(genero);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<GeneroResponseDTO> obtenerGeneroPorId(Long id) {
    return generoRepository.findById(id)
        .map(generoMapper::generoToGeneroResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public List<GeneroResponseDTO> obtenerTodosLosGeneros() {
    return generoRepository.findAll().stream()
        .map(generoMapper::generoToGeneroResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Optional<GeneroResponseDTO> actualizarGenero(Long id, GeneroRequestDTO dto) {
    return generoRepository.findById(id).map(genero -> {
      genero.setNombre(dto.getNombre());
      genero.setDescripcion(dto.getDescripcion());
      genero.setNivel(dto.getNivel());

      if (dto.getParentId() != null) {
        if (genero.getPadre() == null || !dto.getParentId().equals(genero.getPadre().getId())) {
          Genero padre = generoRepository.findById(dto.getParentId())
              .orElseThrow(
                  () -> new RecursoNoEncontradoException("Género padre no encontrado con ID: " + dto.getParentId()));
          genero.setPadre(padre);
        }
      } else {
        genero.setPadre(null);
      }

      Genero generoActualizado = generoRepository.save(genero);
      return generoMapper.generoToGeneroResponseDTO(generoActualizado);
    });
  }

  @Override
  @Transactional
  public boolean eliminarGenero(Long id) {
    Optional<Genero> generoOpt = generoRepository.findById(id);
    if (generoOpt.isPresent()) {
      Genero genero = generoOpt.get();
      // Antes de eliminar, desvincular subgéneros para evitar errores de FK
      // o asegurarse que la BD maneje la eliminación en cascada o SET NULL.
      // La lógica actual de establecer padre a null en subgéneros es una forma de
      // manejarlo.
      if (genero.getSubgeneros() != null && !genero.getSubgeneros().isEmpty()) {
        for (Genero subgenero : genero.getSubgeneros()) {
          subgenero.setPadre(null); // Desvincular subgénero
          generoRepository.save(subgenero); // Guardar el subgénero desvinculado
        }
      }
      generoRepository.deleteById(id);
      return true;
    }
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Genero> obtenerEntidadGeneroPorId(Long id) {
    return generoRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true) // Añadir readOnly = true
  public List<GeneroResponseDTO> obtenerGenerosPrincipalesConSubgeneros() {
    List<Genero> generosPrincipales = generoRepository.findByPadreIsNull();
    return generosPrincipales.stream()
        .map(genero -> {
          // Usar el mapper para convertir la entidad y luego poblar los subgéneros si es
          // necesario
          // El mapper ya debería manejar la conversión de subgéneros si está configurado
          // para ello.
          // Si el mapper no carga los subgéneros por defecto (ej. por
          // @Mapping(ignore=true)),
          // entonces se deben cargar explícitamente como se hace aquí.
          GeneroResponseDTO dto = generoMapper.generoToGeneroResponseDTO(genero);
          if (genero.getSubgeneros() != null) {
            dto.setSubgeneros(genero.getSubgeneros().stream()
                .map(generoMapper::generoToGeneroResponseDTO)
                .collect(Collectors.toList()));
          }
          return dto;
        })
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<GeneroResponseDTO> obtenerGenerosPrincipalesSinSubgeneros() {
    return generoRepository.findByPadreIsNullAndSubgenerosIsEmpty().stream()
        .map(generoMapper::generoToGeneroResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public long contarGeneros() {
    return generoRepository.count();
  }
}