package com.biblioteca.service;

import com.biblioteca.dto.PerfilRequestDTO;
import com.biblioteca.dto.PerfilResponseDTO;
import com.biblioteca.models.Perfil;

import java.util.List;
import java.util.Optional;

public interface PerfilService {
    PerfilResponseDTO crearPerfil(PerfilRequestDTO perfilDto, String username);

    Optional<PerfilResponseDTO> obtenerPerfilPorIdYUsuario(Long id, String username);

    List<PerfilResponseDTO> obtenerPerfilesPorUsuario(String username);

    Optional<Perfil> obtenerPerfilPorId(Long id);

    Optional<PerfilResponseDTO> actualizarPerfil(Long id, PerfilRequestDTO perfilDto, String username);

    boolean eliminarPerfil(Long id, String username);

    long contarPerfilesPorUsuario(String username);

    void asegurarUnPerfilPrincipal(String username, Long perfilIdActual);

    boolean existePerfilPrincipal(String username);

    Optional<PerfilResponseDTO> obtenerPerfilPrincipal(String username);

    void actualizarFechaUltimoActividad(Long id);
}