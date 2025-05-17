package com.biblioteca.mapper;

import com.biblioteca.dto.UsuarioRegistroDTO;
import com.biblioteca.models.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "fechaRegistro", expression = "java(java.time.LocalDateTime.now(java.time.ZoneId.of(\"America/Lima\")))")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    Usuario usuarioRegistroDTOToUsuario(UsuarioRegistroDTO dto);
}