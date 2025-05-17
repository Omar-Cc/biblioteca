package com.biblioteca.dto.comercial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarritoResponseDTO {
    private Long id;
    private Long carritoId;
    private Long contenidoId;
    private Integer cantidad;
    private Integer precio;
    private Integer descuento;
    
    // Información adicional
    private String contenidoTitulo;
    private String contenidoTipoDescripcion;
    private String contenidoImagen;
    
    // Campos calculados
    private Integer subtotal;
    private Integer total;
}