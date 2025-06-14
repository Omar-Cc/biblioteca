package com.biblioteca.models.comercial;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Data
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "historial_pagos_suscripcion")
public class HistorialPagoSuscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer monto; // En centavos
    private LocalDateTime fechaPago;
    private String periodo; // Ej: "Mayo 2024", "Anual 2024-2025"
    private String estado; // Pagado, Fallido, Pendiente, etc.
    
    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suscripcion_id")
    private Suscripcion suscripcion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id")
    private Pago pago;
}