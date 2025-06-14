package com.biblioteca.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.biblioteca.dto.GeneroRequestDTO;
import com.biblioteca.dto.GeneroResponseDTO;
import com.biblioteca.models.contenido.Genero;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GeneroMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "padre", ignore = true)
  @Mapping(target = "subgeneros", ignore = true)
  @Mapping(target = "obras", ignore = true)
  Genero generoRequestDTOToGenero(GeneroRequestDTO dto);

  @Mapping(target = "parentId", source = "padre.id")
  @Mapping(target = "subgeneros", ignore = true)
  GeneroResponseDTO generoToGeneroResponseDTO(Genero genero);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "padre", ignore = true)
  @Mapping(target = "subgeneros", ignore = true)
  @Mapping(target = "obras", ignore = true)
  void updateGeneroFromDto(GeneroRequestDTO dto, @MappingTarget Genero genero);
}