package com.biblioteca.models.comercial;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class HistorialPagoSuscripcion {
    private Long id;
    private Integer monto; // En centavos
    private LocalDateTime fechaPago;
    private String periodo; // Ej: "Mayo 2024", "Anual 2024-2025"
    private String estado; // Pagado, Fallido, Pendiente, etc.
    
    // Relaciones
    private Suscripcion suscripcion;
    private Pago pago;
}