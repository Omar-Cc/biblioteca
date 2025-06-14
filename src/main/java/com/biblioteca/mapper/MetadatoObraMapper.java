package com.biblioteca.mapper;

import com.biblioteca.dto.MetadatoObraRequestDTO;
import com.biblioteca.dto.MetadatoObraResponseDTO;
import com.biblioteca.models.contenido.MetadatoObra;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MetadatoObraMapper {

  @Mapping(target = "obraId", ignore = true)
  @Mapping(target = "obra", ignore = true)
  MetadatoObra metadatoObraRequestDTOToMetadatoObra(MetadatoObraRequestDTO dto);

  MetadatoObraResponseDTO metadatoObraToMetadatoObraResponseDTO(MetadatoObra metadatoObra);

  List<MetadatoObraResponseDTO> metadatosToMetadatoResponseDTOs(List<MetadatoObra> metadatos);
}