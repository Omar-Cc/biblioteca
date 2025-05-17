package com.biblioteca.mapper.comercial;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.biblioteca.dto.comercial.HistorialPagoSuscripcionRequestDTO;
import com.biblioteca.dto.comercial.HistorialPagoSuscripcionResponseDTO;
import com.biblioteca.models.comercial.HistorialPagoSuscripcion;

@Mapper
public interface HistorialPagoSuscripcionMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "fechaPago", expression = "java(java.time.LocalDateTime.now())")
  @Mapping(target = "suscripcion.id", source = "suscripcionId")
  @Mapping(target = "pago.id", source = "pagoId")
  HistorialPagoSuscripcion toEntity(HistorialPagoSuscripcionRequestDTO dto);

  @Mapping(target = "suscripcionId", source = "suscripcion.id")
  @Mapping(target = "pagoId", source = "pago.id")
  HistorialPagoSuscripcionResponseDTO toResponseDTO(HistorialPagoSuscripcion entity);

  List<HistorialPagoSuscripcionResponseDTO> toResponseDTOList(List<HistorialPagoSuscripcion> entities);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "fechaPago", ignore = true)
  @Mapping(target = "suscripcion.id", source = "suscripcionId")
  @Mapping(target = "pago.id", source = "pagoId")
  void updateEntityFromDTO(HistorialPagoSuscripcionRequestDTO dto, @MappingTarget HistorialPagoSuscripcion entity);
}