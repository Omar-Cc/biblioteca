package com.biblioteca.service;

import com.biblioteca.dto.PerfilRequestDTO;
import com.biblioteca.dto.PerfilResponseDTO;
import com.biblioteca.models.acceso.Perfil;

import java.util.List;
import java.util.Optional;

public interface PerfilService {
    PerfilResponseDTO crearPerfil(PerfilRequestDTO perfilDto, String username);

    Optional<PerfilResponseDTO> obtenerPerfilPorIdYUsuario(Long id, String username);

    List<PerfilResponseDTO> obtenerPerfilesPorUsuario(String username);

    Optional<Perfil> obtenerEntidadPerfilPorId(Long id);

    Optional<PerfilResponseDTO> actualizarPerfil(Long id, PerfilRequestDTO perfilDto, String username);

    boolean eliminarPerfil(Long id, String username);

    long contarPerfilesPorUsuario(String username);

    boolean existePerfilPrincipal(String username);

    Optional<PerfilResponseDTO> obtenerPerfilPrincipal(String username);

    void actualizarFechaUltimoActividad(Long id);
}

/*
 * Servicio de notificaciones
 * public void enviarNotificacion(Perfil perfil, TipoNotificacion tipo, String
 * mensaje) {
 * // 1. Verificar si el perfil tiene habilitado este tipo de notificación
 * if (!perfilAceptaTipoNotificacion(perfil, tipo)) {
 * return;
 * }
 * 
 * // 2. Verificar si el lector tiene habilitadas las notificaciones globalmente
 * Lector lector = perfil.getUsuario().getLector();
 * if (lector == null || !lector.getNotificacionesHabilitadas()) {
 * return;
 * }
 * 
 * // 3. Usar SIEMPRE el email del Usuario
 * String email = perfil.getUsuario().getEmail();
 * 
 * // 4. Enviar según preferencias del lector
 * if (lector.getNotificacionesEmail()) {
 * emailService.enviar(email, mensaje);
 * }
 * 
 * if (lector.getNotificacionesSMS() && lector.getTelefono() != null) {
 * smsService.enviar(lector.getTelefono(), mensaje);
 * }
 * }
 */