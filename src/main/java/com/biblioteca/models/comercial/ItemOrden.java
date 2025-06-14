package com.biblioteca.models.comercial;

import com.biblioteca.models.contenido.Contenido;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Data
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "items_orden")
public class ItemOrden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id")
    private Orden orden;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contenido_id")
    private Contenido contenido;
    
    private Integer cantidad;
    private Integer precioAlComprar; // En centavos
    private Integer descuentoAplicado; // En centavos
}