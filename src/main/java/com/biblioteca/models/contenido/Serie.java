package com.biblioteca.models.contenido;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "series")
public class Serie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private String descripcion;
    private int numeroVolumenes;
    private boolean completa;
    
    @OneToMany(mappedBy = "serie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ContenidoSerie> contenidos = new HashSet<>();

    public void addContenidoSerie(ContenidoSerie contenidoSerie) {
        this.contenidos.add(contenidoSerie);
        contenidoSerie.setSerie(this);
    }

    public void removeContenidoSerie(ContenidoSerie contenidoSerie) {
        this.contenidos.remove(contenidoSerie);
        contenidoSerie.setSerie(null);
    }
}