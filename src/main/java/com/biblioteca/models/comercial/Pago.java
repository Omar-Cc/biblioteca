package com.biblioteca.models.comercial;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.biblioteca.enums.EstadoPago;

@Data
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "pagos")
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer monto; // En centavos
    private LocalDateTime fechaPago;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaProcesamiento;
    
    private String referenciaPago;
    private String referenciaExterna;
    private String transaccionId;
    private String codigoRespuesta;
    private String mensaje;
    
    @Enumerated(EnumType.STRING)
    private EstadoPago estado; // EXITOSO, FALLIDO, PENDIENTE, CANCELADO
    
    private String motivoRechazo;
    private String moneda;
    
    // Campos para simulación (opcional)
    private Boolean esSimulado;
    private Boolean simularFallo;
    private String tipoError;
    
    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id")
    private Orden orden;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suscripcion_id")
    private Suscripcion suscripcion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metodo_pago_id")
    private MetodoPago metodoPago;
}