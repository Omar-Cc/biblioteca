package com.biblioteca.mapper;

import com.biblioteca.dto.UsuarioAdminDTO;
import com.biblioteca.dto.UsuarioRegistroDTO;
import com.biblioteca.models.acceso.Rol;
import com.biblioteca.models.acceso.Usuario;
import com.biblioteca.models.comercial.Suscripcion;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {PerfilMapper.class})
public interface UsuarioMapper {

    // ========== MAPEO DE REGISTRO DTO A ENTIDAD ==========
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "emailVerificado", constant = "false")
    @Mapping(target = "fechaRegistro", expression = "java(java.time.LocalDateTime.now(java.time.ZoneId.of(\"America/Lima\")))")
    @Mapping(target = "ultimaActividad", expression = "java(java.time.LocalDateTime.now(java.time.ZoneId.of(\"America/Lima\")))")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true) // Se maneja en el servicio con encoding
    @Mapping(target = "ultimaActualizacionPassword", ignore = true)
    @Mapping(target = "intentosFallidosLogin", constant = "0")
    @Mapping(target = "fechaBloqueo", ignore = true)
    @Mapping(target = "haUsadoPeriodoPrueba", constant = "false")
    @Mapping(target = "fechaPrimerPeriodoPrueba", ignore = true)
    @Mapping(target = "idiomaPreferido", constant = "ES")
    @Mapping(target = "zonaHoraria", constant = "America/Lima")
    @Mapping(target = "notificacionesGlobalesActivas", constant = "true")
    @Mapping(target = "recibirNewsletters", constant = "false")
    @Mapping(target = "recibirPromociones", constant = "false")
    @Mapping(target = "lector", ignore = true)
    @Mapping(target = "suscripciones", ignore = true)
    @Mapping(target = "perfiles", ignore = true)
    @Mapping(target = "perfilPrincipal", ignore = true)
    @Mapping(target = "sesiones", ignore = true)
    Usuario usuarioRegistroDTOToUsuario(UsuarioRegistroDTO dto);

    // ========== MAPEO DE ENTIDAD A ADMIN DTO ==========
    @Mapping(target = "password", ignore = true) // La contraseña no se expone
    @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesToString")
    @Mapping(source = "suscripciones", target = "planActual", qualifiedByName = "suscripcionesToPlanNombre")
    @Mapping(source = "suscripciones", target = "estadoSuscripcion", qualifiedByName = "suscripcionesToEstado")
    @Mapping(source = "suscripciones", target = "fechaVencimientoSuscripcion", qualifiedByName = "suscripcionesToFechaVencimiento")
    @Mapping(source = "perfiles", target = "perfiles")
    @Mapping(source = "perfilPrincipal.id", target = "perfilPrincipalId")
    UsuarioAdminDTO usuarioToUsuarioAdminDTO(Usuario usuario);

    // ========== MÉTODOS AUXILIARES PARA MAPEO ==========
    @Named("rolesToString")
    default Set<String> rolesToString(Set<Rol> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(Rol::getNombre)
                .collect(Collectors.toSet());
    }

    @Named("suscripcionesToPlanNombre")
    default String suscripcionesToPlanNombre(Set<Suscripcion> suscripciones) {
        if (suscripciones == null || suscripciones.isEmpty()) {
            return "Sin Plan";
        }
        return suscripciones.stream()
                .filter(s -> "ACTIVA".equals(s.getEstado()))
                .findFirst()
                .map(s -> s.getPlan() != null ? s.getPlan().getNombre() : "Sin Plan")
                .orElse("Sin Plan");
    }

    @Named("suscripcionesToEstado")
    default String suscripcionesToEstado(Set<Suscripcion> suscripciones) {
        if (suscripciones == null || suscripciones.isEmpty()) {
            return "SIN_SUSCRIPCION";
        }
        return suscripciones.stream()
                .filter(s -> "ACTIVA".equals(s.getEstado()))
                .findFirst()
                .map(Suscripcion::getEstado)
                .orElse("INACTIVA");
    }

    @Named("suscripcionesToFechaVencimiento")
    default java.time.LocalDateTime suscripcionesToFechaVencimiento(Set<Suscripcion> suscripciones) {
        if (suscripciones == null || suscripciones.isEmpty()) {
            return null;
        }
        return suscripciones.stream()
                .filter(s -> "ACTIVA".equals(s.getEstado()))
                .findFirst()
                .map(s -> s.getFechaRenovacion() != null ? s.getFechaRenovacion().atStartOfDay() : null)
                .orElse(null);
    }
}