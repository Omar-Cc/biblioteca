package com.biblioteca.mapper;

import com.biblioteca.dto.PerfilRequestDTO;
import com.biblioteca.dto.PerfilResponseDTO;
import com.biblioteca.dto.InformacionPerfilResponseDTO;
import com.biblioteca.models.acceso.Perfil;
import com.biblioteca.models.acceso.InformacionPerfil;
import com.biblioteca.models.acceso.Usuario;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", imports = {
        java.time.format.DateTimeFormatter.class,
        java.time.ZoneId.class,
        LocalDateTime.class
})
public interface PerfilMapper {

    // ========== MAPEO DE REQUEST DTO A ENTIDAD ==========
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "usuario", source = "usuario")
    @Mapping(target = "informacionPerfil", ignore = true)
    @Mapping(target = "fechaCreacion", expression = "java(java.time.LocalDate.now())")
    @Mapping(target = "fechaModificacion", expression = "java(java.time.LocalDate.now())")
    @Mapping(target = "ultimaActividad", expression = "java(LocalDateTime.now())")
    @Mapping(target = "vecesUsado", constant = "0")
    @Mapping(target = "ultimoUso", ignore = true)
    @Mapping(target = "idiomaPreferido", source = "dto.idiomaPreferido")
    Perfil perfilRequestDTOToPerfil(PerfilRequestDTO dto, Usuario usuario);

    // ========== MAPEO DE ENTIDAD A RESPONSE DTO ==========
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "nombreUsuario", source = "usuario.username")
    @Mapping(target = "idiomaPreferido", source = "idiomaPreferido")
    @Mapping(target = "horasLecturaTotal", source = "informacionPerfil.tiempoLecturaMinutos", qualifiedByName = "minutosAHoras") // Se calcula en el servicio
    @Mapping(target = "fechaCreacion", expression = "java(perfil.getFechaCreacion() != null ? perfil.getFechaCreacion().format(DateTimeFormatter.ISO_LOCAL_DATE) : null)")
    @Mapping(target = "fechaModificacion", expression = "java(perfil.getFechaModificacion() != null ? perfil.getFechaModificacion().format(DateTimeFormatter.ISO_LOCAL_DATE) : null)")
    @Mapping(target = "ultimaActividad", expression = "java(perfil.getUltimaActividad() != null ? perfil.getUltimaActividad().format(DateTimeFormatter.ofPattern(\"dd/MM/yyyy - HH:mm\")) : null)")
    @Mapping(target = "ultimoUso", expression = "java(perfil.getUltimoUso() != null ? perfil.getUltimoUso().format(DateTimeFormatter.ofPattern(\"dd/MM/yyyy - HH:mm\")) : null)")
    @Mapping(target = "informacionExtendida", source = "informacionPerfil")
    PerfilResponseDTO perfilToPerfilResponseDTO(Perfil perfil);

    // ========== MAPEO DE INFORMACIÓN PERFIL A DTO ==========
    @Mapping(target = "horasLecturaTotal", expression = "java(info.getTiempoLecturaMinutos() != null ? info.getTiempoLecturaMinutos() / 60 : 0)")
    @Mapping(target = "progresoMetaAnual", expression = "java(info.getProgresoMetaAnual())")
    @Mapping(target = "progresoMetaMensual", expression = "java(info.getProgresoMetaMensual())")
    @Mapping(target = "fechaCreacion", expression = "java(info.getFechaCreacion() != null ? info.getFechaCreacion().format(DateTimeFormatter.ofPattern(\"dd/MM/yyyy - HH:mm\")) : null)")
    @Mapping(target = "ultimaActualizacion", expression = "java(info.getUltimaActualizacion() != null ? info.getUltimaActualizacion().format(DateTimeFormatter.ofPattern(\"dd/MM/yyyy - HH:mm\")) : null)")
    @Mapping(target = "ultimaActividad", expression = "java(info.getUltimaActividad() != null ? info.getUltimaActividad().format(DateTimeFormatter.ofPattern(\"dd/MM/yyyy - HH:mm\")) : null)")
    @Mapping(target = "fechaUltimoReset", expression = "java(info.getFechaUltimoReset() != null ? info.getFechaUltimoReset().format(DateTimeFormatter.ofPattern(\"dd/MM/yyyy - HH:mm\")) : null)")
    InformacionPerfilResponseDTO informacionPerfilToResponseDTO(InformacionPerfil info);

    // ========== ACTUALIZACIÓN DE PERFIL ==========
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "informacionPerfil", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", expression = "java(java.time.LocalDate.now())")
    @Mapping(target = "ultimaActividad", expression = "java(LocalDateTime.now())")
    @Mapping(target = "vecesUsado", ignore = true)
    @Mapping(target = "ultimoUso", ignore = true)
    void updatePerfilFromDto(PerfilRequestDTO dto, @MappingTarget Perfil perfil);

    // ========== MÉTODO HELPER PARA CONVERSIÓN DE MINUTOS A HORAS ==========
    @org.mapstruct.Named("minutosAHoras")
    default Integer minutosAHoras(Integer minutos) {
        return minutos != null ? minutos / 60 : 0;
    }
}