package com.biblioteca.exceptions;

/**
 * Excepción para cuando un perfil no es válido o no está activo
 */
public class PerfilNoValidoException extends RuntimeException {
    
    public PerfilNoValidoException(String message) {
        super(message);
    }
    
    public PerfilNoValidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
