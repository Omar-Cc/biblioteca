package com.biblioteca.mapper;

import com.biblioteca.dto.AutorRequestDTO;
import com.biblioteca.dto.AutorResponseDTO;
import com.biblioteca.models.contenido.Autor;
import com.biblioteca.models.contenido.AutorObra;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface AutorMapper {

	AutorResponseDTO autorToAutorResponseDTO(Autor autor);

	@Mapping(target = "id", ignore = true)
	Autor autorRequestDTOToAutor(AutorRequestDTO autorRequestDTO);

	List<AutorResponseDTO> autorsToAutorResponseDTOs(List<Autor> autors);

	@Mapping(target = "id", ignore = true)
	void updateAutorFromDto(AutorRequestDTO dto, @MappingTarget Autor entity);

	default List<String> mapAutorRoles(Set<AutorObra> autoresObras) {
		if (autoresObras == null)
			return null;
		return autoresObras.stream()
				.map(AutorObra::getRol)
				.collect(java.util.stream.Collectors.toList());
	}
}