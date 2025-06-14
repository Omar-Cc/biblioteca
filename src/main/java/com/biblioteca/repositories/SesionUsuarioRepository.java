package com.biblioteca.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.biblioteca.models.acceso.SesionUsuario;
import com.biblioteca.enums.EstadoSesion;

@Repository
public interface SesionUsuarioRepository extends JpaRepository<SesionUsuario, Long> {

    // ========== BUSCAR POR TOKEN ==========
    Optional<SesionUsuario> findByToken(String token);

    // ========== BUSCAR POR USUARIO ==========
    List<SesionUsuario> findByUsuarioId(Long usuarioId);

    // ========== CORRECCIÓN: Usar 'estado' en lugar de 'activa' ==========
    List<SesionUsuario> findByUsuarioIdAndEstado(Long usuarioId, EstadoSesion estado);

    // Método específico para sesiones activas (reemplaza
    // findByUsuarioIdAndActivaTrue)
    default List<SesionUsuario> findSesionesActivasByUsuario(Long usuarioId) {
        return findByUsuarioIdAndEstado(usuarioId, EstadoSesion.ACTIVA);
    }

    // ========== BUSCAR POR FECHAS ==========
    List<SesionUsuario> findByFechaInicioAfter(LocalDateTime fecha);

    // ========== CORRECCIÓN: Usar 'fechaCierre' en lugar de 'fechaFin' ==========
    void deleteByFechaCierreBefore(LocalDateTime fecha);

    // ========== MÉTODOS ADICIONALES ÚTILES ==========

    // Buscar sesiones por usuario ordenadas por fecha de inicio
    List<SesionUsuario> findByUsuarioIdOrderByFechaInicioDesc(Long usuarioId);

    // Buscar sesiones por usuario y perfil
    List<SesionUsuario> findByUsuarioIdAndPerfilId(Long usuarioId, Long perfilId);

    // Buscar sesiones por IP
    List<SesionUsuario> findByIpAddress(String ipAddress);

    // Buscar sesiones válidas (activas y no expiradas) con Query personalizada
    @Query("SELECT s FROM SesionUsuario s WHERE s.usuario.id = :usuarioId " +
            "AND s.estado = :estado AND s.fechaExpiracion > :fechaActual")
    List<SesionUsuario> findSesionesValidasByUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("estado") EstadoSesion estado,
            @Param("fechaActual") LocalDateTime fechaActual);

    // Método específico para sesiones válidas y activas
    default List<SesionUsuario> findSesionesActivasValidasByUsuario(Long usuarioId) {
        return findSesionesValidasByUsuario(usuarioId, EstadoSesion.ACTIVA, LocalDateTime.now());
    }

    // Buscar sesiones expiradas que siguen marcadas como activas
    @Query("SELECT s FROM SesionUsuario s WHERE s.fechaExpiracion < :fechaActual " +
            "AND s.estado = :estado")
    List<SesionUsuario> findSesionesExpiradas(
            @Param("fechaActual") LocalDateTime fechaActual,
            @Param("estado") EstadoSesion estado);

    // Método específico para encontrar sesiones expiradas activas
    default List<SesionUsuario> findSesionesActivasExpiradas() {
        return findSesionesExpiradas(LocalDateTime.now(), EstadoSesion.ACTIVA);
    }

    // Contar sesiones activas por usuario
    @Query("SELECT COUNT(s) FROM SesionUsuario s WHERE s.usuario.id = :usuarioId " +
            "AND s.estado = :estado")
    Long countSesionesActivasByUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("estado") EstadoSesion estado);

    // Método específico para contar sesiones activas
    default Long countSesionesActivasByUsuario(Long usuarioId) {
        return countSesionesActivasByUsuario(usuarioId, EstadoSesion.ACTIVA);
    }

    // Buscar sesiones por tipo de dispositivo
    List<SesionUsuario> findByEsSesionMovilAndUsuarioId(Boolean esSesionMovil, Long usuarioId);

    // Buscar sesiones que recuerdan la sesión
    List<SesionUsuario> findByRecordarSesionTrueAndUsuarioId(Long usuarioId);

    // Buscar sesiones por rango de fechas
    @Query("SELECT s FROM SesionUsuario s WHERE s.usuario.id = :usuarioId " +
            "AND s.fechaInicio BETWEEN :fechaInicio AND :fechaFin")
    List<SesionUsuario> findSesionesByUsuarioAndRangoFechas(
            @Param("usuarioId") Long usuarioId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    // Limpiar sesiones cerradas antiguas (para mantenimiento)
    @Query("DELETE FROM SesionUsuario s WHERE s.fechaCierre < :fechaLimite " +
            "AND s.estado IN (:estadosCerrados)")
    void limpiarSesionesCerradasAntiguas(
            @Param("fechaLimite") LocalDateTime fechaLimite,
            @Param("estadosCerrados") List<EstadoSesion> estadosCerrados);

    // Obtener estadísticas de sesiones por usuario
    @Query("SELECT COUNT(s), AVG(s.tiempoActivoMinutos), MAX(s.fechaUltimaActividad) " +
            "FROM SesionUsuario s WHERE s.usuario.id = :usuarioId")
    Object[] getEstadisticasSesionesByUsuario(@Param("usuarioId") Long usuarioId);
}