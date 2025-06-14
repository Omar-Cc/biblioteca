package com.biblioteca.repositories.actividades;

import com.biblioteca.enums.EstadoPrestamo;
import com.biblioteca.models.actividades.Prestamo;
import com.biblioteca.models.acceso.Perfil;
import com.biblioteca.models.contenido.Contenido;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

  // ========== CONSULTAS BÁSICAS POR ESTADO ==========

  List<Prestamo> findByEstado(EstadoPrestamo estado);

  Page<Prestamo> findByEstado(EstadoPrestamo estado, Pageable pageable);

  List<Prestamo> findByEstadoIn(List<EstadoPrestamo> estados);

  // ========== CONSULTAS POR PERFIL ==========

  List<Prestamo> findByPerfil(Perfil perfil);

  List<Prestamo> findByPerfilId(Long perfilId);

  Page<Prestamo> findByPerfilId(Long perfilId, Pageable pageable);

  List<Prestamo> findByPerfilAndEstado(Perfil perfil, EstadoPrestamo estado);

  List<Prestamo> findByPerfilIdAndEstado(Long perfilId, EstadoPrestamo estado);

  Page<Prestamo> findByPerfilIdAndEstado(Long perfilId, EstadoPrestamo estado, Pageable pageable);

  // ========== CONSULTAS POR CONTENIDO ==========

  List<Prestamo> findByContenido(Contenido contenido);

  List<Prestamo> findByContenidoId(Long contenidoId);

  List<Prestamo> findByContenidoAndEstado(Contenido contenido, EstadoPrestamo estado);

  List<Prestamo> findByContenidoIdAndEstado(Long contenidoId, EstadoPrestamo estado);

  // ========== CONSULTAS POR FECHAS ==========

  List<Prestamo> findByFechaPrestamo(LocalDate fechaPrestamo);

  List<Prestamo> findByFechaPrestamoAfter(LocalDate fecha);

  List<Prestamo> findByFechaPrestamoBetween(LocalDate fechaInicio, LocalDate fechaFin);

  List<Prestamo> findByFechaDevolucionPrevista(LocalDate fechaDevolucion);

  List<Prestamo> findByFechaDevolucionPrevistaBefore(LocalDate fecha);

  List<Prestamo> findByFechaDevolucionReal(LocalDate fechaDevolucion);

  // ========== CONSULTAS OPERACIONALES ==========

  /**
   * Encuentra préstamos activos vencidos
   */
  @Query("SELECT p FROM Prestamo p WHERE p.estado = :estado AND p.fechaDevolucionPrevista < :fechaActual")
  List<Prestamo> findPrestamosVencidos(@Param("estado") EstadoPrestamo estado,
      @Param("fechaActual") LocalDate fechaActual);

  /**
   * Encuentra préstamos próximos a vencer en X días
   */
  @Query("SELECT p FROM Prestamo p WHERE p.estado = :estado AND " +
      "p.fechaDevolucionPrevista BETWEEN :fechaActual AND :fechaLimite")
  List<Prestamo> findPrestamosProximosAVencer(
      @Param("estado") EstadoPrestamo estado,
      @Param("fechaActual") LocalDate fechaActual,
      @Param("fechaLimite") LocalDate fechaLimite);

  /**
   * Encuentra préstamos activos de un perfil
   */
  @Query("SELECT p FROM Prestamo p WHERE p.perfil.id = :perfilId AND p.estado = 'ACTIVO'")
  List<Prestamo> findPrestamosActivosByPerfil(@Param("perfilId") Long perfilId);

  /**
   * Cuenta préstamos activos de un perfil
   */
  @Query("SELECT COUNT(p) FROM Prestamo p WHERE p.perfil.id = :perfilId AND p.estado = 'ACTIVO'")
  Long countPrestamosActivosByPerfil(@Param("perfilId") Long perfilId);

  /**
   * Verifica si un contenido está prestado actualmente
   */
  @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Prestamo p " +
      "WHERE p.contenido.id = :contenidoId AND p.estado = 'ACTIVO'")
  boolean isContenidoPrestado(@Param("contenidoId") Long contenidoId);

  /**
   * Encuentra el último préstamo de un contenido específico
   */
  @Query("SELECT p FROM Prestamo p WHERE p.contenido.id = :contenidoId " +
      "ORDER BY p.fechaPrestamo DESC")
  List<Prestamo> findUltimosPrestamosByContenido(@Param("contenidoId") Long contenidoId, Pageable pageable);

  /**
   * Historial de préstamos de un perfil con información de obra
   */
  @Query("SELECT p FROM Prestamo p " +
      "LEFT JOIN FETCH p.contenido c " +
      "LEFT JOIN FETCH c.obra o " +
      "WHERE p.perfil.id = :perfilId " +
      "ORDER BY p.fechaPrestamo DESC")
  Page<Prestamo> findHistorialPrestamosByPerfil(@Param("perfilId") Long perfilId, Pageable pageable);

  // ========== CONSULTAS DE VALIDACIÓN ==========

  /**
   * Verifica si un perfil ya tiene un préstamo activo del mismo contenido
   */
  @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Prestamo p " +
      "WHERE p.perfil.id = :perfilId AND p.contenido.id = :contenidoId AND p.estado = 'ACTIVO'")
  boolean existePrestamoActivoPerfilContenido(@Param("perfilId") Long perfilId, @Param("contenidoId") Long contenidoId);

  /**
   * Encuentra préstamo activo específico entre perfil y contenido
   */
  Optional<Prestamo> findByPerfilIdAndContenidoIdAndEstado(Long perfilId, Long contenidoId, EstadoPrestamo estado);

  // ========== CONSULTAS PARA NOTIFICACIONES ==========

  /**
   * Préstamos que vencen en X días para notificaciones
   */
  @Query("SELECT p FROM Prestamo p " +
      "LEFT JOIN FETCH p.perfil pf " +
      "LEFT JOIN FETCH p.contenido c " +
      "LEFT JOIN FETCH c.obra o " +
      "WHERE p.estado = 'ACTIVO' AND p.fechaDevolucionPrevista = :fechaVencimiento")
  List<Prestamo> findPrestamosParaNotificacionVencimiento(@Param("fechaVencimiento") LocalDate fechaVencimiento);

  /**
   * Préstamos vencidos para recordatorios
   */
  @Query("SELECT p FROM Prestamo p " +
      "LEFT JOIN FETCH p.perfil pf " +
      "LEFT JOIN FETCH p.contenido c " +
      "LEFT JOIN FETCH c.obra o " +
      "WHERE p.estado = 'ACTIVO' AND p.fechaDevolucionPrevista < :fechaActual " +
      "ORDER BY p.fechaDevolucionPrevista ASC")
  List<Prestamo> findPrestamosVencidosParaRecordatorio(@Param("fechaActual") LocalDate fechaActual);

  /**
   * Préstamos antiguos para archivado
   */
  @Query("SELECT p FROM Prestamo p WHERE p.fechaDevolucionReal IS NOT NULL AND " +
      "p.fechaDevolucionReal < :fechaLimite")
  List<Prestamo> findPrestamosParaArchivado(@Param("fechaLimite") LocalDate fechaLimite);

  /**
   * Última actividad en préstamos
   */
  @Query("SELECT MAX(p.fechaActualizacion) FROM Prestamo p")
  Optional<LocalDateTime> findUltimaActividad();

  // ! ========== CONSULTAS PARA REPORTES, DASHBOARD Y CRM ==========
  // ! Las siguientes consultas están destinadas a ser migradas a procedimientos
  // almacenados
  // ! para optimizar el rendimiento en operaciones de análisis de datos y
  // reportería

  // ! --- ESTADÍSTICAS Y MÉTRICAS ---

  /**
   * Estadísticas de préstamos por estado en un período
   */
  @Query("SELECT p.estado, COUNT(p) FROM Prestamo p " +
      "WHERE p.fechaPrestamo BETWEEN :fechaInicio AND :fechaFin " +
      "GROUP BY p.estado")
  List<Object[]> getEstadisticasPrestamosPorEstado(
      @Param("fechaInicio") LocalDate fechaInicio,
      @Param("fechaFin") LocalDate fechaFin);

  /**
   * Préstamos con información completa para reportes
   */
  @Query("SELECT p FROM Prestamo p " +
      "LEFT JOIN FETCH p.contenido c " +
      "LEFT JOIN FETCH c.obra o " +
      "LEFT JOIN FETCH p.perfil pf " +
      "LEFT JOIN FETCH pf.usuario u " +
      "WHERE p.fechaPrestamo BETWEEN :fechaInicio AND :fechaFin")
  List<Prestamo> findPrestamosConDetallesEnPeriodo(
      @Param("fechaInicio") LocalDate fechaInicio,
      @Param("fechaFin") LocalDate fechaFin);

  // ! --- RANKING Y TOP CONTENIDOS ---

  /**
   * Contenidos más prestados
   */
  @Query("SELECT c.id, c.obra.titulo, COUNT(p) as totalPrestamos FROM Prestamo p " +
      "JOIN p.contenido c " +
      "GROUP BY c.id, c.obra.titulo " +
      "ORDER BY COUNT(p) DESC")
  List<Object[]> findContenidosMasPrestados(Pageable pageable);

  /**
   * Perfiles con más préstamos
   */
  @Query("SELECT p.perfil.id, p.perfil.nombreVisible, COUNT(p) as totalPrestamos FROM Prestamo p " +
      "GROUP BY p.perfil.id, p.perfil.nombreVisible " +
      "ORDER BY COUNT(p) DESC")
  List<Object[]> findPerfilesConMasPrestamos(Pageable pageable);

  // ! --- ANÁLISIS TEMPORAL ---

  /**
   * Préstamos por mes para gráficos de tendencias
   */
  @Query("SELECT YEAR(p.fechaPrestamo), MONTH(p.fechaPrestamo), COUNT(p) " +
      "FROM Prestamo p " +
      "WHERE p.fechaPrestamo BETWEEN :fechaInicio AND :fechaFin " +
      "GROUP BY YEAR(p.fechaPrestamo), MONTH(p.fechaPrestamo) " +
      "ORDER BY YEAR(p.fechaPrestamo), MONTH(p.fechaPrestamo)")
  List<Object[]> getPrestamosPorMes(
      @Param("fechaInicio") LocalDate fechaInicio,
      @Param("fechaFin") LocalDate fechaFin);

  /**
   * Tiempo promedio de préstamo por tipo de contenido
   */
  @Query("SELECT TYPE(c), AVG(DATEDIFF(p.fechaDevolucionReal, p.fechaPrestamo)) " +
      "FROM Prestamo p JOIN p.contenido c " +
      "WHERE p.fechaDevolucionReal IS NOT NULL " +
      "GROUP BY TYPE(c)")
  List<Object[]> getTiempoPromedioPrestamosPorTipo();

  // ! --- ANÁLISIS DE USUARIOS ---

  /**
   * Distribución de préstamos por edad de perfil
   */
  @Query("SELECT " +
      "CASE WHEN pf.esInfantil = true THEN 'INFANTIL' ELSE 'ADULTO' END, " +
      "COUNT(p) " +
      "FROM Prestamo p JOIN p.perfil pf " +
      "WHERE p.fechaPrestamo BETWEEN :fechaInicio AND :fechaFin " +
      "GROUP BY pf.esInfantil")
  List<Object[]> getDistribucionPrestamosPorEdad(
      @Param("fechaInicio") LocalDate fechaInicio,
      @Param("fechaFin") LocalDate fechaFin);

  /**
   * Usuarios más activos en los últimos N días
   */
  @Query("SELECT u.username, pf.nombreVisible, COUNT(p) as totalPrestamos " +
      "FROM Prestamo p " +
      "JOIN p.perfil pf " +
      "JOIN pf.usuario u " +
      "WHERE p.fechaPrestamo >= :fechaLimite " +
      "GROUP BY u.id, u.username, pf.nombreVisible " +
      "ORDER BY COUNT(p) DESC")
  List<Object[]> getUsuariosMasActivosRecientes(@Param("fechaLimite") LocalDate fechaLimite, Pageable pageable);

  // ! --- ANÁLISIS DE RETENCIÓN Y COMPORTAMIENTO ---

  /**
   * Tasa de devolución puntual
   */
  @Query("SELECT " +
      "COUNT(CASE WHEN p.fechaDevolucionReal <= p.fechaDevolucionPrevista THEN 1 END) * 100.0 / COUNT(p) " +
      "FROM Prestamo p " +
      "WHERE p.fechaDevolucionReal IS NOT NULL " +
      "AND p.fechaPrestamo BETWEEN :fechaInicio AND :fechaFin")
  Double getTasaDevolucionPuntual(
      @Param("fechaInicio") LocalDate fechaInicio,
      @Param("fechaFin") LocalDate fechaFin);

  /**
   * Préstamos por día de la semana
   */
  @Query("SELECT DAYOFWEEK(p.fechaPrestamo), COUNT(p) " +
      "FROM Prestamo p " +
      "WHERE p.fechaPrestamo BETWEEN :fechaInicio AND :fechaFin " +
      "GROUP BY DAYOFWEEK(p.fechaPrestamo) " +
      "ORDER BY DAYOFWEEK(p.fechaPrestamo)")
  List<Object[]> getPrestamosPorDiaSemana(
      @Param("fechaInicio") LocalDate fechaInicio,
      @Param("fechaFin") LocalDate fechaFin);

  // ! --- MÉTRICAS DE RENDIMIENTO DEL SISTEMA ---

  /**
   * Contenidos con mayor tiempo de préstamo promedio
   */
  @Query("SELECT c.obra.titulo, AVG(DATEDIFF(p.fechaDevolucionReal, p.fechaPrestamo)) as tiempoPromedio " +
      "FROM Prestamo p JOIN p.contenido c " +
      "WHERE p.fechaDevolucionReal IS NOT NULL " +
      "GROUP BY c.id, c.obra.titulo " +
      "HAVING COUNT(p) >= :minimosPrestamos " +
      "ORDER BY tiempoPromedio DESC")
  List<Object[]> getContenidosConMayorTiempoPrestamo(
      @Param("minimosPrestamos") Long minimosPrestamos,
      Pageable pageable);

  /**
   * Resumen ejecutivo de préstamos
   */
  @Query("SELECT " +
      "COUNT(p) as totalPrestamos, " +
      "COUNT(CASE WHEN p.estado = 'ACTIVO' THEN 1 END) as prestamosActivos, " +
      "COUNT(CASE WHEN p.estado = 'VENCIDO' THEN 1 END) as prestamosVencidos, " +
      "COUNT(CASE WHEN p.estado = 'DEVUELTO' THEN 1 END) as prestamosDevueltos, " +
      "AVG(CASE WHEN p.fechaDevolucionReal IS NOT NULL " +
      "THEN DATEDIFF(p.fechaDevolucionReal, p.fechaPrestamo) END) as tiempoPromedioPrestamo " +
      "FROM Prestamo p " +
      "WHERE p.fechaPrestamo BETWEEN :fechaInicio AND :fechaFin")
  List<Object[]> getResumenEjecutivoPrestamos(
      @Param("fechaInicio") LocalDate fechaInicio,
      @Param("fechaFin") LocalDate fechaFin);

}