package com.biblioteca.mapper.comercial;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.biblioteca.dto.comercial.BeneficioRequestDTO;
import com.biblioteca.dto.comercial.BeneficioResponseDTO;
import com.biblioteca.models.comercial.Beneficio;

@Mapper
public interface BeneficioMapper {
    
    Beneficio toEntity(BeneficioRequestDTO dto);
    
    BeneficioResponseDTO toResponseDTO(Beneficio entity);
    
    List<BeneficioResponseDTO> toResponseDTOList(List<Beneficio> entities);
    
    void updateEntityFromDTO(BeneficioRequestDTO dto, @MappingTarget Beneficio entity);
}