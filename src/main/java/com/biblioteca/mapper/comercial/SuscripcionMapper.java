package com.biblioteca.mapper.comercial;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.biblioteca.dto.comercial.SuscripcionRequestDTO;
import com.biblioteca.dto.comercial.SuscripcionResponseDTO;
import com.biblioteca.models.comercial.Suscripcion;

@Mapper(uses = {MetodoPagoMapper.class})
public interface SuscripcionMapper {
    
    @Mapping(target = "usuario.id", source = "usuarioId")
    @Mapping(target = "plan.id", source = "planId")
    @Mapping(target = "metodoPago.id", source = "metodoPagoId")
    @Mapping(target = "historialPagos", ignore = true)
    Suscripcion toEntity(SuscripcionRequestDTO dto);
    
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "planId", source = "plan.id")
    @Mapping(target = "metodoPagoId", source = "metodoPago.id")
    @Mapping(target = "usuarioNombre", source = "usuario.email") // Asumiendo que el nombre visible es el email
    @Mapping(target = "planNombre", source = "plan.nombre")
    @Mapping(target = "metodoPagoNombre", source = "metodoPago.nombre")
    SuscripcionResponseDTO toResponseDTO(Suscripcion entity);
    
    List<SuscripcionResponseDTO> toResponseDTOList(List<Suscripcion> entities);
    
    @Mapping(target = "usuario.id", source = "usuarioId")
    @Mapping(target = "plan.id", source = "planId")
    @Mapping(target = "metodoPago.id", source = "metodoPagoId")
    @Mapping(target = "historialPagos", ignore = true)
    void updateEntityFromDTO(SuscripcionRequestDTO dto, @MappingTarget Suscripcion entity);
}