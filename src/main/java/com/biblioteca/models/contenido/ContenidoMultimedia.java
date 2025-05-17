package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AllArgsConstructor;
import java.time.Duration;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContenidoMultimedia extends ContenidoDigital {
    private Duration duracion;
    private String calidad; // Ejemplo: "1080p", "720p"
    private String requisitosReproduccion;
}