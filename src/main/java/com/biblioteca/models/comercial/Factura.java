package com.biblioteca.models.comercial;

import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class Factura {
    private Long id;
    private String numeroFactura;
    private LocalDate fechaEmision;
    private Integer subtotal; // En centavos
    private Integer impuestos; // En centavos
    private Integer total; // En centavos
    private String datosFacturacion;
    private Orden orden;
}