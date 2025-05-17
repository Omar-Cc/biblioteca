package com.biblioteca.models;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Lector extends Usuario {
    private String nombre; // Nombre específico del lector
    private String apellido; // Apellido específico del lector
    private String direccion;
    private String telefono;
    private Date fechaNacimiento;
    private Integer multasPendientes;
    private String estadoCuenta; // Ej: "ACTIVO", "SUSPENDIDO"

}
