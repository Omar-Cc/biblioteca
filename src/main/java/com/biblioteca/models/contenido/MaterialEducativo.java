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
@Table(name = "materiales_educativos")
@PrimaryKeyJoinColumn(name = "contenido_digital_id")
public class MaterialEducativo extends ContenidoDigital {
	private String nivelEducativo; // "primaria", "secundaria", "universitario"
	private String asignatura; // "matemáticas", "historia", "ciencias"
	private String recursos; // "ejercicios", "videos", "simulaciones"
	private String grupoEdad; // "6-8 años", "9-12 años", etc.
	private boolean requiereSupervisor;
}