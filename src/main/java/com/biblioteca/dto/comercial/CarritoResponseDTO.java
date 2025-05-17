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
public class CarritoResponseDTO {
    private Long id;
    private Long perfilId;
    private LocalDateTime fechaCreacion;
    private String perfilNombre;
    
    @Builder.Default
    private Set<ItemCarritoResponseDTO> items = new HashSet<>();
    
    // Campos calculados
    private int cantidadItems;
    private Integer subtotal;
    private Integer totalDescuentos;
    private Integer total;
}