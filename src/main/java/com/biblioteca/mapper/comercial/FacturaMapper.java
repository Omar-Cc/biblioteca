package com.biblioteca.mapper.comercial;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.biblioteca.dto.comercial.FacturaRequestDTO;
import com.biblioteca.dto.comercial.FacturaResponseDTO;
import com.biblioteca.models.comercial.Factura;

@Mapper
public interface FacturaMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "fechaEmision", expression = "java(java.time.LocalDate.now())")
  @Mapping(target = "orden.id", source = "ordenId")
  Factura toEntity(FacturaRequestDTO dto);

  @Mapping(target = "ordenId", source = "orden.id")
  @Mapping(target = "fechaOrden", source = "orden.fechaCreacion")
  @Mapping(target = "estadoOrden", source = "orden.estadoOrden")
  @Mapping(target = "clienteNombre", source = "orden.perfil.nombreVisible")
  @Mapping(target = "clienteEmail", source = "orden.perfil.usuario.email")
  @Mapping(target = "perfilId", source = "orden.perfil.id")
  FacturaResponseDTO toResponseDTO(Factura entity);

  List<FacturaResponseDTO> toResponseDTOList(List<Factura> entities);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "fechaEmision", ignore = true)
  @Mapping(target = "orden.id", source = "ordenId")
  void updateEntityFromDTO(FacturaRequestDTO dto, @MappingTarget Factura entity);
}