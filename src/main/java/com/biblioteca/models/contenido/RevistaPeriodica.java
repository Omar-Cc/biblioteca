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
@Table(name = "revistas_periodicas")
@PrimaryKeyJoinColumn(name = "contenido_fisico_id")
public class RevistaPeriodica extends ContenidoFisico {
	private String periodicidad; // "semanal", "mensual", "trimestral"
	private String edicion; // "especial", "regular", "aniversario"
	private String issn; // International Standard Serial Number
	private int numeroRevista;
	private int volumen;
	private String temaPrincipal;
}