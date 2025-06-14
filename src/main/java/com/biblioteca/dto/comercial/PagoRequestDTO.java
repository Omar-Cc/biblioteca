package com.biblioteca.dto.comercial;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoRequestDTO {
    @NotNull(message = "El monto es obligatorio")
    @Min(value = 0, message = "El monto debe ser mayor o igual a 0")
    private Integer monto;
    
    private String referenciaPago;
    private String estado;
    private String motivoRechazo; // Solo si el estado es Rechazado
    private String referenciaExterna; // Para integraciones con pasarelas de pago
    
    private Long ordenId; // Opcional - para pagos de órdenes
    
    private Long suscripcionId; // Opcional - para pagos de suscripciones
    
    @NotNull(message = "El ID del método de pago es obligatorio")
    private Long metodoPagoId;
    
    // Campos para simulación (heredados de PagoSimulado)
    private Boolean esSimulado;
    private Boolean simularFallo;
    private String tipoError;
    private String periodo; // Para pagos de suscripciones
}