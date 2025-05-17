package com.biblioteca.mapper.comercial;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.biblioteca.dto.comercial.PlanRequestDTO;
import com.biblioteca.dto.comercial.PlanResponseDTO;
import com.biblioteca.models.comercial.Plan;

@Mapper
public interface PlanMapper {
    
    Plan toEntity(PlanRequestDTO dto);
    
    PlanResponseDTO toResponseDTO(Plan entity);
    
    List<PlanResponseDTO> toResponseDTOList(List<Plan> entities);
    
    void updateEntityFromDTO(PlanRequestDTO dto, @MappingTarget Plan entity);
}