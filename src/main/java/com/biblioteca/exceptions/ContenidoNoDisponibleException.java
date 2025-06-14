package com.biblioteca.exceptions;

public class ContenidoNoDisponibleException extends RuntimeException {

  public ContenidoNoDisponibleException(String message) {
      super(message);
  }

  public ContenidoNoDisponibleException(String message, Throwable cause) {
      super(message, cause);
  }
}
