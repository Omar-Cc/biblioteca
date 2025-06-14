package com.biblioteca.repositories;

import com.biblioteca.dto.ActividadRecienteDTO;
import com.biblioteca.models.acceso.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  Optional<Usuario> findByUsername(String username);

  Optional<Usuario> findByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  // Para contar usuarios nuevos en el mes actual
  long countByFechaRegistroBetween(LocalDateTime startOfMonth, LocalDateTime endOfMonth);

  // Para actividades recientes (ejemplo, basado en ultimaActividad de Usuario)
  // Se usa una consulta JPQL para seleccionar campos específicos para
  // ActividadRecienteDTO
  @Query("SELECT new com.biblioteca.dto.ActividadRecienteDTO(u.username, u.ultimaActividad, 'Actualización de perfil/actividad') FROM Usuario u WHERE u.ultimaActividad IS NOT NULL ORDER BY u.ultimaActividad DESC")
  List<ActividadRecienteDTO> findActividadesRecientesUsuarios(Pageable pageable);

  @Query("SELECT COUNT(u) FROM Usuario u WHERE NOT EXISTS (SELECT 1 FROM Suscripcion s WHERE s.usuario = u AND s.estado IN ('ACTIVA', 'PRUEBA'))")
  long contarUsuariosSinSuscripcionActiva();

  // Si también quieres incluir sesiones, necesitarías una consulta más compleja o
  // unir resultados.
  // Por ejemplo, para sesiones (asumiendo que SesionUsuario tiene
  // fechaInicioSesion):
  // @Query("SELECT new
  // com.biblioteca.dto.ActividadRecienteDTO(s.usuario.username,
  // s.fechaInicioSesion, 'Inicio de sesión') FROM SesionUsuario s ORDER BY
  // s.fechaInicioSesion DESC")
  // List<ActividadRecienteDTO> findActividadesRecientesSesiones(Pageable
  // pageable);
}