package com.biblioteca.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObraGenero {
    private Long obraId; // FK a Obra
    private Long generoId; // FK a Genero
}