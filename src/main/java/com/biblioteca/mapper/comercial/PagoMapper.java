package com.biblioteca.mapper.comercial;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.biblioteca.dto.comercial.PagoRequestDTO;
import com.biblioteca.dto.comercial.PagoResponseDTO;
import com.biblioteca.models.comercial.Pago;

@Mapper(uses = { MetodoPagoMapper.class })
public interface PagoMapper {

    @Mapping(target = "fechaPago", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaProcesamiento", ignore = true)
    @Mapping(target = "transaccionId", ignore = true)
    @Mapping(target = "codigoRespuesta", ignore = true)
    @Mapping(target = "mensaje", ignore = true)
    @Mapping(target = "moneda", ignore = true)
    // IGNORAR todas las relaciones - se asignarán manualmente en el Service
    @Mapping(target = "orden", ignore = true)
    @Mapping(target = "suscripcion", ignore = true)
    @Mapping(target = "metodoPago", ignore = true)
    Pago toEntity(PagoRequestDTO dto);

    @Mapping(target = "ordenId", source = "orden.id")
    @Mapping(target = "suscripcionId", source = "suscripcion.id")
    @Mapping(target = "metodoPagoId", source = "metodoPago.id")
    @Mapping(target = "metodoPagoNombre", source = "metodoPago.nombre")
    @Mapping(target = "periodo", ignore = true)
    PagoResponseDTO toResponseDTO(Pago entity);

    List<PagoResponseDTO> toResponseDTOList(List<Pago> entities);

    @Mapping(target = "fechaPago", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orden", ignore = true)
    @Mapping(target = "suscripcion", ignore = true)
    @Mapping(target = "metodoPago", ignore = true)
    void updateEntityFromDTO(PagoRequestDTO dto, @MappingTarget Pago entity);
}