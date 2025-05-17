package com.biblioteca.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Autor {
    private Long id;
    private String nombre;
    private String biografia;
    private LocalDate fechaNacimiento;
    private String nacionalidad;
    private String foto;
}
