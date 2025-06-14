package com.biblioteca.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.AutorRequestDTO;
import com.biblioteca.dto.AutorResponseDTO;
import com.biblioteca.mapper.AutorMapper;
import com.biblioteca.models.contenido.Autor;
import com.biblioteca.repositories.AutorRepository;
import com.biblioteca.service.AutorService;

@Service
public class AutorServiceImpl implements AutorService {

  private final AutorRepository autorRepository;
  private final AutorMapper autorMapper;

  public AutorServiceImpl(AutorRepository autorRepository, AutorMapper autorMapper) {
    this.autorRepository = autorRepository;
    this.autorMapper = autorMapper;
  }

  @Override
  @Transactional
  public AutorResponseDTO crearAutor(AutorRequestDTO dto) {
    Autor autor = autorMapper.autorRequestDTOToAutor(dto);
    Autor autorGuardado = autorRepository.save(autor);
    return autorMapper.autorToAutorResponseDTO(autorGuardado);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<AutorResponseDTO> obtenerAutorPorId(Long id) {
    return autorRepository.findById(id)
        .map(autorMapper::autorToAutorResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AutorResponseDTO> obtenerTodosLosAutores() {
    return autorRepository.findAll().stream()
        .map(autorMapper::autorToAutorResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Optional<AutorResponseDTO> actualizarAutor(Long id, AutorRequestDTO dto) {
    Optional<Autor> autorOpt = autorRepository.findById(id);
    if (autorOpt.isPresent()) {
      Autor autor = autorOpt.get();
      autorMapper.updateAutorFromDto(dto, autor);
      Autor autorActualizado = autorRepository.save(autor);
      return Optional.of(autorMapper.autorToAutorResponseDTO(autorActualizado));
    }
    return Optional.empty();
  }

  @Override
  @Transactional
  public boolean eliminarAutor(Long id, boolean estadoAutor, String motivoEliminacion) {
    if (autorRepository.existsById(id)) {
      autorRepository.deleteById(id);
      return true;
    }
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Autor> obtenerEntidadAutorPorId(Long id) {
    return autorRepository.findById(id);
  }
}