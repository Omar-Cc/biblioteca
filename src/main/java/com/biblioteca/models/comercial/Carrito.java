package com.biblioteca.models.comercial;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.biblioteca.models.acceso.Perfil;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(exclude = { "items" })
@ToString(exclude = { "items" })
@Entity
@Table(name = "carritos")
public class Carrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id")
    private Perfil perfil;
    
    private LocalDateTime fechaCreacion;
    
    // ✅ AGREGAR: Auditoría completa
    private LocalDateTime fechaUltimaModificacion;
    private String usuarioModificacion;
    
    // ✅ AGREGAR: Estado del carrito
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoCarrito estado = EstadoCarrito.ACTIVO;
    
    // ✅ AGREGAR: Información de sesión
    private String sessionId; // Para usuarios anónimos
    
    // ✅ AGREGAR: Límites y configuración
    @Builder.Default
    private Integer limiteItems = 50;
    private LocalDateTime fechaExpiracion;
    
    // ✅ AGREGAR: Gestión de concurrencia
    @Version
    private Long version;
    
    // ✅ AGREGAR: Relación con items
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<ItemCarrito> items = new HashSet<>();
    
    // ✅ MEJORAR: Método más seguro para agregar items
    public void addItem(ItemCarrito item) {
        if (this.items.size() >= this.limiteItems) {
            throw new IllegalStateException("Carrito ha alcanzado el límite máximo de items");
        }
        
        // Verificar si el item ya existe
        java.util.Optional<ItemCarrito> existente = this.items.stream()
            .filter(i -> i.getContenido().getId().equals(item.getContenido().getId()))
            .findFirst();
            
        if (existente.isPresent()) {
            existente.get().setCantidad(existente.get().getCantidad() + item.getCantidad());
            existente.get().setFechaUltimaModificacion(LocalDateTime.now());
        } else {
            this.items.add(item);
            item.setCarrito(this);
            item.setFechaAgregado(LocalDateTime.now());
        }
        
        this.fechaUltimaModificacion = LocalDateTime.now();
    }
    
    public void removeItem(ItemCarrito item) {
        this.items.remove(item);
        item.setCarrito(null);
        this.fechaUltimaModificacion = LocalDateTime.now();
    }
    
    // ✅ AGREGAR: Métodos de cálculo usando los items
    public int getCantidadTotalItems() {
        return items.stream()
            .mapToInt(ItemCarrito::getCantidad)
            .sum();
    }
    
    public Integer getSubtotalCarrito() {
        return items.stream()
            .mapToInt(ItemCarrito::getSubtotal)
            .sum();
    }
    
    public Integer getTotalDescuentosCarrito() {
        return items.stream()
            .mapToInt(ItemCarrito::getDescuentoTotal)
            .sum();
    }
    
    public Integer getTotalCarrito() {
        return Math.max(0, getSubtotalCarrito() - getTotalDescuentosCarrito());
    }
    
    // ✅ AGREGAR: Métodos de auditoría automática
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (fechaCreacion == null) {
            fechaCreacion = now;
        }
        fechaUltimaModificacion = now;
        
        if (fechaExpiracion == null) {
            fechaExpiracion = now.plusDays(30); // 30 días por defecto
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaUltimaModificacion = LocalDateTime.now();
    }
}