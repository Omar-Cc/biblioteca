package com.biblioteca.mapper.comercial;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.biblioteca.dto.comercial.MetodoPagoRequestDTO;
import com.biblioteca.dto.comercial.MetodoPagoResponseDTO;
import com.biblioteca.models.comercial.MetodoPago;

@Mapper(componentModel = "spring")
public interface MetodoPagoMapper {

    @Mapping(target = "id", ignore = true)
    MetodoPago toEntity(MetodoPagoRequestDTO dto);

    MetodoPagoResponseDTO toResponseDTO(MetodoPago entity);

    List<MetodoPagoResponseDTO> toResponseDTOList(List<MetodoPago> entities);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDTO(MetodoPagoRequestDTO dto, @MappingTarget MetodoPago entity);
}