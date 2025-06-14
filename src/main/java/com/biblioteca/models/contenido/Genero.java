package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "generos")
@EqualsAndHashCode(exclude = { "subgeneros", "obras" })
@ToString(exclude = { "subgeneros", "obras" })
public class Genero {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nombre;
	private String descripcion;
	private Integer nivel;

	// Relación padre
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Genero padre;

	// Relación hijos (subgéneros)
	@OneToMany(mappedBy = "padre", cascade = CascadeType.ALL, orphanRemoval = false)
	@Builder.Default
	private List<Genero> subgeneros = new ArrayList<>();

	@ManyToMany(mappedBy = "generos", fetch = FetchType.LAZY)
	@Builder.Default
	private Set<Obra> obras = new HashSet<>();

	// Métodos helper para mantener la consistencia bidireccional
	public void agregarSubgenero(Genero subgenero) {
		this.subgeneros.add(subgenero);
		subgenero.setPadre(this);
	}

	public void removerSubgenero(Genero subgenero) {
		this.subgeneros.remove(subgenero);
		subgenero.setPadre(null);
	}
}