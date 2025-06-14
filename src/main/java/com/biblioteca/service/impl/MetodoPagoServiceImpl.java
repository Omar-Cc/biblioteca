package com.biblioteca.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.comercial.MetodoPagoRequestDTO;
import com.biblioteca.dto.comercial.MetodoPagoResponseDTO;
import com.biblioteca.mapper.comercial.MetodoPagoMapper;
import com.biblioteca.models.comercial.MetodoPago;
import com.biblioteca.repositories.comercial.MetodoPagoRepository;
import com.biblioteca.service.MetodoPagoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetodoPagoServiceImpl implements MetodoPagoService {

  private final MetodoPagoRepository metodoPagoRepository;
  private final MetodoPagoMapper metodoPagoMapper;

  @Override
  @Transactional
  public MetodoPagoResponseDTO crearMetodoPago(MetodoPagoRequestDTO metodoPagoDTO) {
    MetodoPago metodoPago = metodoPagoMapper.toEntity(metodoPagoDTO);
    MetodoPago metodoPagoGuardado = metodoPagoRepository.save(metodoPago);
    return metodoPagoMapper.toResponseDTO(metodoPagoGuardado);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<MetodoPagoResponseDTO> obtenerMetodoPagoPorId(Long id) {
    return metodoPagoRepository.findById(id)
        .map(metodoPagoMapper::toResponseDTO);
  }

  @Override
  public List<MetodoPagoResponseDTO> obtenerTodosLosMetodosPago() {
    return metodoPagoRepository.findAll()
        .stream()
        .map(metodoPagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<MetodoPagoResponseDTO> obtenerMetodosPagoActivos() {
    return metodoPagoMapper.toResponseDTOList(metodoPagoRepository.findByActivoTrue());
  }

  @Override
  @Transactional
  public Optional<MetodoPagoResponseDTO> actualizarMetodoPago(Long id, MetodoPagoRequestDTO metodoPagoDTO) {
    return metodoPagoRepository.findById(id)
        .map(metodoPago -> {
          metodoPagoMapper.updateEntityFromDTO(metodoPagoDTO, metodoPago);
          MetodoPago metodoPagoActualizado = metodoPagoRepository.save(metodoPago);
          return metodoPagoMapper.toResponseDTO(metodoPagoActualizado);
        });
  }

  @Override
  @Transactional
  public boolean eliminarMetodoPago(Long id) {
    if (metodoPagoRepository.existsById(id)) {
      metodoPagoRepository.deleteById(id);
      return true;
    }
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<MetodoPago> obtenerEntidadMetodoPagoPorId(Long id) {
    return metodoPagoRepository.findById(id);
  }

  @Override
  @Transactional
  public boolean activarMetodoPago(Long id) {
    return metodoPagoRepository.findById(id)
        .map(metodoPago -> {
          metodoPago.setActivo(true);
          metodoPagoRepository.save(metodoPago);
          return true;
        })
        .orElse(false);
  }

  @Override
  @Transactional
  public boolean desactivarMetodoPago(Long id) {
    return metodoPagoRepository.findById(id)
        .map(metodoPago -> {
          metodoPago.setActivo(false);
          metodoPagoRepository.save(metodoPago);
          return true;
        })
        .orElse(false);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean verificarRequiereAutorizacion(Long id) {
    return metodoPagoRepository.findById(id)
        .map(MetodoPago::isRequiereAutorizacion)
        .orElse(false);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<MetodoPagoResponseDTO> obtenerMetodoPagoPorNombre(String nombre) {
    return metodoPagoRepository.findByNombre(nombre)
        .map(metodoPagoMapper::toResponseDTO);
  }
}