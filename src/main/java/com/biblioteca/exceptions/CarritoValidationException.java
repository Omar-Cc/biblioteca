package com.biblioteca.exceptions;

/**
 * Excepción para errores de validación específicos del carrito
 */
public class CarritoValidationException extends RuntimeException {
    
    public CarritoValidationException(String message) {
        super(message);
    }
    
    public CarritoValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
