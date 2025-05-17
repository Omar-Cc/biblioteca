package com.biblioteca.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutorObra {
    private Long obraId; // FK a Obra
    private Long autorId; // FK a Autor
    private String rol; // "principal", "colaborador", "editor"
}