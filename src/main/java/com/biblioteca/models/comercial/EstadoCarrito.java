package com.biblioteca.models.comercial;

/**
 * Estados posibles de un carrito de compras
 */
public enum EstadoCarrito {
    /**
     * Carrito activo, en uso por el usuario
     */
    ACTIVO,
    
    /**
     * Carrito abandonado por inactividad
     */
    ABANDONADO,
    
    /**
     * Carrito convertido a una orden de compra
     */
    CONVERTIDO_A_ORDEN,
    
    /**
     * Carrito expirado automáticamente
     */
    EXPIRADO
}
