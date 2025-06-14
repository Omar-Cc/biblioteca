package com.biblioteca.dto.actividades;

import java.time.LocalDate;

import com.biblioteca.enums.EstadoPrestamo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestamoRequestDTO {

  @NotNull(message = "El ID del contenido es obligatorio")
  @Positive(message = "El ID del contenido debe ser positivo")
  private Long contenidoId;

  @NotNull(message = "El ID del perfil es obligatorio")
  @Positive(message = "El ID del perfil debe ser positivo")
  private Long perfilId;

  @NotNull(message = "La fecha de préstamo es obligatoria")
  private LocalDate fechaPrestamo;

  @NotNull(message = "La fecha de devolución prevista es obligatoria")
  @Future(message = "La fecha de devolución debe ser futura")
  private LocalDate fechaDevolucionPrevista;

  private LocalDate fechaDevolucionReal;

  private EstadoPrestamo estado;

  @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
  private String observaciones;
}