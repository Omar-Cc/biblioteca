package com.biblioteca.mapper;

import com.biblioteca.dto.AutorRequestDTO;
import com.biblioteca.dto.AutorResponseDTO;
import com.biblioteca.models.Autor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AutorMapper {
    AutorResponseDTO autorToAutorResponseDTO(Autor autor);

    @Mapping(target = "id", ignore = true)
    Autor autorRequestDTOToAutor(AutorRequestDTO autorRequestDTO);
    List<AutorResponseDTO> autorsToAutorResponseDTOs(List<Autor> autors);

    @Mapping(target = "id", ignore = true)
    void updateAutorFromDto(AutorRequestDTO dto, @MappingTarget Autor entity);
}