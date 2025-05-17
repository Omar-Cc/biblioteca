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
@EqualsAndHashCode(exclude = { "items" })
@ToString(exclude = { "items" })
public class Carrito {
    private Long id;
    private Perfil perfil;
    private LocalDateTime fechaCreacion;
    
    @Builder.Default
    private Set<ItemCarrito> items = new HashSet<>();
    
    public void addItem(ItemCarrito item) {
        this.items.add(item);
        item.setCarrito(this);
    }
    
    public void removeItem(ItemCarrito item) {
        this.items.remove(item);
        item.setCarrito(null);
    }
}