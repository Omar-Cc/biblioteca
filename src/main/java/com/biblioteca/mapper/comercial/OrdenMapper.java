package com.biblioteca.mapper.comercial;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.biblioteca.dto.comercial.OrdenRequestDTO;
import com.biblioteca.dto.comercial.OrdenResponseDTO;
import com.biblioteca.models.comercial.ItemOrden;
import com.biblioteca.models.comercial.Orden;
import com.biblioteca.models.comercial.Pago;

@Mapper(uses = { ItemOrdenMapper.class, PagoMapper.class, FacturaMapper.class })
public interface OrdenMapper {

  @Mapping(target = "fechaCreacion", ignore = true) // Se establecerá al crear
  @Mapping(target = "estadoOrden", constant = "Pendiente") // Estado inicial
  @Mapping(target = "totalOrden", ignore = true) // Se calculará
  @Mapping(target = "items", ignore = true) // No se establecen los items al crear
  @Mapping(target = "pagos", ignore = true) // No se establecen los pagos al crear
  @Mapping(target = "perfil.id", source = "perfilId")
  @Mapping(target = "carrito.id", source = "carritoId")
  @Mapping(target = "factura", ignore = true) // No se establece al crear
  Orden toEntity(OrdenRequestDTO dto);

  @Mapping(target = "perfilId", source = "perfil.id")
  @Mapping(target = "carritoId", source = "carrito.id")
  @Mapping(target = "perfilNombre", source = "perfil.nombreVisible")
  @Mapping(target = "cantidadItems", ignore = true)
  @Mapping(target = "subtotal", ignore = true)
  @Mapping(target = "totalDescuentos", ignore = true)
  @Mapping(target = "totalPagado", ignore = true)
  @Mapping(target = "saldoPendiente", ignore = true)
  OrdenResponseDTO toResponseDTO(Orden entity);

  List<OrdenResponseDTO> toResponseDTOList(List<Orden> entities);

  @AfterMapping
  default void calculateTotals(Orden entity, @MappingTarget OrdenResponseDTO dto) {
    if (entity.getItems() != null) {
      dto.setCantidadItems(entity.getItems().size());

      int subtotal = 0;
      int descuentos = 0;

      for (ItemOrden item : entity.getItems()) {
        Integer itemPrecio = item.getPrecioAlComprar() != null ? item.getPrecioAlComprar() : 0;
        Integer itemDescuento = item.getDescuentoAplicado() != null ? item.getDescuentoAplicado() : 0;
        Integer cantidad = item.getCantidad() != null ? item.getCantidad() : 1;

        subtotal += (itemPrecio * cantidad);
        descuentos += (itemDescuento * cantidad);
      }

      dto.setSubtotal(subtotal);
      dto.setTotalDescuentos(descuentos);

      int totalPagado = 0;
      if (entity.getPagos() != null) {
        for (Pago pago : entity.getPagos()) {
          if ("Completado".equals(pago.getEstado())) {
            totalPagado += pago.getMonto() != null ? pago.getMonto() : 0;
          }
        }
      }

      dto.setTotalPagado(totalPagado);
      int totalOrden = subtotal - descuentos;
      dto.setSaldoPendiente(totalOrden - totalPagado);
    }
  }
}