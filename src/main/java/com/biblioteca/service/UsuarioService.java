package com.biblioteca.service;

import com.biblioteca.dto.ActividadRecienteDTO;
import com.biblioteca.dto.UsuarioAdminDTO;
import com.biblioteca.dto.UsuarioDataDTO;
import com.biblioteca.dto.UsuarioRegistroDTO;
import com.biblioteca.models.Lector;
import com.biblioteca.models.Usuario;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UsuarioService extends UserDetailsService {
    Usuario registrarUsuario(UsuarioRegistroDTO registroDTO);

    Optional<Usuario> buscarPorUsername(String username);

    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorId(Long id);

    // Actualizar datos básicos del usuario
    Usuario actualizarUsuario(String username, UsuarioDataDTO datosActualizados);

    // Cambiar contraseña
    boolean cambiarPassword(String username, String passwordActual, String nuevaPassword);

    // Listar usuarios (para admin)
    List<Usuario> listarTodosLosUsuarios();

    // Activar/desactivar usuario
    boolean toggleEstadoUsuario(Long id);

    Usuario crearUsuarioConRoles(UsuarioAdminDTO usuarioDTO);

    void actualizarLector(Lector lector);

    boolean eliminarCuenta(String username, String password);
    
    // Métodos para el dashboard
    long contarUsuarios();
    long contarUsuariosNuevosMes();
    List<ActividadRecienteDTO> obtenerActividadesRecientes(int limit);
    
}