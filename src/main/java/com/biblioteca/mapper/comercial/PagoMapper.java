package com.biblioteca.mapper.comercial;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.biblioteca.dto.comercial.PagoRequestDTO;
import com.biblioteca.dto.comercial.PagoResponseDTO;
import com.biblioteca.models.comercial.Pago;

@Mapper(uses = {MetodoPagoMapper.class})
public interface PagoMapper {
    
    @Mapping(target = "fechaPago", ignore = true) // Se establecerá al crear
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orden.id", source = "ordenId")
    @Mapping(target = "metodoPago.id", source = "metodoPagoId")
    Pago toEntity(PagoRequestDTO dto);
    
    @Mapping(target = "ordenId", source = "orden.id")
    @Mapping(target = "metodoPagoId", source = "metodoPago.id")
    @Mapping(target = "metodoPagoNombre", source = "metodoPago.nombre")
    PagoResponseDTO toResponseDTO(Pago entity);
    
    List<PagoResponseDTO> toResponseDTOList(List<Pago> entities);
    
    @Mapping(target = "fechaPago", ignore = true) // No se actualiza
    @Mapping(target = "id", ignore = true) // No se actualiza
    @Mapping(target = "orden.id", source = "ordenId")
    @Mapping(target = "metodoPago.id", source = "metodoPagoId")
    void updateEntityFromDTO(PagoRequestDTO dto, @MappingTarget Pago entity);
}