package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AllArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "mangas")
@PrimaryKeyJoinColumn(name = "publicacion_ilustrada_id")
public class Manga extends PublicacionIlustradaFisica {
	private String sentidoLectura; // "derecha_izquierda", "izquierda_derecha"
	private String demografico; // "shonen", "shojo", "seinen", "josei"
	private boolean esColorido;
	private String origen; // "japones", "manhwa", "manhua", "occidental"
}