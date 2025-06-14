package com.biblioteca.events;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Evento publicado cuando se aplica un cupón de descuento
 */
@Data
@Builder
public class CuponAplicadoEvent {
    private Long perfilId;
    private String codigoCupon;
    private Integer descuentoAplicado;
    private LocalDateTime timestamp;
}
