package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import com.biblioteca.enums.FormatoFisico;

import lombok.AllArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "contenidos_fisicos")
@Inheritance(strategy = InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name = "contenido_id")
public abstract class ContenidoFisico extends Contenido {
    private int stockDisponible;
    private int minStock;
    private String ubicacionFisica; // Descripción o código de ubicación
    
    @Enumerated(EnumType.STRING)
    private FormatoFisico formatoFisico;
}