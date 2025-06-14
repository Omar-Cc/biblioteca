package com.biblioteca.mapper.comercial;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.biblioteca.dto.comercial.ItemCarritoRequestDTO;
import com.biblioteca.dto.comercial.ItemCarritoResponseDTO;
import com.biblioteca.models.comercial.ItemCarrito;
import com.biblioteca.models.contenido.Contenido;

@Mapper(componentModel = "spring")
public interface ItemCarritoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "precio", ignore = true) // Se establecerá según el precio actual del contenido
    @Mapping(target = "descuento", ignore = true) // Se establecerá según descuentos aplicables
    @Mapping(target = "carrito.id", source = "carritoId")
    @Mapping(target = "contenido", source = "contenidoId", qualifiedByName = "toContenidoProxy")
    @Mapping(target = "cantidadMaxima", ignore = true)
    @Mapping(target = "fechaAgregado", ignore = true)
    @Mapping(target = "fechaUltimaModificacion", ignore = true)
    @Mapping(target = "motivoDescuento", ignore = true)
    @Mapping(target = "precioOriginal", ignore = true)
    public abstract ItemCarrito toEntity(ItemCarritoRequestDTO dto);

    @Mapping(target = "carritoId", source = "carrito.id")
    @Mapping(target = "contenidoId", source = "contenido.id")
    @Mapping(target = "contenidoTitulo", source = "contenido.obra.titulo")
    @Mapping(target = "contenidoTipoDescripcion", source = "contenido.tipo")
    @Mapping(target = "contenidoImagen", source = "contenido.portadaUrl")
    @Mapping(target = "subtotal", expression = "java(entity.getSubtotal())")
    @Mapping(target = "total", expression = "java(entity.getTotal())")
    @Mapping(target = "descuentoTotal", expression = "java(entity.getDescuentoTotal())")
    @Mapping(target = "contenidoAutor", ignore = true)
    @Mapping(target = "contenidoEditorial", ignore = true)
    @Mapping(target = "disponible", ignore = true)
    @Mapping(target = "esRegalo", ignore = true)
    @Mapping(target = "estadoItem", ignore = true)
    @Mapping(target = "notasEspeciales", ignore = true)
    @Mapping(target = "stockDisponible", ignore = true)
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

    // Clase interna para resolver el problema de instanciación de clase abstracta
    public static class ContenidoProxy extends Contenido {
        public ContenidoProxy(Long id) {
            super();
            setId(id);
        }
    }
}