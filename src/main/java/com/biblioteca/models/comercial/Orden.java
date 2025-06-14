package com.biblioteca.models.comercial;

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
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Data
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "ordenes")
public class Orden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id")
    private Perfil perfil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id")
    private Carrito carrito;

    private LocalDateTime fechaCreacion;
    private String estadoOrden; // Pendiente, Pagada, Cancelada, etc.
    private Integer totalOrden; // En centavos

    @OneToOne(mappedBy = "orden", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Factura factura;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<ItemOrden> items = new HashSet<>();

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Pago> pagos = new HashSet<>();

    private String motivoCancelacion;
    private LocalDateTime fechaCompletada;
    
    public void addItem(ItemOrden item) {
        this.items.add(item);
        item.setOrden(this);
    }

    public void removeItem(ItemOrden item) {
        this.items.remove(item);
        item.setOrden(null);
    }

    public void addPago(Pago pago) {
        this.pagos.add(pago);
        pago.setOrden(this);
    }

    public void removePago(Pago pago) {
        this.pagos.remove(pago);
        pago.setOrden(null);
    }
}