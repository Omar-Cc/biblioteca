package com.biblioteca.service;

import com.biblioteca.dto.ActividadRecienteDTO;
import com.biblioteca.dto.UsuarioAdminDTO;
import com.biblioteca.dto.UsuarioDataDTO;
import com.biblioteca.dto.UsuarioRegistroDTO;
import com.biblioteca.models.acceso.Usuario;

import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UsuarioService extends UserDetailsService {
    Usuario registrarUsuario(UsuarioRegistroDTO registroDTO);

    Optional<Usuario> buscarPorUsername(String username);

    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorId(Long id);

    // Actualizar datos básicos del usuario
    Usuario actualizarDatosUsuario(String username, UsuarioDataDTO datosActualizados);

    // Método genérico para guardar/actualizar un usuario, útil para otros servicios
    Usuario actualizarUsuario(Usuario usuario);

    // Cambiar contraseña
    boolean cambiarPassword(String username, String passwordActual, String nuevaPassword);

    // Listar usuarios (para admin)
    List<UsuarioAdminDTO> listarTodosLosUsuariosAdmin();

    // Activar/desactivar usuario
    boolean toggleEstadoUsuario(Long id);

    UsuarioAdminDTO crearUsuarioConRoles(UsuarioAdminDTO usuarioDTO);

    boolean eliminarCuenta(String username, String password);

    // Métodos para el dashboard
    long contarUsuarios();

    long contarUsuariosNuevosMes();

    List<ActividadRecienteDTO> obtenerActividadesRecientes(int limit);

    boolean tieneUsuarioSuscripcionActiva(Long usuarioId);

    Optional<String> obtenerPlanActualUsuario(Long usuarioId);

    long contarSuscripcionesActivas();

    long contarUsuariosConSuscripcionActiva();

    /**
     * Actualizar usuario completo desde panel de administración
     */
    UsuarioAdminDTO actualizarUsuarioAdmin(UsuarioAdminDTO usuarioDTO);
    
    /**
     * Actualizar solo los roles de un usuario
     */
    void actualizarRolesUsuario(Long usuarioId, Set<String> nuevosRoles);
    
}