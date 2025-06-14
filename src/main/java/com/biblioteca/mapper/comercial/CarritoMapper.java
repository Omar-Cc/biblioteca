package com.biblioteca.mapper.comercial;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.biblioteca.dto.comercial.CarritoRequestDTO;
import com.biblioteca.dto.comercial.CarritoResponseDTO;
import com.biblioteca.models.comercial.Carrito;

@Mapper(componentModel = "spring", uses = { ItemCarritoMapper.class })
public interface CarritoMapper {
    
    @Mapping(target = "id", ignore = true) // Se generará automáticamente
    @Mapping(target = "fechaCreacion", ignore = true) // Se establecerá al crear
    @Mapping(target = "items", ignore = true) // No se establecen los items al crear
    @Mapping(target = "perfil", ignore = true) // Se establecerá con la relación
    // Agregar mappings para los nuevos campos
    @Mapping(target = "fechaUltimaModificacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "fechaExpiracion", ignore = true)
    @Mapping(target = "limiteItems", ignore = true)
    @Mapping(target = "version", ignore = true)
    Carrito toEntity(CarritoRequestDTO dto);

    @Mapping(target = "perfilNombre", source = "perfil.nombreVisible")
    @Mapping(target = "perfilId", source = "perfil.id")
    @Mapping(target = "cantidadItems", expression = "java(entity.getCantidadTotalItems())")
    @Mapping(target = "subtotal", expression = "java(entity.getSubtotalCarrito())")
    @Mapping(target = "totalDescuentos", expression = "java(entity.getTotalDescuentosCarrito())")
    @Mapping(target = "total", expression = "java(entity.getTotalCarrito())")
    // Agregar mappings para los nuevos campos del DTO
    @Mapping(target = "tieneItemsNoDisponibles", ignore = true)
    @Mapping(target = "requiresActualizacionPrecios", ignore = true)
    @Mapping(target = "itemsRestantes", ignore = true)
    @Mapping(target = "ahorroTotal", ignore = true)
    @Mapping(target = "promocionesAplicadas", ignore = true)
    @Mapping(target = "costoEnvioEstimado", ignore = true)
    @Mapping(target = "fechaEntregaEstimada", ignore = true)
    CarritoResponseDTO toResponseDTO(Carrito entity);

	List<CarritoResponseDTO> toResponseDTOList(List<Carrito> entities);
}