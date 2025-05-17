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
public class PagoResponseDTO {
    private Long id;
    private Integer monto;
    private LocalDateTime fechaPago;
    private String referenciaPago;
    private String estado;
    private Long ordenId;
    private Long metodoPagoId;
    
    // Información adicional útil
    private String metodoPagoNombre;
}