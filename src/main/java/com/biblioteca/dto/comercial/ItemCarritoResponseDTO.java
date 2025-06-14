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
public class ItemCarritoResponseDTO {
    private Long id;
    private Long carritoId;
    private Long contenidoId;
    private Integer cantidad;
    private Integer precio;
    private Integer descuento;
    
    // ✅ AGREGAR: Información de auditoría
    private LocalDateTime fechaAgregado;
    private LocalDateTime fechaUltimaModificacion;
    
    // ✅ AGREGAR: Límites y validaciones
    private Integer cantidadMaxima;
    
    // ✅ AGREGAR: Información de precios
    private Integer precioOriginal;
    private String motivoDescuento;
    
    // ✅ AGREGAR: Información adicional del contenido
    private String contenidoTitulo;
    private String contenidoTipoDescripcion;
    private String contenidoImagen;
    private String contenidoAutor;
    private String contenidoEditorial;
    
    // ✅ AGREGAR: Estado y disponibilidad
    private boolean disponible;
    private Integer stockDisponible;
    private String estadoItem; // DISPONIBLE, AGOTADO, RESERVADO
    
    // ✅ AGREGAR: Información de regalo
    private Boolean esRegalo;
    private String notasEspeciales;
    
    // Campos calculados
    private Integer subtotal;
    private Integer total;
    private Integer descuentoTotal;
}