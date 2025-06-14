package com.biblioteca.models.acceso;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "empleados")
@PrimaryKeyJoinColumn(name = "usuario_id") // Vincula con la tabla usuarios
public class Empleado extends Usuario {
    private String departamento;
    private String cargo;
}
