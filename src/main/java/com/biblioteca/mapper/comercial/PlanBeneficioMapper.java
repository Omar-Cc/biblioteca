package com.biblioteca.mapper.comercial;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.biblioteca.dto.comercial.PlanBeneficioRequestDTO;
import com.biblioteca.dto.comercial.PlanBeneficioResponseDTO;
import com.biblioteca.models.comercial.PlanBeneficio;

@Mapper(uses = {PlanMapper.class, BeneficioMapper.class})
public interface PlanBeneficioMapper {
    
        
    @Mapping(target = "plan.id", source = "planId")
    @Mapping(target = "beneficio.id", source = "beneficioId")
    PlanBeneficio toEntity(PlanBeneficioRequestDTO dto);
    
    @Mapping(target = "plan", source = "plan")
    @Mapping(target = "beneficio", source = "beneficio")
    PlanBeneficioResponseDTO toResponseDTO(PlanBeneficio entity);
    
    List<PlanBeneficioResponseDTO> toResponseDTOList(List<PlanBeneficio> entities);
    
    @Mapping(target = "plan.id", source = "planId")
    @Mapping(target = "beneficio.id", source = "beneficioId")
    void updateEntityFromDTO(PlanBeneficioRequestDTO dto, @MappingTarget PlanBeneficio entity);
}