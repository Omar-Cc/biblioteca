package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

import com.biblioteca.enums.TipoLicenciaDigital;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "contenidos_digitales")
@PrimaryKeyJoinColumn(name = "contenido_id")
public class ContenidoDigital extends Contenido {
    private BigDecimal tamanioArchivo; // Por ejemplo en MB o KB
    private String formato; // PDF, EPUB, MP3, etc.
    private boolean permiteDescarga;
    
    @Enumerated(EnumType.STRING)
    private TipoLicenciaDigital tipoLicencia;
    
    private int licencias; // Número de licencias disponibles (si aplica)
}