package com.biblioteca.events;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Evento publicado cuando se agrega un item al carrito
 */
@Data
@Builder
public class ItemAgregadoEvent {
    private Long perfilId;
    private Long contenidoId;
    private String contenidoTipo;
    private Integer cantidad;
    private Integer precio;
    private LocalDateTime timestamp;
}
