package com.biblioteca.exceptions;

public class OperacionNoPermitidaException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public OperacionNoPermitidaException(String message) {
    super(message);
  }

  public OperacionNoPermitidaException(String message, Throwable cause) {
    super(message, cause);
  }

}
