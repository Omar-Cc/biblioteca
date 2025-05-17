package com.biblioteca.dto.comercial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPagoResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private boolean requiereAutorizacion;
    private boolean activo;
}