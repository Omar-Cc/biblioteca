package com.biblioteca.models.contenido;

import com.biblioteca.enums.TipoRelacionObra;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "relaciones_obras")
@IdClass(RelacionObraId.class)
public class RelacionObra {
  @Id
  @Column(name = "obra_origen_id")
  private Long obraOrigenId;

  @Id
  @Column(name = "obra_relacionada_id")
  private Long obraRelacionadaId;

  @Enumerated(EnumType.STRING)
  private TipoRelacionObra tipoRelacion;

  private Integer orden; // Para secuencias ordenadas

  @ManyToOne
  @JoinColumn(name = "obra_origen_id", insertable = false, updatable = false)
  private Obra obraOrigen;

  @ManyToOne
  @JoinColumn(name = "obra_relacionada_id", insertable = false, updatable = false)
  private Obra obraRelacionada;
}