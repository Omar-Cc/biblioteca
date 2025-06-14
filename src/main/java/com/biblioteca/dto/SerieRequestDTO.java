package com.biblioteca.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SerieRequestDTO {
    @NotBlank(message = "El nombre de la serie es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El número de volúmenes es obligatorio")
    @Min(value = 1, message = "El número de volúmenes debe ser mayor a 0")
    private Integer numeroVolumenes;

    private boolean completa;
}