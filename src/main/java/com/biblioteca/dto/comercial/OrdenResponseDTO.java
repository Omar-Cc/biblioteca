package com.biblioteca.dto.comercial;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenResponseDTO {
    private Long id;
    private Long perfilId;
    private Long carritoId;
    private LocalDateTime fechaCreacion;
    private String estadoOrden;
    private Integer totalOrden;
    private String perfilNombre;
    
    @Builder.Default
    private Set<ItemOrdenResponseDTO> items = new HashSet<>();
    
    @Builder.Default
    private Set<PagoResponseDTO> pagos = new HashSet<>();
    
    private FacturaResponseDTO factura;
    
    // Campos calculados
    private int cantidadItems;
    private Integer subtotal;
    private Integer totalDescuentos;
    private Integer totalPagado;
    private Integer saldoPendiente;
}