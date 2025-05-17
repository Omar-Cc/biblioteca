package com.biblioteca.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.biblioteca.dto.LectorRequestDTO;
import com.biblioteca.dto.LectorResponseDTO;
import com.biblioteca.models.Lector;
import com.biblioteca.models.Usuario;

@Mapper(componentModel = "spring")
public interface LectorMapper {

    // Convertir Usuario a Lector con datos del DTO
    @Mapping(target = "id", source = "usuario.id")
    @Mapping(target = "username", source = "usuario.username")
    @Mapping(target = "password", source = "usuario.password")
    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "activo", source = "usuario.activo")
    @Mapping(target = "fechaRegistro", source = "usuario.fechaRegistro")
    @Mapping(target = "ultimaActividad", source = "usuario.ultimaActividad")
    @Mapping(target = "planId", source = "usuario.planId")
    @Mapping(target = "roles", source = "usuario.roles")
    @Mapping(target = "perfiles", source = "usuario.perfiles")
    @Mapping(target = "multasPendientes", constant = "0")
    @Mapping(target = "estadoCuenta", constant = "ACTIVO")
    Lector usuarioYDtoToLector(Usuario usuario, LectorRequestDTO dto);
    
    // Actualizar un Lector existente con datos del DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "ultimaActividad", ignore = true)
    @Mapping(target = "planId", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "perfiles", ignore = true)
    @Mapping(target = "multasPendientes", ignore = true)
    @Mapping(target = "estadoCuenta", ignore = true)
    void updateLectorFromDto(LectorRequestDTO dto, @MappingTarget Lector lector);
    
    // Convertir Lector a DTO de respuesta
    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "activo", source = "activo")
    LectorResponseDTO lectorToLectorResponseDTO(Lector lector);
}