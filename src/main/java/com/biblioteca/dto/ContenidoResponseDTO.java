package com.biblioteca.dto;

import com.biblioteca.dto.actividades.PrestamoResponseDTO;
import com.biblioteca.enums.FormatoFisico;
import com.biblioteca.enums.TipoContenido;
import com.biblioteca.enums.TipoLicenciaDigital;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ContenidoResponseDTO {
  // Campos de Contenido
  private Long id;
  private ObraResponseDTO obra;
  private EditorialResponseDTO editorial;
  private String portadaUrl;
  private String sinopsis;
  private Integer precio;
  private boolean enVenta;
  private TipoContenido tipo;
  private boolean puedeSerPrestado;
  private LocalDate fechaCreacion;
  private String isbn; // Para libros
  private boolean activo;

  // Campos de ContenidoFisico
  private Integer stockDisponible;
  private Integer minStock;
  private String ubicacionFisica;
  private FormatoFisico formatoFisico;

  // Campos de ContenidoDigital
  private BigDecimal tamanioArchivo;
  private String formatoDigital; // e.g., PDF, EPUB
  private Boolean permiteDescarga;
  private TipoLicenciaDigital tipoLicencia;
  private Integer licencias;

  // Campos de LibroFisico
  private Integer paginas;

  // Campos de PublicacionIlustradaFisica (Manga, Comic)
  private String ilustrador;
  private Integer volumen;

  // Campos de Manga
  private String sentidoLectura;

  // Campos de Comic
  private Boolean colorido;

  // Campos de RevistaPeriodica
  private String periodicidad;
  private String edicion;
  private String issn; // Para publicaciones periódicas

  // Campos de AudioLibro
  private String duracionAudioLibro; // e.g., "PT2H30M" o Long en segundos/milisegundos
  private String narrador;
  private String calidadAudio;

  // Campos de MaterialEducativo
  private String nivelEducativo;
  private String asignatura;
  private String recursosEducativos;

  // Campos de ContenidoMultimedia
  private String duracionMultimedia; // e.g., "PT1H45M" o Long
  private String calidadMultimedia;
  private String requisitosReproduccion;

  // Campos de Series
  private SerieResponseDTO serie;
  private Integer ordenEnSerie;

  // ========== CAMPOS PARA GESTIÓN DE PRÉSTAMOS ==========

  /**
   * Indica si este contenido ya está prestado por el perfil actual
   * (solo se llena cuando hay un perfil activo en sesión)
   */
  private Boolean yaPrestadoPorPerfil;

  /**
   * Información del préstamo activo si existe
   * (solo se llena cuando yaPrestadoPorPerfil = true)
   */
  private PrestamoResponseDTO prestamoActivo;

  // ========== MÉTODOS HELPER PARA PRÉSTAMOS ==========

  /**
   * Verifica de forma segura si está prestado por el perfil actual
   */
  public boolean isYaPrestadoPorPerfilSafe() {
    return Boolean.TRUE.equals(yaPrestadoPorPerfil);
  }

  /**
   * Verifica si tiene un préstamo activo con información válida
   */
  public boolean tienePrestamoActivo() {
    return isYaPrestadoPorPerfilSafe() && prestamoActivo != null;
  }

  /**
   * Obtiene el ID del préstamo activo de forma segura
   */
  public Long getPrestamoActivoId() {
    return tienePrestamoActivo() ? prestamoActivo.getId() : null;
  }

  /**
   * Verifica si el préstamo está próximo a vencer (menos de 3 días)
   */
  public boolean isPrestamoProximoAVencer() {
    if (!tienePrestamoActivo())
      return false;

    LocalDate fechaVencimiento = prestamoActivo.getFechaDevolucionPrevista();
    if (fechaVencimiento == null)
      return false;

    return fechaVencimiento.isBefore(LocalDate.now().plusDays(3));
  }

  /**
   * Verifica si el préstamo está vencido
   */
  public boolean isPrestamoVencido() {
    if (!tienePrestamoActivo())
      return false;

    LocalDate fechaVencimiento = prestamoActivo.getFechaDevolucionPrevista();
    if (fechaVencimiento == null)
      return false;

    return fechaVencimiento.isBefore(LocalDate.now());
  }

  /**
   * Obtiene el estado del préstamo de forma legible
   */
  public String getEstadoPrestamoTexto() {
    if (!tienePrestamoActivo())
      return "Disponible";

    if (isPrestamoVencido())
      return "Vencido";
    if (isPrestamoProximoAVencer())
      return "Próximo a vencer";
    return "Prestado";
  }

  /**
   * Obtiene la clase CSS apropiada para el estado del préstamo
   */
  public String getEstadoPrestamoClase() {
    if (!tienePrestamoActivo())
      return "text-success";

    if (isPrestamoVencido())
      return "text-danger";
    if (isPrestamoProximoAVencer())
      return "text-warning";
    return "text-info";
  }
}