package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.biblioteca.enums.TipoRelacionObra;

import java.util.ArrayList;
import java.util.HashSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Obra {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String titulo;

	@Column(columnDefinition = "TEXT")
	private String descripcion;

	private int anioPublicacion;

	@Column(unique = true) // ISBN suele ser único
	private String isbn;

	@ManyToOne
	@JoinColumn(name = "editorial_id")
	private Editorial editorial;

	@ElementCollection
	@CollectionTable(name = "obra_idiomas", joinColumns = @JoinColumn(name = "obra_id"))
	@Column(name = "idioma")
	@Builder.Default
	private List<String> idioma = new ArrayList<>();

	@Builder.Default
	private Boolean disponible = true;
	private String motivoEliminacion;
	private LocalDate fechaCreacion;
	private LocalDate fechaActualizacion;

	// Versionado
	private String version; // "1a edición", "2a edición revisada", "edición especial"

	@ManyToOne
	@JoinColumn(name = "obra_original_id", insertable = false, updatable = false)
	private Obra obraOriginal;

	@OneToMany(mappedBy = "obraOriginal", cascade = CascadeType.ALL)
	@Builder.Default
	private Set<Obra> ediciones = new HashSet<>();

	// Metadatos
	@OneToMany(mappedBy = "obra", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<MetadatoObra> metadatos = new HashSet<>();

	// Relaciones con otras obras
	@OneToMany(mappedBy = "obraOrigen", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<RelacionObra> obrasRelacionadas = new HashSet<>();

	@OneToMany(mappedBy = "obraRelacionada")
	@Builder.Default
	private Set<RelacionObra> referenciadaPor = new HashSet<>();

	@OneToMany(mappedBy = "obra", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<AutorObra> autoresObras = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "obras_generos", joinColumns = @JoinColumn(name = "obra_id"), inverseJoinColumns = @JoinColumn(name = "genero_id"))
	@Builder.Default
	private Set<Genero> generos = new HashSet<>();

	// Método helper para agregar un autor con su rol
	public void addAutor(Autor autor, String rol) {
		AutorObra autorObra = AutorObra.builder()
				.obra(this)
				.autor(autor)
				.rol(rol)
				.build();
		this.autoresObras.add(autorObra);
		// Si la entidad Autor también gestiona la colección AutorObra,
		// se debería actualizar aquí también para mantener la bidireccionalidad.
		// autor.getAutoresObras().add(autorObra); // Ejemplo
	}

	public void removeAutor(Autor autor) {
		// Lógica para remover AutorObra basado en el autor
		this.autoresObras.removeIf(ao -> ao.getAutor().equals(autor) && ao.getObra().equals(this));
		// autor.getAutoresObras().removeIf(ao -> ao.getObra().equals(this)); // Ejemplo
		// si Autor gestiona la colección
	}

	// Métodos helper para la relación con Genero
	public void addGenero(Genero genero) {
		this.generos.add(genero);
		genero.getObras().add(this);
	}

	public void removeGenero(Genero genero) {
		this.generos.remove(genero);
		genero.getObras().remove(this);
	}

	// Métodos de utilidad
	public void addMetadato(String clave, String valor, String categoria) {
		MetadatoObra metadato = MetadatoObra.builder()
				.obraId(this.id)
				.clave(clave)
				.valor(valor)
				.categoria(categoria)
				.obra(this)
				.build();
		this.metadatos.add(metadato);
	}

	public void addRelacionObra(Obra obraRelacionada, TipoRelacionObra tipo, Integer orden) {
		RelacionObra relacion = RelacionObra.builder()
				.obraOrigenId(this.id)
				.obraRelacionadaId(obraRelacionada.getId())
				.tipoRelacion(tipo)
				.orden(orden)
				.obraOrigen(this)
				.obraRelacionada(obraRelacionada)
				.build();
		this.obrasRelacionadas.add(relacion);
	}

	public Long getObraOriginalId() {
		return obraOriginal != null ? obraOriginal.getId() : null;
	}
}
