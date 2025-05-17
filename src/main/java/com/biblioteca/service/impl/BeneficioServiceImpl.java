package com.biblioteca.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.biblioteca.dto.comercial.BeneficioRequestDTO;
import com.biblioteca.dto.comercial.BeneficioResponseDTO;
import com.biblioteca.mapper.comercial.BeneficioMapper;
import com.biblioteca.models.comercial.Beneficio;
import com.biblioteca.service.BeneficioService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BeneficioServiceImpl implements BeneficioService {

  private final List<Beneficio> beneficios = new ArrayList<>();
  private final AtomicLong beneficioIdCounter = new AtomicLong(0);
  private final BeneficioMapper beneficioMapper;
  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;

  @PostConstruct
  public void initBeneficiosData() {
    try {
      InputStream inputStream = resourceLoader.getResource("classpath:data/beneficios-data.json").getInputStream();
      List<BeneficioRequestDTO> beneficiosDTOs = objectMapper.readValue(inputStream,
          new TypeReference<List<BeneficioRequestDTO>>() {
          });
      beneficiosDTOs.forEach(this::crearBeneficio);
      System.out.println("Datos iniciales de Beneficios cargados desde JSON: " + beneficios.size() + " beneficios.");
    } catch (Exception e) {
      System.err.println("Error al cargar datos iniciales de beneficios desde JSON: " + e.getMessage());
      e.printStackTrace();

      // Si no se pueden cargar datos desde JSON, crear beneficios por defecto
      if (beneficios.isEmpty()) {
        crearBeneficiosPorDefecto();
      }
    }
  }

  private void crearBeneficiosPorDefecto() {
    // Beneficios básicos
    crearBeneficio(BeneficioRequestDTO.builder()
        .nombre("Acceso a libros digitales")
        .descripcion("Acceso ilimitado a la biblioteca digital")
        .icono("book")
        .tipoDato("booleano")
        .categoriaId(1L)
        .activo(true)
        .build());

    crearBeneficio(BeneficioRequestDTO.builder()
        .nombre("Préstamos simultáneos")
        .descripcion("Número de libros que se pueden prestar a la vez")
        .icono("list")
        .tipoDato("numerico")
        .categoriaId(1L)
        .activo(true)
        .build());

    crearBeneficio(BeneficioRequestDTO.builder()
        .nombre("Descarga de contenido")
        .descripcion("Posibilidad de descargar contenido para lectura offline")
        .icono("download")
        .tipoDato("booleano")
        .categoriaId(2L)
        .activo(true)
        .build());

    crearBeneficio(BeneficioRequestDTO.builder()
        .nombre("Acceso a contenido exclusivo")
        .descripcion("Acceso a material y lanzamientos exclusivos")
        .icono("star")
        .tipoDato("booleano")
        .categoriaId(2L)
        .activo(true)
        .build());

    crearBeneficio(BeneficioRequestDTO.builder()
        .nombre("Duración máxima de préstamo")
        .descripcion("Días máximos de préstamo")
        .icono("calendar")
        .tipoDato("numerico")
        .categoriaId(1L)
        .activo(true)
        .build());

    System.out.println("Beneficios por defecto creados: " + beneficios.size() + " beneficios.");
  }

  @Override
  public BeneficioResponseDTO crearBeneficio(BeneficioRequestDTO beneficioDTO) {
    Beneficio beneficio = beneficioMapper.toEntity(beneficioDTO);
    beneficio.setId(beneficioIdCounter.incrementAndGet());
    beneficios.add(beneficio);
    return beneficioMapper.toResponseDTO(beneficio);
  }

  @Override
  public Optional<BeneficioResponseDTO> obtenerBeneficioPorId(Long id) {
    return beneficios.stream()
        .filter(b -> b.getId().equals(id))
        .findFirst()
        .map(beneficioMapper::toResponseDTO);
  }

  @Override
  public List<BeneficioResponseDTO> obtenerTodosLosBeneficios() {
    return beneficios.stream()
        .map(beneficioMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<BeneficioResponseDTO> actualizarBeneficio(Long id, BeneficioRequestDTO beneficioDTO) {
    return beneficios.stream()
        .filter(b -> b.getId().equals(id))
        .findFirst()
        .map(beneficio -> {
          beneficioMapper.updateEntityFromDTO(beneficioDTO, beneficio);
          return beneficioMapper.toResponseDTO(beneficio);
        });
  }

  @Override
  public boolean eliminarBeneficio(Long id) {
    return beneficios.removeIf(b -> b.getId().equals(id));
  }

  @Override
  public Optional<Beneficio> obtenerEntidadBeneficioPorId(Long id) {
    return beneficios.stream()
        .filter(b -> b.getId().equals(id))
        .findFirst();
  }

  @Override
  public List<BeneficioResponseDTO> obtenerBeneficiosPorTipo(String tipoDato) {
    return beneficios.stream()
        .filter(b -> tipoDato.equals(b.getTipoDato()))
        .map(beneficioMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<BeneficioResponseDTO> obtenerBeneficiosActivos() {
    return beneficios.stream()
        .filter(Beneficio::isActivo)
        .map(beneficioMapper::toResponseDTO)
        .collect(Collectors.toList());
  }
}