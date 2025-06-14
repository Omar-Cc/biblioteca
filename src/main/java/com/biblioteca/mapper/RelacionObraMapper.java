package com.biblioteca.mapper;

import com.biblioteca.dto.RelacionObraRequestDTO;
import com.biblioteca.dto.RelacionObraResponseDTO;
import com.biblioteca.models.contenido.RelacionObra;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RelacionObraMapper {

  @Mapping(target = "obraOrigenId", ignore = true)
  @Mapping(target = "obraRelacionadaId", source = "obraRelacionadaId")
  @Mapping(target = "obraOrigen", ignore = true)
  @Mapping(target = "obraRelacionada", ignore = true)
  RelacionObra relacionObraRequestDTOToRelacionObra(RelacionObraRequestDTO dto);

  @Mapping(target = "tituloObraRelacionada", source = "obraRelacionada.titulo")
  RelacionObraResponseDTO relacionObraToRelacionObraResponseDTO(RelacionObra relacionObra);

  List<RelacionObraResponseDTO> relacionesObraToRelacionObraResponseDTOs(List<RelacionObra> relaciones);
}