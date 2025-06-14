package com.biblioteca.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.comercial.BeneficioRequestDTO;
import com.biblioteca.dto.comercial.BeneficioResponseDTO;
import com.biblioteca.mapper.comercial.BeneficioMapper;
import com.biblioteca.models.comercial.Beneficio;
import com.biblioteca.repositories.comercial.BeneficioRepository;
import com.biblioteca.service.BeneficioService;

@Service
public class BeneficioServiceImpl implements BeneficioService {

  private final BeneficioRepository beneficioRepository;
  private final BeneficioMapper beneficioMapper;

  public BeneficioServiceImpl(BeneficioRepository beneficioRepository, BeneficioMapper beneficioMapper) {
    this.beneficioRepository = beneficioRepository;
    this.beneficioMapper = beneficioMapper;
  }

  @Override
  @Transactional
  public BeneficioResponseDTO crearBeneficio(BeneficioRequestDTO beneficioDTO) {
    Beneficio beneficio = beneficioMapper.toEntity(beneficioDTO);
    // El ID será generado por la base de datos
    Beneficio beneficioGuardado = beneficioRepository.save(beneficio);
    return beneficioMapper.toResponseDTO(beneficioGuardado);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<BeneficioResponseDTO> obtenerBeneficioPorId(Long id) {
    return beneficioRepository.findById(id)
        .map(beneficioMapper::toResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BeneficioResponseDTO> obtenerTodosLosBeneficios() {
    return beneficioRepository.findAll().stream()
        .map(beneficioMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Optional<BeneficioResponseDTO> actualizarBeneficio(Long id, BeneficioRequestDTO beneficioDTO) {
    Optional<Beneficio> beneficioOpt = beneficioRepository.findById(id);
    if (beneficioOpt.isPresent()) {
      Beneficio beneficio = beneficioOpt.get();
      beneficioMapper.updateEntityFromDTO(beneficioDTO, beneficio);
      Beneficio beneficioActualizado = beneficioRepository.save(beneficio);
      return Optional.of(beneficioMapper.toResponseDTO(beneficioActualizado));
    }
    return Optional.empty();
  }

  @Override
  @Transactional
  public boolean eliminarBeneficio(Long id) {
    if (beneficioRepository.existsById(id)) {
      beneficioRepository.deleteById(id);
      return true;
    }
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Beneficio> obtenerEntidadBeneficioPorId(Long id) {
    return beneficioRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BeneficioResponseDTO> obtenerBeneficiosPorTipo(String tipoDato) {
    return beneficioRepository.findByTipoDato(tipoDato).stream()
        .map(beneficioMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<BeneficioResponseDTO> obtenerBeneficiosActivos() {
    return beneficioRepository.findByActivoTrue().stream()
        .map(beneficioMapper::toResponseDTO)
        .collect(Collectors.toList());
  }
}