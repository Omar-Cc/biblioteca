package com.biblioteca.models.contenido;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "metadatos_obras")
@IdClass(MetadatoObraId.class)
public class MetadatoObra {
  @Id
  @Column(name = "obra_id")
  private Long obraId;

  @Id
  private String clave; // "tag", "palabra_clave", "clasificacion_edad", "tema"

  private String valor; // "fantasia", "aventura", "+13", "magia"

  private String categoria; // "genero_especifico", "edad", "tema_principal"

  @ManyToOne
  @JoinColumn(name = "obra_id", insertable = false, updatable = false)
  private Obra obra;
}