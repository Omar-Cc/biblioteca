package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;

import java.time.LocalDate;

import com.biblioteca.enums.TipoContenido;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Inheritance(strategy = InheritanceType.JOINED) // Estrategia de herencia
public abstract class Contenido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "obra_id") // Asumiendo que Obra es una entidad
    private Obra obra;

    @ManyToOne
    @JoinColumn(name = "editorial_id") // Asumiendo que Editorial es una entidad
    private Editorial editorial;

    private String portadaUrl; // URL o path a la imagen

    @Column(columnDefinition = "TEXT")
    private String sinopsis;

    private Integer precio; // En centavos

    private boolean enVenta;

    @Enumerated(EnumType.STRING)
    private TipoContenido tipo;

    private boolean puedeSerPrestado;

    private LocalDate fechaCreacion;
    
    private String isbn; // Puede ser específico de la edición/contenido

    @Builder.Default
    private boolean activo = true;
}
