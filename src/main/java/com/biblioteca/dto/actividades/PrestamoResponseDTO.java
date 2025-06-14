package com.biblioteca.dto.actividades;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.biblioteca.enums.EstadoPrestamo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestamoResponseDTO {

  private Long id;

  // Información del contenido
  private ContenidoBasicoDTO contenido;

  // Información del perfil
  private PerfilBasicoDTO perfil;

  private LocalDate fechaPrestamo;
  private LocalDate fechaDevolucionPrevista;
  private LocalDate fechaDevolucionReal;
  private EstadoPrestamo estado;
  private String estadoDescripcion;
  private String observaciones;
  private LocalDateTime fechaCreacion;
  private LocalDateTime fechaActualizacion;

  // Campos calculados
  private boolean vencido;
  private boolean devuelto;
  private long diasRetraso;
  private long diasRestantes;
  private boolean proximoAVencer;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ContenidoBasicoDTO {
    private Long id;
    private String titulo;
    private String tipoContenido;
    private String isbn;
    private String portadaUrl;
    private boolean enVenta;
    private boolean puedeSerPrestado;
    private Integer precio;

    private ObraBasicaDTO obra;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ObraBasicaDTO {
      private Long id;
      private String titulo;
      private String autor;
      private Integer anioPublicacion;
      private String version;
    }
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class PerfilBasicoDTO {
    private Long id;
    private String nombreVisible;
    private String fotoPerfil;
    private boolean esInfantil;
    private boolean activo;
    private String idiomaPreferido;
  }
}