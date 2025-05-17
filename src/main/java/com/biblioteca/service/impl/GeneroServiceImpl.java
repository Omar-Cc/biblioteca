package com.biblioteca.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.biblioteca.dto.GeneroRequestDTO;
import com.biblioteca.dto.GeneroResponseDTO;
import com.biblioteca.mapper.GeneroMapper;
import com.biblioteca.models.Genero;
import com.biblioteca.service.GeneroService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class GeneroServiceImpl implements GeneroService {
  private final List<Genero> generos = new ArrayList<>();
  private final AtomicLong generoIdCounter = new AtomicLong();
  private final GeneroMapper generoMapper;
  private final ObjectMapper objectMapper; // Para leer JSON
  private final ResourceLoader resourceLoader; // Para cargar el archivo

  // Modificar constructor para inyectar ObjectMapper y ResourceLoader
  GeneroServiceImpl(GeneroMapper generoMapper, ObjectMapper objectMapper, ResourceLoader resourceLoader) {
    this.generoMapper = generoMapper;
    this.objectMapper = objectMapper;
    this.resourceLoader = resourceLoader;
  }

  @PostConstruct
  public void initGenerosData() {
    try {
      InputStream inputStream = resourceLoader.getResource("classpath:data/generos-data.json").getInputStream();
      List<GeneroRequestDTO> generoDTOs = objectMapper.readValue(inputStream,
          new TypeReference<List<GeneroRequestDTO>>() {
          });
      generoDTOs.forEach(this::crearGenero); // Usar un método auxiliar o directamente crearGenero
      System.out.println("Datos iniciales de Géneros cargados desde JSON: " + generos.size() + " géneros.");
    } catch (Exception e) {
      System.err.println("Error al cargar datos iniciales de géneros desde JSON: " + e.getMessage());
    }
  }

  @Override
  public GeneroResponseDTO crearGenero(GeneroRequestDTO dto) {
    Genero genero = generoMapper.generoRequestDTOToGenero(dto);
    genero.setId(generoIdCounter.incrementAndGet());
    generos.add(genero);
    return generoMapper.generoToGeneroResponseDTO(genero);
  }

  @Override
  public Optional<GeneroResponseDTO> obtenerGeneroPorId(Long id) {
    return generos.stream()
        .filter(g -> g.getId().equals(id))
        .map(generoMapper::generoToGeneroResponseDTO)
        .findFirst();
  }

  @Override
  public List<GeneroResponseDTO> obtenerTodosLosGeneros() {
    return generos.stream()
        .map(generoMapper::generoToGeneroResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<GeneroResponseDTO> actualizarGenero(Long id, GeneroRequestDTO dto) {
    Optional<Genero> generoOpt = obtenerEntidadGeneroPorId(id);
    if (generoOpt.isPresent()) {
      Genero genero = generoOpt.get();

      generoMapper.updateGeneroFromDto(dto, genero);

      return Optional.of(generoMapper.generoToGeneroResponseDTO(genero));
    }
    return Optional.empty();
  }

  @Override
  public boolean eliminarGenero(Long id) {
    return generos.removeIf(g -> g.getId().equals(id));
  }

  @Override
  public Optional<Genero> obtenerEntidadGeneroPorId(Long id) {
    return generos.stream()
        .filter(e -> e.getId().equals(id))
        .findFirst();
  }

  // Método para obtener todos los géneros principales (sin padre) con subgéneros
  @Override
  public List<GeneroResponseDTO> obtenerGenerosPrincipalesConSubgeneros() {
    construirJerarquia(); // Asegúrate de que la jerarquía esté construida antes de obtener los géneros
    return generos.stream()
        .filter(g -> g.getParentId() == null)
        .map(g -> {
          GeneroResponseDTO generoDTO = generoMapper.generoToGeneroResponseDTO(g);
          generoDTO.setSubgeneros(g.getSubgeneros().stream()
              .map(generoMapper::generoToGeneroResponseDTO)
              .collect(Collectors.toList()));
          return generoDTO;
        })
        .collect(Collectors.toList());
  }

  // Método para obtener solo géneros de nivel superior (sin padre) sin subgéneros
  @Override
  public List<GeneroResponseDTO> obtenerGenerosPrincipalesSinSubgeneros() {
    return generos.stream()
        .filter(g -> g.getParentId() == null && g.getSubgeneros().isEmpty())
        .map(generoMapper::generoToGeneroResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public long contarGeneros() {
    return generos.size();
  }

  // Método para construir la jerarquía después de cargar todos los géneros
  private void construirJerarquia() {
    for (Genero genero : generos) {
      if (genero.getParentId() != null) {
        obtenerEntidadGeneroPorId(genero.getParentId()).ifPresent(padre -> {
          padre.getSubgeneros().add(genero);
        });
      }
    }
  }

}
