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
@Table(name = "comics")
@PrimaryKeyJoinColumn(name = "publicacion_ilustrada_id")
public class Comic extends PublicacionIlustradaFisica {
	private boolean colorido;
	private String estilo; // "superheros", "europeo", "indie", "underground"
	private String universo; // "Marvel", "DC", "independiente"
	private boolean esLimitado; // Para ediciones limitadas
}