package com.biblioteca.events;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Evento publicado cuando se vacía el carrito
 */
@Data
@Builder
public class CarritoVaciadoEvent {
    private Long perfilId;
    private Integer itemsEliminados;
    private Integer valorTotal;
    private LocalDateTime timestamp;
}
