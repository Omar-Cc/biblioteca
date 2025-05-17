package com.biblioteca.service.impl;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.biblioteca.dto.AutorResponseDTO;
import com.biblioteca.dto.GeneroResponseDTO;
import com.biblioteca.dto.ObraPopularDTO;
import com.biblioteca.dto.ObraRequestDTO;
import com.biblioteca.dto.ObraResponseDTO;
import com.biblioteca.mapper.AutorMapper;
import com.biblioteca.mapper.GeneroMapper;
import com.biblioteca.mapper.ObraMapper;
import com.biblioteca.models.AutorObra;
import com.biblioteca.models.Editorial;
import com.biblioteca.models.Obra;
import com.biblioteca.models.ObraGenero;
import com.biblioteca.service.AutorService;
import com.biblioteca.service.EditorialService;
import com.biblioteca.service.GeneroService;
import com.biblioteca.service.ObraService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class ObraServiceImpl implements ObraService {

  private final ObraMapper obraMapper;
  private final EditorialService editorialService;
  private final AutorService autorService;
  private final GeneroService generoService;

  private final AutorMapper autorMapper;
  private final GeneroMapper generoMapper;

  private final List<Obra> obras = new ArrayList<>();
  private final List<AutorObra> autoresObras = new ArrayList<>();
  private final List<ObraGenero> obrasGeneros = new ArrayList<>();
  private final AtomicLong obraIdCounter = new AtomicLong();

  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;

  public ObraServiceImpl(ObraMapper obraMapper, EditorialService editorialService,
      AutorService autorService, GeneroService generoService,
      AutorMapper autorMapper, GeneroMapper generoMapper,
      ObjectMapper objectMapper, ResourceLoader resourceLoader) {
    this.obraMapper = obraMapper;
    this.editorialService = editorialService;
    this.autorService = autorService;
    this.generoService = generoService;
    this.autorMapper = autorMapper;
    this.generoMapper = generoMapper;
    this.objectMapper = objectMapper;
    this.resourceLoader = resourceLoader;
  }

  @PostConstruct
  public void initObrasData() {
    try {
      InputStream inputStream = resourceLoader.getResource("classpath:data/obras-data.json").getInputStream();
      List<ObraRequestDTO> obraDTOs = objectMapper.readValue(inputStream,
          new TypeReference<List<ObraRequestDTO>>() {
          });

      for (ObraRequestDTO dto : obraDTOs) {
        try {
          crearObra(dto);
        } catch (Exception e) {
          System.err.println("Error al crear obra '" + dto.getTitulo() + "': " + e.getMessage());
        }
      }
      System.out.println("Datos iniciales de Obras cargados desde JSON: " + obras.size() + " obras.");
    } catch (Exception e) {
      System.err.println("Error al cargar datos iniciales de obras desde JSON: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public ObraResponseDTO crearObra(ObraRequestDTO dto) {
    // 1. Mapear DTO a Entidad (campos básicos) usando ObraMapper
    Obra obra = obraMapper.obraRequestDTOToObra(dto);

    // 2. Obtener y asignar la Editorial desde EditorialService
    Editorial editorial = editorialService.obtenerEntidadEditorialPorId(dto.getEditorialId())
        .orElseThrow(() -> new IllegalArgumentException("Editorial no encontrada con ID: " + dto.getEditorialId()));
    obra.setEditorial(editorial);

    // 3. Establecer campos gestionados por el servidor
    obra.setId(obraIdCounter.incrementAndGet());
    obra.setFechaCreacion(LocalDate.now());
    obra.setFechaActualizacion(LocalDate.now());
    obras.add(obra);

    // 4. Manejar Autores (crear relaciones en AutorObra)
    if (dto.getAutorIds() != null && dto.getAutorRoles() != null &&
        dto.getAutorIds().size() == dto.getAutorRoles().size()) {
      for (int i = 0; i < dto.getAutorIds().size(); i++) {
        Long autorId = dto.getAutorIds().get(i);
        String rol = dto.getAutorRoles().get(i);
        // Validar que el autor exista usando AutorService
        autorService.obtenerEntidadAutorPorId(autorId)
            .orElseThrow(() -> new IllegalArgumentException("Autor no encontrado con ID: " + autorId));
        autoresObras.add(new AutorObra(obra.getId(), autorId, rol));
      }
    }

    // 5. Manejar Géneros (crear relaciones en ObraGenero)
    if (dto.getGeneroIds() != null) {
      for (Long generoId : dto.getGeneroIds()) {
        // Validar que el género exista usando GeneroService
        generoService.obtenerEntidadGeneroPorId(generoId)
            .orElseThrow(() -> new IllegalArgumentException("Género no encontrado con ID: " + generoId));
        obrasGeneros.add(new ObraGenero(obra.getId(), generoId));
      }
    }

    // 6. Mapear Entidad a ResponseDTO y poblar campos de relación
    return convertirAObraResponseDTO(obra);
  }

  @Override
  public Optional<ObraResponseDTO> obtenerObraPorId(Long id) {
    return obras.stream()
        .filter(o -> o.getId().equals(id))
        .map(this::convertirAObraResponseDTO)
        .findFirst();
  }

  @Override
  public List<ObraResponseDTO> obtenerTodasLasObras() {
    return obras.stream()
        .map(this::convertirAObraResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<ObraResponseDTO> actualizarObra(Long id, ObraRequestDTO dto) {
    Optional<Obra> obraOpt = obras.stream().filter(o -> o.getId().equals(id)).findFirst();
    if (obraOpt.isEmpty()) {
      return Optional.empty();
    }
    Obra obraExistente = obraOpt.get();

    // 1. Actualizar campos básicos usando el mapper
    obraMapper.updateObraFromDto(dto, obraExistente);

    // 2. Actualizar Editorial si el ID cambió
    if (!obraExistente.getEditorial().getId().equals(dto.getEditorialId())) {
      Editorial nuevaEditorial = editorialService.obtenerEntidadEditorialPorId(dto.getEditorialId())
          .orElseThrow(() -> new IllegalArgumentException("Editorial no encontrada con ID: " + dto.getEditorialId()));
      obraExistente.setEditorial(nuevaEditorial);
    }
    obraExistente.setFechaActualizacion(LocalDate.now());

    // 3. Actualizar relaciones de Autores
    autoresObras.removeIf(ao -> ao.getObraId().equals(id));
    if (dto.getAutorIds() != null && dto.getAutorRoles() != null &&
        dto.getAutorIds().size() == dto.getAutorRoles().size()) {
      for (int i = 0; i < dto.getAutorIds().size(); i++) {
        Long autorId = dto.getAutorIds().get(i);
        String rol = dto.getAutorRoles().get(i);
        autorService.obtenerEntidadAutorPorId(autorId)
            .orElseThrow(() -> new IllegalArgumentException("Autor no encontrado con ID: " + autorId));
        autoresObras.add(new AutorObra(obraExistente.getId(), autorId, rol));
      }
    }

    // 4. Actualizar relaciones de Géneros
    obrasGeneros.removeIf(og -> og.getObraId().equals(id));
    if (dto.getGeneroIds() != null) {
      for (Long generoId : dto.getGeneroIds()) {
        generoService.obtenerEntidadGeneroPorId(generoId)
            .orElseThrow(() -> new IllegalArgumentException("Género no encontrado con ID: " + generoId));
        obrasGeneros.add(new ObraGenero(obraExistente.getId(), generoId));
      }
    }
    return Optional.of(convertirAObraResponseDTO(obraExistente));
  }

  @Override
  public boolean eliminarObra(Long id, boolean estadoObra, String motivo) {
    boolean removed = obras.removeIf(o -> o.getId().equals(id));
    if (removed) {
      autoresObras.removeIf(ao -> ao.getObraId().equals(id));
      obrasGeneros.removeIf(og -> og.getObraId().equals(id));
    }
    return removed;
  }

  @Override
  public Optional<Obra> obtenerEntidadObraPorId(Long id) {
    return obras.stream()
        .filter(o -> o.getId().equals(id))
        .findFirst();
  }

  private ObraResponseDTO convertirAObraResponseDTO(Obra obra) {
    ObraResponseDTO dto = obraMapper.obraToObraResponseDTO(obra);

    // Poblar Autores
    List<AutorResponseDTO> autoresDeObraDTO = autoresObras.stream()
        .filter(ao -> ao.getObraId().equals(obra.getId()))
        .map(ao -> {
          return autorService.obtenerEntidadAutorPorId(ao.getAutorId());
        })
        .filter(Optional::isPresent) // Filtra los Optional que están vacíos
        .map(Optional::get) // Extrae el Autor del Optional
        .map(autorMapper::autorToAutorResponseDTO) // Mapea Autor a AutorResponseDTO
        .collect(Collectors.toList());
    dto.setAutores(autoresDeObraDTO);

    // Poblar Géneros
    List<GeneroResponseDTO> generosDeObraDTO = obrasGeneros.stream()
        .filter(og -> og.getObraId().equals(obra.getId()))
        .map(og -> {
          return ((GeneroServiceImpl) generoService).obtenerEntidadGeneroPorId(og.getGeneroId());
        })
        .filter(Optional::isPresent) // Filtra los Optional que están vacíos
        .map(Optional::get) // Extrae el Genero del Optional
        .map(generoMapper::generoToGeneroResponseDTO) // Mapea Genero a GeneroResponseDTO
        .collect(Collectors.toList());
    dto.setGeneros(generosDeObraDTO);

    return dto;
  }

  @Override
  public long contarObras() {
    return obras.size();
  }

  @Override
  public long contarObrasNuevasMes() {
    LocalDateTime inicioMes = YearMonth.now().atDay(1).atStartOfDay();
    LocalDateTime finMes = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);

    return obras.stream()
        .filter(o -> o.getFechaCreacion() != null)
        .filter(o -> !o.getFechaCreacion().isBefore(inicioMes.toLocalDate())
            && !o.getFechaCreacion().isAfter(finMes.toLocalDate()))
        .count();
  }

  @Override
  public long contarPrestamos() {
    return /* prestamos.size(); */ 0;
  }

  @Override
  public long contarPrestamosMes() {
    LocalDateTime inicioMes = YearMonth.now().atDay(1).atStartOfDay();
    LocalDateTime finMes = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);

    /*
     * return prestamos.stream()
     * .filter(p -> p.getFechaPrestamo() != null)
     * .filter(p -> !p.getFechaPrestamo().isBefore(inicioMes) &&
     * !p.getFechaPrestamo().isAfter(finMes))
     * .count();
     */
    return 0;
  }

  @Override
  public Map<String, Integer> obtenerPrestamosPorMes() {
    Map<String, Integer> prestamosPorMes = new LinkedHashMap<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

    // Obtener los últimos 6 meses
    YearMonth mesActual = YearMonth.now();

    for (int i = 5; i >= 0; i--) {
      YearMonth mes = mesActual.minusMonths(i);
      String nombreMes = mes.format(formatter);

      LocalDateTime inicioMes = mes.atDay(1).atStartOfDay();
      LocalDateTime finMes = mes.atEndOfMonth().atTime(23, 59, 59);

      long count = /*
                    * prestamos.stream()
                    * .filter(p -> p.getFechaPrestamo() != null)
                    * .filter(p -> !p.getFechaPrestamo().isBefore(inicioMes) &&
                    * !p.getFechaPrestamo().isAfter(finMes))
                    * .count();
                    */ 0;

          prestamosPorMes.put(nombreMes, (int) count);
    }

    return prestamosPorMes;
  }

  @Override
  public Map<String, Integer> obtenerObrasPorGenero() {
    Map<String, Integer> obrasPorGenero = new LinkedHashMap<>();

    // Agrupar obras por género usando la tabla de relación ObraGenero
    Map<Long, List<Obra>> obrasPorGeneroId = obras.stream()
        .collect(Collectors.groupingBy(
            obra -> obrasGeneros.stream()
                .filter(og -> og.getObraId().equals(obra.getId()))
                .findFirst()
                .map(ObraGenero::getGeneroId)
                .orElse(0L)));

    // Convertir a nombres de géneros
    obrasPorGeneroId.forEach((generoId, listaObras) -> {
      if (generoId > 0) {
        generoService.obtenerGeneroPorId(generoId).ifPresent(genero -> {
          obrasPorGenero.put(genero.getNombre(), listaObras.size());
        });
      }
    });

    return obrasPorGenero;
  }

  @Override
  public List<ObraPopularDTO> obtenerObrasPopulares(int limit) {
    // Contamos préstamos por obra
    Map<Long, Long> contadorPrestamos = new HashMap<>();
    /*
     * for (Prestamo prestamo : prestamos) {
     * Long obraId = prestamo.getObra().getId();
     * contadorPrestamos.put(obraId, contadorPrestamos.getOrDefault(obraId, 0L) +
     * 1);
     * }
     */

    // Crear lista de DTOs
    return contadorPrestamos.entrySet().stream()
        .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
        .limit(limit)
        .map(entry -> {
          Obra obra = buscarObraPorId(entry.getKey());
          if (obra != null) {
            return ObraPopularDTO.builder()
                .id(obra.getId())
                .titulo(obra.getTitulo())
                /* .autor(obra.getAutor() != null ? obra.getAutor().getNombre() : "Desconocido") */
                .prestamos(entry.getValue().intValue())
                .build();
          }
          return null;
        })
        .filter(dto -> dto != null)
        .collect(Collectors.toList());
  }

  // Método auxiliar para buscar una obra por ID
  private Obra buscarObraPorId(Long id) {
    return obras.stream()
        .filter(o -> o.getId().equals(id))
        .findFirst()
        .orElse(null);
  }
  
}
