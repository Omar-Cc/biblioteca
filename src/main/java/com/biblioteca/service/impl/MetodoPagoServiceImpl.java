package com.biblioteca.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.biblioteca.dto.comercial.MetodoPagoRequestDTO;
import com.biblioteca.dto.comercial.MetodoPagoResponseDTO;
import com.biblioteca.mapper.comercial.MetodoPagoMapper;
import com.biblioteca.models.comercial.MetodoPago;
import com.biblioteca.service.MetodoPagoService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetodoPagoServiceImpl implements MetodoPagoService {

  private final List<MetodoPago> metodosPago = new ArrayList<>();
  private final AtomicLong metodoPagoIdCounter = new AtomicLong(0);
  private final MetodoPagoMapper metodoPagoMapper;
  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;

  @PostConstruct
  public void initMetodosPagoData() {
    try {
      InputStream inputStream = resourceLoader.getResource("classpath:data/metodos-pago-data.json").getInputStream();
      List<MetodoPagoRequestDTO> metodosPagoDTO = objectMapper.readValue(inputStream,
          new TypeReference<List<MetodoPagoRequestDTO>>() {
          });
      metodosPagoDTO.forEach(this::crearMetodoPago);
      System.out.println("Datos iniciales de Métodos de Pago cargados desde JSON: " + metodosPago.size() + " métodos.");
    } catch (Exception e) {
      System.err.println("Error al cargar datos iniciales de métodos de pago desde JSON: " + e.getMessage());
      e.printStackTrace();

      // Si no se pueden cargar datos desde JSON, crear métodos de pago por defecto
      if (metodosPago.isEmpty()) {
        crearMetodosPagoPorDefecto();
      }
    }
  }

  private void crearMetodosPagoPorDefecto() {
    // Tarjeta de crédito
    crearMetodoPago(MetodoPagoRequestDTO.builder()
        .nombre("Tarjeta de Crédito")
        .descripcion("Pago con tarjeta de crédito Visa, MasterCard, American Express")
        .requiereAutorizacion(true)
        .activo(true)
        .build());

    // Tarjeta de débito
    crearMetodoPago(MetodoPagoRequestDTO.builder()
        .nombre("Tarjeta de Débito")
        .descripcion("Pago con tarjeta de débito bancaria")
        .requiereAutorizacion(true)
        .activo(true)
        .build());

    // PayPal
    crearMetodoPago(MetodoPagoRequestDTO.builder()
        .nombre("PayPal")
        .descripcion("Pago a través de la plataforma PayPal")
        .requiereAutorizacion(true)
        .activo(true)
        .build());

    // Transferencia bancaria
    crearMetodoPago(MetodoPagoRequestDTO.builder()
        .nombre("Transferencia Bancaria")
        .descripcion("Pago mediante transferencia bancaria. Se procesará una vez confirmado el pago.")
        .requiereAutorizacion(false)
        .activo(true)
        .build());

    // Código de promoción
    crearMetodoPago(MetodoPagoRequestDTO.builder()
        .nombre("Código de Promoción")
        .descripcion("Pago utilizando un código promocional o cupón de descuento")
        .requiereAutorizacion(false)
        .activo(true)
        .build());

    System.out.println("Métodos de pago por defecto creados: " + metodosPago.size() + " métodos.");
  }

  @Override
  public MetodoPagoResponseDTO crearMetodoPago(MetodoPagoRequestDTO metodoPagoDTO) {
    MetodoPago metodoPago = metodoPagoMapper.toEntity(metodoPagoDTO);
    metodoPago.setId(metodoPagoIdCounter.incrementAndGet());
    metodosPago.add(metodoPago);
    return metodoPagoMapper.toResponseDTO(metodoPago);
  }

  @Override
  public Optional<MetodoPagoResponseDTO> obtenerMetodoPagoPorId(Long id) {
    return metodosPago.stream()
        .filter(mp -> mp.getId().equals(id))
        .findFirst()
        .map(metodoPagoMapper::toResponseDTO);
  }

  @Override
  public List<MetodoPagoResponseDTO> obtenerTodosLosMetodosPago() {
    return metodosPago.stream()
        .map(metodoPagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<MetodoPagoResponseDTO> obtenerMetodosPagoActivos() {
    return metodosPago.stream()
        .filter(MetodoPago::isActivo)
        .map(metodoPagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<MetodoPagoResponseDTO> actualizarMetodoPago(Long id, MetodoPagoRequestDTO metodoPagoDTO) {
    return metodosPago.stream()
        .filter(mp -> mp.getId().equals(id))
        .findFirst()
        .map(metodoPago -> {
          metodoPagoMapper.updateEntityFromDTO(metodoPagoDTO, metodoPago);
          return metodoPagoMapper.toResponseDTO(metodoPago);
        });
  }

  @Override
  public boolean eliminarMetodoPago(Long id) {
    return metodosPago.removeIf(mp -> mp.getId().equals(id));
  }

  @Override
  public Optional<MetodoPago> obtenerEntidadMetodoPagoPorId(Long id) {
    return metodosPago.stream()
        .filter(mp -> mp.getId().equals(id))
        .findFirst();
  }

  @Override
  public boolean activarMetodoPago(Long id) {
    return obtenerEntidadMetodoPagoPorId(id)
        .map(metodoPago -> {
          metodoPago.setActivo(true);
          return true;
        })
        .orElse(false);
  }

  @Override
  public boolean desactivarMetodoPago(Long id) {
    return obtenerEntidadMetodoPagoPorId(id)
        .map(metodoPago -> {
          metodoPago.setActivo(false);
          return true;
        })
        .orElse(false);
  }

  @Override
  public boolean verificarRequiereAutorizacion(Long id) {
    return obtenerEntidadMetodoPagoPorId(id)
        .map(MetodoPago::isRequiereAutorizacion)
        .orElse(false);
  }

  @Override
  public Optional<MetodoPagoResponseDTO> obtenerMetodoPagoPorNombre(String nombre) {
    return metodosPago.stream()
        .filter(mp -> mp.getNombre().equalsIgnoreCase(nombre))
        .findFirst()
        .map(metodoPagoMapper::toResponseDTO);
  }
}