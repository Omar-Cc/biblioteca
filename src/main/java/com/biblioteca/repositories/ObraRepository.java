package com.biblioteca.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.biblioteca.models.contenido.Obra;

@Repository
public interface ObraRepository extends JpaRepository<Obra, Long> {
  // Para contar obras nuevas en un rango de fechas (mejor que filtrar en memoria)
  long countByFechaCreacionBetween(LocalDate inicio, LocalDate fin);

  // Para obtener las N obras más recientes (ejemplo para obras populares)
  // Usando JPQL para seleccionar solo las necesarias y ordenarlas
  @Query("SELECT o FROM Obra o ORDER BY o.fechaCreacion DESC")
  List<Obra> findTopNByOrderByFechaCreacionDesc(Pageable pageable);

  // Método simplificado si solo necesitas las N primeras sin paginación compleja:
  // List<Obra> findTopNByOrderByFechaCreacionDesc(int n); // Esto requeriría una
  // implementación custom o ajustar el nombre
  // si Spring Data no lo soporta directamente así.
  // La forma con Pageable es más estándar:
  // En el servicio:
  // obraRepository.findTopNByOrderByFechaCreacionDesc(PageRequest.of(0, limit))
}