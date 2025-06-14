package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AllArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.PrimaryKeyJoinColumn;


@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "libros_fisicos")
@PrimaryKeyJoinColumn(name = "contenido_id")
public class LibroFisico extends ContenidoFisico { // Asumiendo que ContenidoFisico es una entidad o MappedSuperclass
    private int paginas;
}