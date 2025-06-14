package com.biblioteca.dto.comercial;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoResponseDTO {
    private Long id;
    private Long perfilId;
    private LocalDateTime fechaCreacion;
    private String perfilNombre;
    
    @Builder.Default
    private Set<ItemCarritoResponseDTO> items = new HashSet<>();
    
    // ✅ AGREGAR: Información adicional útil según mejoras sugeridas
    private String estado;
    private LocalDateTime fechaUltimaModificacion;
    private boolean tieneItemsNoDisponibles;
    private boolean requiresActualizacionPrecios;
    
    // ✅ AGREGAR: Información de límites
    private Integer limiteItems;
    private Integer itemsRestantes;
    
    // ✅ AGREGAR: Información de ahorro
    private Integer ahorroTotal;
    private List<String> promocionesAplicadas;
    
    // ✅ AGREGAR: Estimaciones de envío
    private Integer costoEnvioEstimado;
    private LocalDate fechaEntregaEstimada;
    
    // ✅ AGREGAR: Información de sesión
    private String sessionId;
    private LocalDateTime fechaExpiracion;
    
    // Campos calculados originales
    private int cantidadItems;
    private Integer subtotal;
    private Integer totalDescuentos;
    private Integer total;
}