package com.biblioteca.events;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Evento publicado cuando se elimina un item del carrito
 */
@Data
@Builder
public class ItemEliminadoEvent {
    private Long perfilId;
    private Long contenidoId;
    private String contenidoTipo;
    private Integer cantidad;
    private LocalDateTime timestamp;
}
