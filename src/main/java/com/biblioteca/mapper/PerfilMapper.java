package com.biblioteca.mapper;

import com.biblioteca.dto.PerfilRequestDTO;
import com.biblioteca.dto.PerfilResponseDTO;
import com.biblioteca.models.Perfil;
import com.biblioteca.models.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", imports = { java.time.format.DateTimeFormatter.class, java.time.ZoneId.class })
public interface PerfilMapper {

    @Mapping(target = "id", ignore = true) // El ID se genera en el servicio
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "usuario", source = "usuario") // Se espera que el Usuario ya esté asignado
    @Mapping(target = "fechaCreacion", ignore = true) // Se establece en el servicio
    @Mapping(target = "fechaModificacion", ignore = true) // Se establece en el servicio
    @Mapping(target = "ultimaActividad", ignore = true) // Se establece en el servicio
    Perfil perfilRequestDTOToPerfil(PerfilRequestDTO dto, Usuario usuario);

    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "nombreUsuario", source = "usuario.username")
    @Mapping(target = "fechaCreacion", expression = "java(perfil.getFechaCreacion() != null ? perfil.getFechaCreacion().format(DateTimeFormatter.ISO_LOCAL_DATE) : null)")
    @Mapping(target = "fechaModificacion", expression = "java(perfil.getFechaModificacion() != null ? perfil.getFechaModificacion().format(DateTimeFormatter.ISO_LOCAL_DATE) : null)")
    @Mapping(target = "ultimaActividad", expression = "java(perfil.getUltimaActividad() != null ? perfil.getUltimaActividad().format(java.time.format.DateTimeFormatter.ofPattern(\"dd/MM/yyyy - HH:mm\")) : null)")
    PerfilResponseDTO perfilToPerfilResponseDTO(Perfil perfil);

    @Mapping(target = "id", ignore = true) // No se actualiza el ID
    @Mapping(target = "usuario", ignore = true) // El usuario no se cambia en una actualización de perfil
    @Mapping(target = "activo", ignore = true) // El estado activo se maneja por separado si es necesario
    @Mapping(target = "fechaCreacion", ignore = true) // No se actualiza la fecha de creación
    @Mapping(target = "fechaModificacion", ignore = true) // Se establece en el servicio
    @Mapping(target = "ultimaActividad", ignore = true) // Se establece en el servicio
    void updatePerfilFromDto(PerfilRequestDTO dto, @MappingTarget Perfil perfil);
}