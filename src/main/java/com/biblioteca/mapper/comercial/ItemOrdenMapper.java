package com.biblioteca.mapper.comercial;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.biblioteca.dto.comercial.ItemOrdenRequestDTO;
import com.biblioteca.dto.comercial.ItemOrdenResponseDTO;
import com.biblioteca.mapper.ContenidoMapper;
import com.biblioteca.models.comercial.ItemOrden;
import com.biblioteca.models.contenido.Contenido;

@Mapper(componentModel = "spring", uses = {ContenidoMapper.class})
public interface ItemOrdenMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "orden.id", source = "ordenId")
  @Mapping(target = "contenido", source = "contenidoId", qualifiedByName = "toContenidoProxy")
  ItemOrden toEntity(ItemOrdenRequestDTO dto);

  @Mapping(target = "ordenId", source = "orden.id")
  @Mapping(target = "contenidoId", source = "contenido.id")
  @Mapping(target = "contenidoTitulo", source = "contenido.obra.titulo")
  @Mapping(target = "contenidoTipoDescripcion", source = "contenido.tipo")
  @Mapping(target = "contenidoImagen", source = "contenido.portadaUrl")
  @Mapping(target = "subtotal", ignore = true)
  @Mapping(target = "total", ignore = true)
  ItemOrdenResponseDTO toResponseDTO(ItemOrden entity);

  List<ItemOrdenResponseDTO> toResponseDTOList(List<ItemOrden> entities);

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
      // que cargará el contenido real según su tipo
      return new ContenidoProxy(id);
  }

  @AfterMapping
  default void calculateTotals(ItemOrden entity, @MappingTarget ItemOrdenResponseDTO dto) {
    Integer precio = entity.getPrecioAlComprar() != null ? entity.getPrecioAlComprar() : 0;
    Integer descuento = entity.getDescuentoAplicado() != null ? entity.getDescuentoAplicado() : 0;
    Integer cantidad = entity.getCantidad() != null ? entity.getCantidad() : 1;

    dto.setSubtotal(precio * cantidad);
    dto.setTotal((precio - descuento) * cantidad);
  }
  
  // Clase interna para resolver el problema de instanciación de clase abstracta
  public class ContenidoProxy extends Contenido {
      public ContenidoProxy(Long id) {
          super();
          setId(id);
      }
  }
}