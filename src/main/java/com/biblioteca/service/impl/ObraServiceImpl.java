package com.biblioteca.service.impl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.AutorResponseDTO;
import com.biblioteca.dto.GeneroResponseDTO;
import com.biblioteca.dto.ObraPopularDTO;
import com.biblioteca.dto.ObraRequestDTO;
import com.biblioteca.dto.ObraResponseDTO;
import com.biblioteca.exceptions.RecursoNoEncontradoException;
import com.biblioteca.mapper.AutorMapper;
import com.biblioteca.mapper.GeneroMapper;
import com.biblioteca.mapper.ObraMapper;
import com.biblioteca.models.contenido.Autor;
import com.biblioteca.models.contenido.AutorObra;
import com.biblioteca.models.contenido.Editorial;
import com.biblioteca.models.contenido.Genero;
import com.biblioteca.models.contenido.Obra;
import com.biblioteca.repositories.AutorObraRepository;
import com.biblioteca.repositories.ObraRepository;
import com.biblioteca.service.AutorService;
import com.biblioteca.service.EditorialService;
import com.biblioteca.service.GeneroService;
import com.biblioteca.service.ObraService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ObraServiceImpl implements ObraService {

  private final ObraRepository obraRepository;
  private final AutorObraRepository autorObraRepository; // Para manejar la entidad de unión
  private final ObraMapper obraMapper;
  private final EditorialService editorialService; // Se mantiene para obtener la entidad Editorial
  private final AutorService autorService; // Se mantiene para obtener entidades Autor
  private final GeneroService generoService; // Se mantiene para obtener entidades Genero
  private final AutorMapper autorMapper; // Para convertir Autor a AutorResponseDTO
  private final GeneroMapper generoMapper; // Para convertir Genero a GeneroResponseDTO

  @Override
  @Transactional
  public ObraResponseDTO crearObra(ObraRequestDTO dto) {
    Obra obra = obraMapper.obraRequestDTOToObra(dto);

    Editorial editorial = editorialService.obtenerEntidadEditorialPorId(dto.getEditorialId())
        .orElseThrow(() -> new RecursoNoEncontradoException("Editorial no encontrada con ID: " + dto.getEditorialId()));
    obra.setEditorial(editorial);

    obra.setFechaCreacion(LocalDate.now());
    obra.setFechaActualizacion(LocalDate.now());

    // Manejar Géneros (relación ManyToMany directa)
    if (dto.getGeneroIds() != null && !dto.getGeneroIds().isEmpty()) {
      Set<Genero> generos = new HashSet<>();
      for (Long generoId : dto.getGeneroIds()) {
        Genero genero = generoService.obtenerEntidadGeneroPorId(generoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Género no encontrado con ID: " + generoId));
        generos.add(genero);
      }
      obra.setGeneros(generos);
    }

    // Guardar la obra primero para obtener su ID
    Obra obraGuardada = obraRepository.save(obra);

    if (dto.getAutorIds() != null && !dto.getAutorIds().isEmpty() &&
        dto.getAutorRoles() != null && dto.getAutorIds().size() == dto.getAutorRoles().size()) {

      List<AutorObra> autoresObras = new ArrayList<>();
      for (int i = 0; i < dto.getAutorIds().size(); i++) {
        Long autorId = dto.getAutorIds().get(i);
        String rol = dto.getAutorRoles().get(i);

        Autor autor = autorService.obtenerEntidadAutorPorId(autorId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Autor no encontrado con ID: " + autorId));

        // ✅ SOLUCIÓN: Crear AutorObra con clave compuesta correcta
        AutorObra autorObra = AutorObra.builder()
            .obraId(obraGuardada.getId())
            .autorId(autorId)
            .obra(obraGuardada)
            .autor(autor)
            .rol(rol)
            .build();

        AutorObra autorObraGuardado = autorObraRepository.save(autorObra);
        autoresObras.add(autorObraGuardado);
      }

      obraGuardada.setAutoresObras(new HashSet<>(autoresObras));
    }

    return convertirAObraResponseDTO(obraGuardada);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ObraResponseDTO> obtenerObraPorId(Long id) {
    return obraRepository.findById(id)
        .map(this::convertirAObraResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ObraResponseDTO> obtenerTodasLasObras() {
    return obraRepository.findAll().stream()
        .map(this::convertirAObraResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Optional<ObraResponseDTO> actualizarObra(Long id, ObraRequestDTO dto) {
    return obraRepository.findById(id).map(obraExistente -> {
      obraMapper.updateObraFromDto(dto, obraExistente);

      if (!obraExistente.getEditorial().getId().equals(dto.getEditorialId())) {
        Editorial editorial = editorialService.obtenerEntidadEditorialPorId(dto.getEditorialId())
            .orElseThrow(
                () -> new RecursoNoEncontradoException("Editorial no encontrada con ID: " + dto.getEditorialId()));
        obraExistente.setEditorial(editorial);
      }
      obraExistente.setFechaActualizacion(LocalDate.now());

      // Actualizar Géneros
      obraExistente.getGeneros().clear(); // Limpiar géneros existentes
      if (dto.getGeneroIds() != null && !dto.getGeneroIds().isEmpty()) {
        Set<Genero> nuevosGeneros = new HashSet<>();
        for (Long generoId : dto.getGeneroIds()) {
          Genero genero = generoService.obtenerEntidadGeneroPorId(generoId)
              .orElseThrow(() -> new RecursoNoEncontradoException("Género no encontrado con ID: " + generoId));
          nuevosGeneros.add(genero);
        }
        obraExistente.setGeneros(nuevosGeneros);
      }

      // Actualizar Autores (AutorObra)
      // Primero, eliminar las relaciones AutorObra existentes para esta obra
      autorObraRepository.deleteByObraId(obraExistente.getId());
      obraExistente.getAutoresObras().clear();

      if (dto.getAutorIds() != null && !dto.getAutorIds().isEmpty() &&
          dto.getAutorRoles() != null && dto.getAutorIds().size() == dto.getAutorRoles().size()) {

        List<AutorObra> nuevosAutoresObras = new ArrayList<>();
        for (int i = 0; i < dto.getAutorIds().size(); i++) {
          Long autorId = dto.getAutorIds().get(i);
          String rol = dto.getAutorRoles().get(i);

          Autor autor = autorService.obtenerEntidadAutorPorId(autorId)
              .orElseThrow(() -> new RecursoNoEncontradoException("Autor no encontrado con ID: " + autorId));

          AutorObra autorObra = AutorObra.builder()
              .obraId(obraExistente.getId())
              .autorId(autorId)
              .obra(obraExistente)
              .autor(autor)
              .rol(rol)
              .build();

          AutorObra autorObraGuardado = autorObraRepository.save(autorObra);
          nuevosAutoresObras.add(autorObraGuardado);
        }
        obraExistente.setAutoresObras(new HashSet<>(nuevosAutoresObras));
      }

      Obra obraActualizada = obraRepository.save(obraExistente);
      return convertirAObraResponseDTO(obraActualizada);
    });
  }

  @Override
  @Transactional
  public boolean eliminarObra(Long id, boolean estadoObra, String motivo) {
    // En lugar de eliminar, se cambia el estado 'disponible'
    return obraRepository.findById(id).map(obra -> {
      obra.setDisponible(estadoObra);
      obra.setMotivoEliminacion(motivo);
      obra.setFechaActualizacion(LocalDate.now());
      obraRepository.save(obra);
      return true;
    }).orElse(false);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Obra> obtenerEntidadObraPorId(Long id) {
    return obraRepository.findById(id);
  }

  private ObraResponseDTO convertirAObraResponseDTO(Obra obra) {
    ObraResponseDTO dto = obraMapper.obraToObraResponseDTO(obra);

    if (obra.getAutoresObras() != null) {
      List<AutorResponseDTO> autoresDeObraDTO = obra.getAutoresObras().stream()
          .map(ao -> {
            AutorResponseDTO autorDto = autorMapper.autorToAutorResponseDTO(ao.getAutor());
            // Aquí podrías añadir el rol al AutorResponseDTO si este DTO lo soporta,
            // o crear un DTO específico para "AutorConRolEnObra".
            // Por ahora, el rol está en AutorObra y no directamente en AutorResponseDTO.
            return autorDto;
          })
          .collect(Collectors.toList());
      dto.setAutores(autoresDeObraDTO);
    }

    if (obra.getGeneros() != null) {
      List<GeneroResponseDTO> generosDeObraDTO = obra.getGeneros().stream()
          .map(generoMapper::generoToGeneroResponseDTO)
          .collect(Collectors.toList());
      dto.setGeneros(generosDeObraDTO);
    }
    return dto;
  }

  @Override
  @Transactional(readOnly = true)
  public long contarObras() {
    return obraRepository.count();
  }

  @Override
  @Transactional(readOnly = true)
  public long contarObrasNuevasMes() {
    LocalDate inicioMes = YearMonth.now().atDay(1);
    LocalDate finMes = YearMonth.now().atEndOfMonth();
    // Es mejor crear un método en el repositorio para esto:
    // return obraRepository.countByFechaCreacionBetween(inicioMes, finMes);
    // Implementación actual (menos eficiente):
    return obraRepository.findAll().stream()
        .filter(o -> o.getFechaCreacion() != null)
        .filter(o -> !o.getFechaCreacion().isBefore(inicioMes) && !o.getFechaCreacion().isAfter(finMes))
        .count();
  }

  // Los siguientes métodos del dashboard requerirían lógica más compleja
  // o consultas específicas en los repositorios (ej. PrestamoRepository)
  // que no están en el alcance de esta refactorización de ObraService.
  // Se dejan como estaban o con comentarios.

  @Override
  @Transactional(readOnly = true)
  public long contarPrestamos() {
    // TODO: Implementar con PrestamoRepository.count() o similar
    return 0; // Placeholder
  }

  @Override
  @Transactional(readOnly = true)
  public long contarPrestamosMes() {
    // TODO: Implementar con PrestamoRepository y filtro de fecha
    return 0; // Placeholder
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Integer> obtenerPrestamosPorMes() {
    // TODO: Implementar con agregación de PrestamoRepository
    return new LinkedHashMap<>(); // Placeholder
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Integer> obtenerObrasPorGenero() {
    // Esta implementación es más factible con los datos actuales
    return obraRepository.findAll().stream()
        .flatMap(obra -> obra.getGeneros().stream())
        .collect(Collectors.groupingBy(
            Genero::getNombre,
            Collectors.summingInt(g -> 1)));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ObraPopularDTO> obtenerObrasPopulares(int limit) {
    // TODO: Implementar con lógica de popularidad (ej. número de préstamos, vistas)
    // y luego mapear a ObraPopularDTO.
    // Por ahora, devolvemos las obras más recientes como placeholder.
    Pageable pageable = PageRequest.of(0, limit);
    return obraRepository.findTopNByOrderByFechaCreacionDesc(pageable).stream()
        .map(obra -> {
          String autoresNombres = "Desconocido"; // Default value
          if (obra.getAutoresObras() != null && !obra.getAutoresObras().isEmpty()) {
            String nombresConcatenados = obra.getAutoresObras().stream()
                .map(autorObra -> {
                  Autor autor = autorObra.getAutor();
                  return (autor != null && autor.getNombre() != null) ? autor.getNombre() : null;
                })
                .filter(nombre -> nombre != null && !nombre.isEmpty())
                .collect(Collectors.joining(", "));

            if (!nombresConcatenados.isEmpty()) {
              autoresNombres = nombresConcatenados;
            }
          }
          return new ObraPopularDTO(obra.getId(), obra.getTitulo(), autoresNombres, 0);
        })
        .collect(Collectors.toList());
  }
}