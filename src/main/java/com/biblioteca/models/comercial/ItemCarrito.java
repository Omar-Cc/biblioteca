package com.biblioteca.models.comercial;

import com.biblioteca.models.contenido.Contenido;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "items_carrito")
public class ItemCarrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id")
    private Carrito carrito;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contenido_id")
    private Contenido contenido;
    
    private Integer cantidad;
    private Integer precio; // En centavos
    private Integer descuento; // En centavos
    
    // Auditoría
    private LocalDateTime fechaAgregado;
    private LocalDateTime fechaUltimaModificacion;
    
    // Límites por item
    private Integer cantidadMaxima = 10;
    
    // Información de precios histórica
    private Integer precioOriginal; // Para comparar con promociones
    private String motivoDescuento;
    
    // Validaciones
    @PrePersist
    @PreUpdate
    private void validarItem() {
        if (cantidad != null && cantidadMaxima != null && cantidad > cantidadMaxima) {
            throw new IllegalArgumentException("Cantidad excede el máximo permitido");
        }
        if (cantidad != null && cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
    }
    
    // Métodos de cálculo
    public Integer getSubtotal() {
        if (precio == null || cantidad == null) {
            return 0;
        }
        return precio * cantidad;
    }
    
    public Integer getDescuentoTotal() {
        if (descuento == null || cantidad == null) {
            return 0;
        }
        return descuento * cantidad;
    }
    
    public Integer getTotal() {
        return Math.max(0, getSubtotal() - getDescuentoTotal());
    }
}