package com.biblioteca.models.comercial;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class Pago {
    private Long id;
    private Integer monto; // En centavos
    private LocalDateTime fechaPago;
    private String referenciaPago;
    private String estado; // Completado, Pendiente, Rechazado, etc.
    
    // Relaciones
    private Orden orden;
    private MetodoPago metodoPago;
}