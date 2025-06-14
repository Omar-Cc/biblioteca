package com.biblioteca.dto.comercial;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialPagoSuscripcionResponseDTO {
    private Long id;
    private Integer monto;
    private LocalDateTime fechaPago;
    private String periodo;
    private String estado;
    private Long suscripcionId;
    private Long pagoId;
    private String metodoPago;
}