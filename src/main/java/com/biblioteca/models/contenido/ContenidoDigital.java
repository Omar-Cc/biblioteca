package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

import com.biblioteca.enums.TipoLicenciaDigital;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContenidoDigital extends Contenido {
    private BigDecimal tamanioArchivo; // Por ejemplo en MB o KB
    private String formato; // PDF, EPUB, MP3, etc.
    private boolean permiteDescarga;
    private TipoLicenciaDigital tipoLicencia;
    private int licencias; // Número de licencias disponibles (si aplica)
}