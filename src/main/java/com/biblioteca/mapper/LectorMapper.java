package com.biblioteca.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.biblioteca.dto.LectorRequestDTO;
import com.biblioteca.dto.LectorResponseDTO;
import com.biblioteca.models.acceso.Lector;
import com.biblioteca.models.acceso.Usuario;

@Mapper(componentModel = "spring")
public interface LectorMapper {

    // Convertir Usuario a Lector con datos del DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "multasPendientes", constant = "0")
    @Mapping(target = "estadoCuenta", constant = "ACTIVO")
    Lector usuarioYDtoToLector(Usuario usuario, LectorRequestDTO dto);

    // Actualizar un Lector existente con datos del DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "multasPendientes", ignore = true)
    @Mapping(target = "estadoCuenta", ignore = true)
    void updateLectorFromDto(LectorRequestDTO dto, @MappingTarget Lector lector);

    // Convertir Lector a DTO de respuesta
    @Mapping(target = "id", source = "lector.id")
    @Mapping(target = "username", source = "lector.usuario.username")
    @Mapping(target = "email", source = "lector.usuario.email")
    @Mapping(target = "activo", source = "lector.usuario.activo")
    @Mapping(target = "fechaRegistro", source = "lector.usuario.fechaRegistro")
    @Mapping(target = "ultimaActividad", source = "lector.usuario.ultimaActividad")
    LectorResponseDTO lectorToLectorResponseDTO(Lector lector);
}