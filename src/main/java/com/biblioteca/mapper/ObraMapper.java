package com.biblioteca.mapper;

import com.biblioteca.dto.ObraRequestDTO;
import com.biblioteca.dto.ObraResponseDTO;
import com.biblioteca.models.contenido.Obra;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
    AutorMapper.class,
    EditorialMapper.class,
    GeneroMapper.class,
    MetadatoObraMapper.class,
    RelacionObraMapper.class
})
public interface ObraMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "editorial", ignore = true)
  @Mapping(target = "fechaCreacion", ignore = true)
  @Mapping(target = "fechaActualizacion", ignore = true)
  @Mapping(target = "disponible", ignore = true)
  @Mapping(target = "motivoEliminacion", ignore = true)
  @Mapping(target = "obraOriginal", ignore = true)
  @Mapping(target = "ediciones", ignore = true)
  @Mapping(target = "metadatos", ignore = true)
  @Mapping(target = "obrasRelacionadas", ignore = true)
  @Mapping(target = "referenciadaPor", ignore = true)
  @Mapping(target = "autoresObras", ignore = true)
  @Mapping(target = "generos", ignore = true)
  Obra obraRequestDTOToObra(ObraRequestDTO obraRequestDTO);

  @Mapping(target = "editorialId", source = "editorial.id")
  @Mapping(target = "autorIds", ignore = true)
  @Mapping(target = "autorRoles", ignore = true)
  @Mapping(target = "generoIds", ignore = true)
  @Mapping(target = "obraOriginalId", source = "obraOriginal.id")
  @Mapping(target = "metadatos", ignore = true)
  @Mapping(target = "obrasRelacionadas", ignore = true)
  ObraRequestDTO obraToObraRequestDTO(Obra obra);

  @Mapping(source = "editorial", target = "editorial")
  @Mapping(target = "autores", ignore = true)
  @Mapping(source = "generos", target = "generos")
  @Mapping(target = "obraOriginalId", source = "obraOriginal.id")
  @Mapping(target = "ediciones", ignore = true)
  @Mapping(target = "metadatos", ignore = true)
  @Mapping(target = "obrasRelacionadas", source = "obrasRelacionadas")
  @Mapping(target = "referenciadaPor", source = "referenciadaPor")
  ObraResponseDTO obraToObraResponseDTO(Obra obra);

  List<ObraResponseDTO> obrasToObraResponseDTOs(List<Obra> obras);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "editorial", ignore = true)
  @Mapping(target = "fechaCreacion", ignore = true)
  @Mapping(target = "fechaActualizacion", ignore = true)
  @Mapping(target = "disponible", ignore = true)
  @Mapping(target = "motivoEliminacion", ignore = true)
  @Mapping(target = "obraOriginal", ignore = true)
  @Mapping(target = "ediciones", ignore = true)
  @Mapping(target = "metadatos", ignore = true)
  @Mapping(target = "obrasRelacionadas", ignore = true)
  @Mapping(target = "referenciadaPor", ignore = true)
  @Mapping(target = "autoresObras", ignore = true)
  @Mapping(target = "generos", ignore = true)
  void updateObraFromDto(ObraRequestDTO dto, @MappingTarget Obra entity);
}