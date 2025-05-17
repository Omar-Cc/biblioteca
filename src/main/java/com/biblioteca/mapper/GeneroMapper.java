package com.biblioteca.mapper;

import com.biblioteca.dto.GeneroRequestDTO;
import com.biblioteca.dto.GeneroResponseDTO;
import com.biblioteca.models.Genero;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GeneroMapper {
  GeneroResponseDTO generoToGeneroResponseDTO(Genero genero);

  @Mapping(target = "id", ignore = true)
  Genero generoRequestDTOToGenero(GeneroRequestDTO generoRequestDTO);

  List<GeneroResponseDTO> generosToGeneroResponseDTOs(List<Genero> generos);

  
    @Mapping(target = "id", ignore = true)
    void updateGeneroFromDto(GeneroRequestDTO dto, @MappingTarget Genero entity);
}