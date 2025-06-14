package com.biblioteca.dto.comercial;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPagoRequestDTO {
    private String tipo;
    @NotBlank(message = "El nombre del método de pago es obligatorio")
    private String nombre;
    private String descripcion;
    private boolean requiereAutorizacion;
    private boolean activo;
}