package com.biblioteca.mapper;

import com.biblioteca.dto.EditorialRequestDTO;
import com.biblioteca.dto.EditorialResponseDTO;
import com.biblioteca.models.Editorial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EditorialMapper {
  EditorialResponseDTO editorialToEditorialResponseDTO(Editorial editorial);

  @Mapping(target = "id", ignore = true)
  Editorial editorialRequestDTOToEditorial(EditorialRequestDTO editorialRequestDTO);

  List<EditorialResponseDTO> editorialsToEditorialResponseDTOs(List<Editorial> editorials);

  @Mapping(target = "id", ignore = true)
  void updateEditorialFromDto(EditorialRequestDTO dto, @MappingTarget Editorial entity);
}