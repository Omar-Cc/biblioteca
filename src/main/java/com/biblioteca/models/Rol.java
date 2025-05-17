package com.biblioteca.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rol {
    private Long id;
    private String nombre; // Ej: "ROLE_USER", "ROLE_ADMIN"
    private String descripcion;
}