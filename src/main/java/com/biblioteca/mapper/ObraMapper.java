package com.biblioteca.mapper;

import com.biblioteca.dto.ObraRequestDTO;
import com.biblioteca.dto.ObraResponseDTO;
import com.biblioteca.models.Obra;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = { AutorMapper.class, EditorialMapper.class, GeneroMapper.class })
public interface ObraMapper {

  @Mapping(target = "id", ignore = true) // El ID se genera al guardar o no se modifica desde el DTO
  @Mapping(target = "editorial", ignore = true) // Se manejará en el servicio usando editorialId de ObraRequestDTO
  @Mapping(target = "fechaCreacion", ignore = true) // Se establece en el servicio
  @Mapping(target = "fechaActualizacion", ignore = true) // Se establece en el servicio
  Obra obraRequestDTOToObra(ObraRequestDTO obraRequestDTO);

  @Mapping(target = "editorialId", source = "editorial.id")
  @Mapping(target = "autorIds", ignore = true) // Se manejará en el servicio
  @Mapping(target = "autorRoles", ignore = true) // Se manejará en el servicio
  @Mapping(target = "generoIds", ignore = true) // Se manejará en el servicio
  ObraRequestDTO obraToObraRequestDTO(Obra obra);

  @Mapping(source = "editorial", target = "editorial")
  @Mapping(target = "autores", ignore = true) // Se poblará en el servicio
  @Mapping(target = "generos", ignore = true) // Se poblará en el servicio
  ObraResponseDTO obraToObraResponseDTO(Obra obra);

  List<ObraResponseDTO> obrasToObraResponseDTOs(List<Obra> obras);

  @Mapping(target = "id", ignore = true) // No actualizar ID
  @Mapping(target = "editorial", ignore = true) // Se manejará en el servicio
  @Mapping(target = "fechaCreacion", ignore = true)
  @Mapping(target = "fechaActualizacion", ignore = true) // Se actualizará en el servicio
  void updateObraFromDto(ObraRequestDTO dto, @MappingTarget Obra entity);

}