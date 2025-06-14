package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contenidos_series")
@IdClass(ContenidoSerieId.class)
public class ContenidoSerie {
    @Id
    @Column(name = "serie_id")
    private Long serieId;
    
    @Id
    @Column(name = "contenido_id")
    private Long contenidoId;
    
    private int orden; // Orden del contenido dentro de la serie
    
    @ManyToOne
    @JoinColumn(name = "serie_id", insertable = false, updatable = false)
    private Serie serie;
    
    @ManyToOne
    @JoinColumn(name = "contenido_id", insertable = false, updatable = false)
    private Contenido contenido;
    
    // Constructor que solo recibe los valores de clave primaria y orden
    public ContenidoSerie(Long serieId, Long contenidoId, int orden) {
        this.serieId = serieId;
        this.contenidoId = contenidoId;
        this.orden = orden;
    }
}