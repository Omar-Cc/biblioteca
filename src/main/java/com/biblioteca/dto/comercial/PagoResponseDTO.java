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
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaProcesamiento;
    private String referenciaPago;
    private String estado;
    private Long ordenId; // Opcional
    private Long suscripcionId; // Opcional
    private Long metodoPagoId;
    
    // Información adicional útil
    private String metodoPagoNombre;
    private String transaccionId;
    private String codigoRespuesta;
    private String mensaje;
    private String periodo; // Para suscripciones
    private Boolean esSimulado;
}