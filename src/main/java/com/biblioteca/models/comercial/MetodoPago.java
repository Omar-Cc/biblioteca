package com.biblioteca.models.comercial;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "metodos_pago")
public class MetodoPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo; // TARJETA_CREDITO, TARJETA_DEBITO, SIMULADO, etc.
    private String nombre; // Para mostrar en la UI
    private String descripcion;

    private boolean requiereAutorizacion;

    @Builder.Default
    private boolean activo = true;
}