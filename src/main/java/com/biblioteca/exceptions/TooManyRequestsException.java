package com.biblioteca.exceptions;

/**
 * Excepción para rate limiting - demasiadas solicitudes
 */
public class TooManyRequestsException extends RuntimeException {
    
    public TooManyRequestsException(String message) {
        super(message);
    }
    
    public TooManyRequestsException(String message, Throwable cause) {
        super(message, cause);
    }
}
