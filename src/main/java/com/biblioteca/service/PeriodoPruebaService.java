package com.biblioteca.service;

public interface PeriodoPruebaService {
    boolean puedeUsarPeriodoPrueba(Long usuarioId, Long planId);
    void marcarPeriodoPruebaUsado(Long usuarioId);
    boolean haUsadoPeriodoPrueba(Long usuarioId);
}