package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import com.biblioteca.enums.FormatoFisico;

import lombok.AllArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class ContenidoFisico extends Contenido {
    private int stockDisponible;
    private int minStock;
    private String ubicacionFisica; // Descripción o código de ubicación
    private FormatoFisico formatoFisico;
}