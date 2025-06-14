package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AllArgsConstructor;
import java.time.Duration;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "contenidos_multimedia")
@PrimaryKeyJoinColumn(name = "contenido_digital_id")
public class ContenidoMultimedia extends ContenidoDigital {
    private Duration duracion;
    private String calidad; // Ejemplo: "1080p", "720p"
    private String requisitosReproduccion;
}