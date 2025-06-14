package com.biblioteca.service;

import java.util.Optional;

import com.biblioteca.models.acceso.Rol;

import java.util.List;

public interface RolService {
    Rol guardarRol(Rol rol);
    Optional<Rol> buscarPorNombre(String nombre);
    List<Rol> obtenerTodosLosRoles();
    void inicializarRoles();
}