package com.biblioteca.mapper.comercial;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.biblioteca.dto.comercial.ItemCarritoRequestDTO;
import com.biblioteca.dto.comercial.ItemCarritoResponseDTO;
import com.biblioteca.mapper.ContenidoMapper;
import com.biblioteca.models.comercial.ItemCarrito;
import com.biblioteca.models.contenido.Contenido;

@Mapper(componentModel = "spring", uses = { ContenidoMapper.class })
public interface ItemCarritoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "precio", ignore = true) // Se establecerá según el precio actual del contenido
    @Mapping(target = "descuento", ignore = true) // Se establecerá según descuentos aplicables
    @Mapping(target = "carrito.id", source = "carritoId")
    @Mapping(target = "contenido", source = "contenidoId", qualifiedByName = "toContenidoProxy")
    ItemCarrito toEntity(ItemCarritoRequestDTO dto);

    @Mapping(target = "carritoId", source = "carrito.id")
    @Mapping(target = "contenidoId", source = "contenido.id")
    @Mapping(target = "contenidoTitulo", source = "contenido.obra.titulo")
    @Mapping(target = "contenidoTipoDescripcion", source = "contenido.tipo")
    @Mapping(target = "contenidoImagen", source = "contenido.portadaUrl")
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "total", ignore = true)
    ItemCarritoResponseDTO toResponseDTO(ItemCarrito entity);

    List<ItemCarritoResponseDTO> toResponseDTOList(List<ItemCarrito> entities);

    /**
     * Método que crea un proxy de Contenido para ser utilizado en el mapeo
     * hasta que el servicio pueda cargar el contenido real desde la base de datos.
     */
    @Named("toContenidoProxy")
    default Contenido toContenidoProxy(Long id) {
        if (id == null) {
            return null;
        }

        // Esta implementación es temporal y será reemplazada por el servicio
        // que cargará el contenido real según su tipo (LibroFisico, AudioLibro, etc.)
        return new ContenidoProxy(id);
    }

    @AfterMapping
    default void calculateTotals(ItemCarrito entity, @MappingTarget ItemCarritoResponseDTO dto) {
        Integer precio = entity.getPrecio() != null ? entity.getPrecio() : 0;
        Integer descuento = entity.getDescuento() != null ? entity.getDescuento() : 0;
        Integer cantidad = entity.getCantidad() != null ? entity.getCantidad() : 1;

        dto.setSubtotal(precio * cantidad);
        dto.setTotal((precio - descuento) * cantidad);
    }

    @AfterMapping
    default void setDefaultValues(ItemCarrito entity, @MappingTarget ItemCarritoResponseDTO dto) {
        // En caso de que los datos de contenido sean nulos, establecer valores
        // predeterminados
        if (dto.getContenidoTitulo() == null && entity.getContenido() != null
                && entity.getContenido().getObra() != null) {
            dto.setContenidoTitulo(entity.getContenido().getObra().getTitulo());
        }

        // Si aún es nulo, usar un valor por defecto
        if (dto.getContenidoTitulo() == null) {
            dto.setContenidoTitulo("Sin título");
        }
    }

    // Clase interna para resolver el problema de instanciación de clase abstracta
    public class ContenidoProxy extends Contenido {
        public ContenidoProxy(Long id) {
            super();
            setId(id);
        }
    }
}