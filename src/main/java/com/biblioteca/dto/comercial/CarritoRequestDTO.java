package com.biblioteca.dto.comercial;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoRequestDTO {
    @NotNull(message = "El ID del perfil es obligatorio")
    private Long perfilId;
    
    // ✅ AGREGAR: Información adicional según mejoras sugeridas
    private String sessionId; // Para usuarios anónimos
    private String origen; // WEB, MOBILE, API
    private Integer limiteItemsPersonalizado; // Límite específico para este carrito
    private String notasEspeciales;
    
    @Builder.Default
    private Boolean esCarritoTemporal = false; // Para carritos de prueba o temporales
}