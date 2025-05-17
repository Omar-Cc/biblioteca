package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AllArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RevistaPeriodica extends ContenidoFisico {
    private String periodicidad; // "Semanal", "Mensual", "Trimestral"
    private String edicion; // "Enero 2024", "Vol. 3, No. 2"
    private String issn;
}