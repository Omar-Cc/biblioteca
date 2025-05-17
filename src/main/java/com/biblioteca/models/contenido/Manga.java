package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AllArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Manga extends PublicacionIlustradaFisica {
    private String sentidoLectura; // "Oriental (derecha a izquierda)", "Occidental (izquierda a derecha)"
}