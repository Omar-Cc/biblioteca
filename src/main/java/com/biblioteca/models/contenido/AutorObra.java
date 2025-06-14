package com.biblioteca.models.contenido;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "autores_obras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = { "obra", "autor" }) // Excluir para evitar lazy loading
@IdClass(AutorObraId.class)
public class AutorObra {
    @Id
    @Column(name = "obra_id")
    private Long obraId;

    @Id
    @Column(name = "autor_id")
    private Long autorId;

    @ManyToOne
    @JoinColumn(name = "obra_id", insertable = false, updatable = false)
    private Obra obra;

    @ManyToOne
    @JoinColumn(name = "autor_id", insertable = false, updatable = false)
    private Autor autor;

    @Column(nullable = false)
    private String rol; // "principal", "colaborador", "editor"

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AutorObra autorObra = (AutorObra) o;
        return Objects.equals(obraId, autorObra.obraId) &&
                Objects.equals(autorId, autorObra.autorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(obraId, autorId);
    }
}