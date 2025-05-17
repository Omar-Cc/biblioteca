package com.biblioteca.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.biblioteca.dto.AutorRequestDTO;
import com.biblioteca.dto.AutorResponseDTO;
import com.biblioteca.mapper.AutorMapper;
import com.biblioteca.models.Autor;
import com.biblioteca.service.AutorService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.annotation.PostConstruct;

@Service
public class AutorServiceImpl implements AutorService {

  private final List<Autor> autores = new ArrayList<>();
  private final AtomicLong autorIdCounter = new AtomicLong();
  private final AutorMapper autorMapper;
  private final ObjectMapper objectMapper; // Para leer JSON
  private final ResourceLoader resourceLoader;

  public AutorServiceImpl(AutorMapper autorMapper, ObjectMapper objectMapper, ResourceLoader resourceLoader) {
    this.autorMapper = autorMapper;
    this.objectMapper = objectMapper;
    // Registrar el módulo JavaTimeModule si no está configurado globalmente
    this.objectMapper.registerModule(new JavaTimeModule());
    this.resourceLoader = resourceLoader;
  }

  @PostConstruct
  public void initAutoresData() {
    try {
      InputStream inputStream = resourceLoader.getResource("classpath:data/autores-data.json").getInputStream();
      List<AutorRequestDTO> autorDTOs = objectMapper.readValue(inputStream, new TypeReference<List<AutorRequestDTO>>() {});
      autorDTOs.forEach(this::crearAutor);
      System.out.println("Datos iniciales de Autores cargados desde JSON: " + autores.size() + " autores.");
    } catch (Exception e) {
      System.err.println("Error al cargar datos iniciales de autores desde JSON: " + e.getMessage());
    }
  }
  
  @Override
  public AutorResponseDTO crearAutor(AutorRequestDTO dto) {
    
    Autor autor = autorMapper.autorRequestDTOToAutor(dto);
    autor.setId(autorIdCounter.incrementAndGet());

    autores.add(autor);
    
    return autorMapper.autorToAutorResponseDTO(autor);
  }

  @Override
  public Optional<AutorResponseDTO> obtenerAutorPorId(Long id) {
    return autores.stream()
        .filter(a -> a.getId().equals(id))
        .map(autorMapper::autorToAutorResponseDTO)
        .findFirst();
  }

  @Override
  public List<AutorResponseDTO> obtenerTodosLosAutores() {
    return autores.stream()
        .map(autorMapper::autorToAutorResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<AutorResponseDTO> actualizarAutor(Long id, AutorRequestDTO dto) {
    Optional<Autor> autorOpt = obtenerEntidadAutorPorId(id);
    if (autorOpt.isPresent()) {
      Autor autor = autorOpt.get();
      autorMapper.updateAutorFromDto(dto, autor);

      return Optional.of(autorMapper.autorToAutorResponseDTO(autor));
    }
    return Optional.empty();
  }

  @Override
  public boolean eliminarAutor(Long id, boolean estadoAutor, String motivoEliminacion) {
    return autores.removeIf(a -> a.getId().equals(id));
  }

  @Override
  public Optional<Autor> obtenerEntidadAutorPorId(Long id) {
    return autores.stream()
        .filter(a -> a.getId().equals(id))
        .findFirst();
  }

}
