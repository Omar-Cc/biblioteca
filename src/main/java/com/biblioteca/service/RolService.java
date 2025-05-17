package com.biblioteca.service;

import com.biblioteca.models.Rol;
import java.util.Optional;
import java.util.List;

public interface RolService {
    Rol guardarRol(Rol rol);
    Optional<Rol> buscarPorNombre(String nombre);
    List<Rol> obtenerTodosLosRoles();
    void inicializarRoles();
}