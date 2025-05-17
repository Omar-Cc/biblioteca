package com.biblioteca.mapper.comercial;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.biblioteca.dto.comercial.CarritoRequestDTO;
import com.biblioteca.dto.comercial.CarritoResponseDTO;
import com.biblioteca.models.comercial.Carrito;
import com.biblioteca.models.comercial.ItemCarrito;

@Mapper(uses = { ItemCarritoMapper.class })
public interface CarritoMapper {

    @Mapping(target = "id", ignore = true) // Se generará automáticamente
    @Mapping(target = "fechaCreacion", ignore = true) // Se establecerá al crear
    @Mapping(target = "items", ignore = true) // No se establecen los items al crear
    @Mapping(target = "perfil", ignore = true) // Se establecerá con la relación
    Carrito toEntity(CarritoRequestDTO dto);

    @Mapping(target = "perfilNombre", source = "perfil.nombreVisible")
    @Mapping(target = "cantidadItems", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "totalDescuentos", ignore = true)
    @Mapping(target = "total", ignore = true)
    CarritoResponseDTO toResponseDTO(Carrito entity);

    List<CarritoResponseDTO> toResponseDTOList(List<Carrito> entities);

    @AfterMapping
    default void calculateTotals(Carrito entity, @MappingTarget CarritoResponseDTO dto) {
        if (entity.getItems() != null) {
            dto.setCantidadItems(entity.getItems().size());

            int subtotalCalc = 0;
            int descuentosCalc = 0;

            for (ItemCarrito item : entity.getItems()) {
                Integer itemPrecio = item.getPrecio() != null ? item.getPrecio() : 0;
                // Descuento unitario del item
                Integer itemDescuento = item.getDescuento() != null ? item.getDescuento() : 0;
                Integer cantidad = item.getCantidad() != null ? item.getCantidad() : 1;

                subtotalCalc += (itemPrecio * cantidad); // Suma de (precio original unitario * cantidad)
                descuentosCalc += (itemDescuento * cantidad); // Suma de (descuento unitario * cantidad)
            }

            dto.setSubtotal(subtotalCalc);
            dto.setTotalDescuentos(descuentosCalc);
            dto.setTotal(subtotalCalc - descuentosCalc);
        } else {
            // Si no hay items, los totales deben ser 0
            dto.setCantidadItems(0);
            dto.setSubtotal(0);
            dto.setTotalDescuentos(0);
            dto.setTotal(0);
        }
    }
}