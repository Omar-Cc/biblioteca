package com.biblioteca.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.biblioteca.dto.SerieRequestDTO;
import com.biblioteca.dto.SerieResponseDTO;
import com.biblioteca.models.contenido.Serie;
import com.biblioteca.repositories.SerieRepository;
import com.biblioteca.service.SerieService;

@Service
public class SerieServiceImpl implements SerieService {

  @Autowired
  private SerieRepository serieRepository;

  @Override
  public SerieResponseDTO crearSerie(SerieRequestDTO serieRequestDTO) {
    Serie serie = Serie.builder()
        .nombre(serieRequestDTO.getNombre())
        .descripcion(serieRequestDTO.getDescripcion())
        .numeroVolumenes(serieRequestDTO.getNumeroVolumenes())
        .completa(serieRequestDTO.isCompleta())
        .build();

    Serie serieGuardada = serieRepository.save(serie);
    return convertirAResponseDTO(serieGuardada);
  }

  @Override
  public Optional<SerieResponseDTO> obtenerSeriePorId(Long id) {
    return serieRepository.findById(id)
        .map(this::convertirAResponseDTO);
  }

  @Override
  public List<SerieResponseDTO> obtenerTodasLasSeries() {
    return serieRepository.findAll().stream()
        .map(this::convertirAResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<SerieResponseDTO> actualizarSerie(Long id, SerieRequestDTO serieRequestDTO) {
    return serieRepository.findById(id)
        .map(serie -> {
          serie.setNombre(serieRequestDTO.getNombre());
          serie.setDescripcion(serieRequestDTO.getDescripcion());
          serie.setNumeroVolumenes(serieRequestDTO.getNumeroVolumenes());
          serie.setCompleta(serieRequestDTO.isCompleta());

          Serie serieActualizada = serieRepository.save(serie);
          return convertirAResponseDTO(serieActualizada);
        });
  }

  @Override
  public boolean eliminarSerie(Long id) {
    if (serieRepository.existsById(id)) {
      serieRepository.deleteById(id);
      return true;
    }
    return false;
  }

  @Override
  public Optional<Serie> obtenerEntidadSeriePorId(Long id) {
    return serieRepository.findById(id);
  }

  @Override
  public List<SerieResponseDTO> obtenerSeriesCompletas() {
    return serieRepository.findAll().stream()
        .filter(Serie::isCompleta)
        .map(this::convertirAResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<SerieResponseDTO> obtenerSeriesEnProgreso() {
    return serieRepository.findAll().stream()
        .filter(serie -> !serie.isCompleta())
        .map(this::convertirAResponseDTO)
        .collect(Collectors.toList());
  }

  /**
   * Convierte una entidad Serie a SerieResponseDTO.
   *
   * @param serie La entidad Serie a convertir.
   * @return El DTO de respuesta correspondiente.
   */
  private SerieResponseDTO convertirAResponseDTO(Serie serie) {
    return SerieResponseDTO.builder()
        .id(serie.getId())
        .nombre(serie.getNombre())
        .descripcion(serie.getDescripcion())
        .numeroVolumenes(serie.getNumeroVolumenes())
        .completa(serie.isCompleta())
        .build();
  }
}