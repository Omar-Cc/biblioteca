package com.biblioteca.mapper.comercial;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.biblioteca.dto.comercial.MetodoPagoRequestDTO;
import com.biblioteca.dto.comercial.MetodoPagoResponseDTO;
import com.biblioteca.models.comercial.MetodoPago;

@Mapper
public interface MetodoPagoMapper {
    
    MetodoPago toEntity(MetodoPagoRequestDTO dto);
    
    MetodoPagoResponseDTO toResponseDTO(MetodoPago entity);
    
    List<MetodoPagoResponseDTO> toResponseDTOList(List<MetodoPago> entities);
    
    void updateEntityFromDTO(MetodoPagoRequestDTO dto, @MappingTarget MetodoPago entity);
}