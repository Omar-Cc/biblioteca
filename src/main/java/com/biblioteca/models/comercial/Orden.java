package com.biblioteca.models.comercial;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.biblioteca.models.Perfil;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(exclude = { "items", "pagos" })
@ToString(exclude = { "items", "pagos" })
public class Orden {
    private Long id;
    private Perfil perfil;
    private Carrito carrito;
    private LocalDateTime fechaCreacion;
    private String estadoOrden; // Pendiente, Pagada, Cancelada, etc.
    private Integer totalOrden; // En centavos
    private Factura factura;
    
    @Builder.Default
    private Set<ItemOrden> items = new HashSet<>();
    
    @Builder.Default
    private Set<Pago> pagos = new HashSet<>();
    
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