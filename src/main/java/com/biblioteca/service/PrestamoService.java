package com.biblioteca.service;

import com.biblioteca.dto.actividades.PrestamoRequestDTO;
import com.biblioteca.dto.actividades.PrestamoResponseDTO;
import com.biblioteca.enums.EstadoPrestamo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PrestamoService {

  // ========== OPERACIONES CRUD ==========

  /**
   * Crear un nuevo préstamo
   */
  PrestamoResponseDTO crearPrestamo(PrestamoRequestDTO prestamoRequest);

  /**
   * Obtener préstamo por ID
   */
  PrestamoResponseDTO obtenerPrestamoPorId(Long id);

  /**
   * Actualizar un préstamo existente
   */
  PrestamoResponseDTO actualizarPrestamo(Long id, PrestamoRequestDTO prestamoRequest);

  /**
   * Eliminar un préstamo (soft delete)
   */
  void eliminarPrestamo(Long id);

  /**
   * Obtener todos los préstamos con paginación
   */
  Page<PrestamoResponseDTO> obtenerTodosPrestamos(Pageable pageable);

  // ========== OPERACIONES POR ESTADO ==========

  /**
   * Obtener préstamos por estado
   */
  List<PrestamoResponseDTO> obtenerPrestamosPorEstado(EstadoPrestamo estado);

  /**
   * Obtener préstamos por estado con paginación
   */
  Page<PrestamoResponseDTO> obtenerPrestamosPorEstado(EstadoPrestamo estado, Pageable pageable);

  /**
   * Obtener préstamos por múltiples estados
   */
  List<PrestamoResponseDTO> obtenerPrestamosPorEstados(List<EstadoPrestamo> estados);

  // ========== OPERACIONES POR PERFIL ==========

  /**
   * Obtener préstamos de un perfil
   */
  List<PrestamoResponseDTO> obtenerPrestamosPorPerfil(Long perfilId);

  /**
   * Obtener préstamos de un perfil con paginación
   */
  Page<PrestamoResponseDTO> obtenerPrestamosPorPerfil(Long perfilId, Pageable pageable);

  /**
   * Obtener préstamos activos de un perfil
   */
  List<PrestamoResponseDTO> obtenerPrestamosActivosPorPerfil(Long perfilId);

  /**
   * Obtener préstamos de un perfil por estado
   */
  Page<PrestamoResponseDTO> obtenerPrestamosPorPerfilYEstado(Long perfilId, EstadoPrestamo estadoEnum,
      Pageable pageable);

  /**
   * Obtener historial completo de préstamos de un perfil
   */
  Page<PrestamoResponseDTO> obtenerHistorialPrestamosPerfil(Long perfilId, Pageable pageable);

  /**
   * Contar préstamos activos de un perfil
   */
  Long contarPrestamosActivosPorPerfil(Long perfilId);

  // ========== OPERACIONES POR CONTENIDO ==========

  /**
   * Obtener préstamos de un contenido
   */
  List<PrestamoResponseDTO> obtenerPrestamosPorContenido(Long contenidoId);

  /**
   * Verificar si un contenido está prestado actualmente
   */
  boolean verificarContenidoPrestado(Long contenidoId);

  /**
   * Obtener último préstamo de un contenido
   */
  PrestamoResponseDTO obtenerUltimoPrestamoContenido(Long contenidoId);

  // ========== OPERACIONES DE GESTIÓN ==========

  /**
   * Devolver un préstamo
   */
  PrestamoResponseDTO devolverPrestamo(Long prestamoId);

  /**
   * Renovar un préstamo
   */
  PrestamoResponseDTO renovarPrestamo(Long prestamoId, Integer diasExtension);

  /**
   * Marcar préstamo como perdido
   */
  PrestamoResponseDTO marcarComoPerdido(Long prestamoId, String observaciones);

  /**
   * Cancelar un préstamo
   */
  PrestamoResponseDTO cancelarPrestamo(Long prestamoId, String motivo);

  /**
   * Cambiar estado de un préstamo
   */
  PrestamoResponseDTO cambiarEstadoPrestamo(Long prestamoId, EstadoPrestamo nuevoEstado, String observaciones);

  // ========== OPERACIONES DE VENCIMIENTO ==========

  /**
   * Obtener préstamos vencidos
   */
  List<PrestamoResponseDTO> obtenerPrestamosVencidos();

  /**
   * Obtener préstamos próximos a vencer
   */
  List<PrestamoResponseDTO> obtenerPrestamosProximosAVencer(Integer diasAnticipacion);

  /**
   * Procesar vencimientos automáticos
   */
  void procesarVencimientosAutomaticos();

  /**
   * Obtener préstamos para notificación de vencimiento
   */
  List<PrestamoResponseDTO> obtenerPrestamosParaNotificacionVencimiento(LocalDate fechaVencimiento);

  // ========== VALIDACIONES DE NEGOCIO ==========

  /**
   * Verificar si un perfil puede realizar más préstamos
   */
  boolean puedeRealizarPrestamo(Long perfilId);

  /**
   * Obtiene el préstamo activo entre un perfil y contenido específico
   */
  Optional<PrestamoResponseDTO> obtenerPrestamoActivoPorPerfilYContenido(Long perfilId, Long contenidoId);

  /**
   * Verificar si un perfil ya tiene prestado un contenido específico
   */
  boolean perfilTieneContenidoPrestado(Long perfilId, Long contenidoId);

  /**
   * Obtener límite de préstamos para un perfil
   */
  Integer obtenerLimitePrestamos(Long perfilId);

  /**
   * Verificar disponibilidad de contenido para préstamo
   */
  boolean verificarDisponibilidadContenido(Long contenidoId);

  // ========== OPERACIONES POR FECHAS ==========

  /**
   * Obtener préstamos por fecha específica
   */
  List<PrestamoResponseDTO> obtenerPrestamosPorFecha(LocalDate fecha);

  /**
   * Obtener préstamos en un rango de fechas
   */
  List<PrestamoResponseDTO> obtenerPrestamosEnRango(LocalDate fechaInicio, LocalDate fechaFin);

  /**
   * Obtener préstamos con devolución prevista en una fecha
   */
  List<PrestamoResponseDTO> obtenerPrestamosConDevolucionEn(LocalDate fecha);

  // ========== OPERACIONES DE BÚSQUEDA AVANZADA ==========

  /**
   * Buscar préstamos con filtros múltiples
   */
  Page<PrestamoResponseDTO> buscarPrestamosConFiltros(
      Long perfilId,
      Long contenidoId,
      EstadoPrestamo estado,
      LocalDate fechaInicio,
      LocalDate fechaFin,
      Boolean soloVencidos,
      Pageable pageable);

  /**
   * Buscar préstamos por título de obra
   */
  List<PrestamoResponseDTO> buscarPrestamosPorTituloObra(String titulo);

  /**
   * Buscar préstamos por nombre de usuario
   */
  List<PrestamoResponseDTO> buscarPrestamosPorNombreUsuario(String nombreUsuario);

  // ========== OPERACIONES DE NOTIFICACIÓN ==========

  /**
   * Obtener préstamos que requieren notificación de recordatorio
   */
  List<PrestamoResponseDTO> obtenerPrestamosParaRecordatorio();

  /**
   * Marcar notificación como enviada
   */
  void marcarNotificacionEnviada(Long prestamoId, String tipoNotificacion);

  // ========== OPERACIONES DE MANTENIMIENTO ==========

  /**
   * Obtener préstamos para archivado
   */
  List<PrestamoResponseDTO> obtenerPrestamosParaArchivado(LocalDate fechaLimite);

  /**
   * Limpiar préstamos antiguos
   */
  void limpiarPrestamosAntiguos(LocalDate fechaLimite);

  /**
   * Sincronizar estados de préstamos
   */
  void sincronizarEstadosPrestamos();

  // ========== OPERACIONES DE ESTADÍSTICAS BÁSICAS ==========

  /**
   * Obtener total de préstamos activos en el sistema
   */
  Long obtenerTotalPrestamosActivos();

  /**
   * Obtener total de préstamos vencidos
   */
  Long obtenerTotalPrestamosVencidos();

  /**
   * Obtener total de préstamos del día
   */
  Long obtenerTotalPrestamosDelDia();

  /**
   * Obtener estadísticas básicas de préstamos
   */
  Object obtenerEstadisticasBasicas();

  // ========== OPERACIONES BATCH ==========

  /**
   * Procesar renovaciones automáticas
   */
  void procesarRenovacionesAutomaticas();

  /**
   * Procesar devoluciones masivas
   */
  List<PrestamoResponseDTO> procesarDevolucionesMasivas(List<Long> prestamosIds);

  /**
   * Actualizar estados masivamente
   */
  void actualizarEstadosMasivamente(List<Long> prestamosIds, EstadoPrestamo nuevoEstado);
}