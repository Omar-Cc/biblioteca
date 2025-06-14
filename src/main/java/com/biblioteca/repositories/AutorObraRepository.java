package com.biblioteca.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.biblioteca.models.contenido.AutorObra;
import com.biblioteca.models.contenido.AutorObraId;

@Repository
public interface AutorObraRepository extends JpaRepository<AutorObra, AutorObraId> {

  // Método para eliminar todas las entradas de AutorObra para una obra específica
  @Modifying // Necesario para consultas de eliminación o actualización
  @Query("DELETE FROM AutorObra ao WHERE ao.obra.id = :obraId")
  void deleteByObraId(@Param("obraId") Long obraId);

  // Si no usas una clase Id compuesta y AutorObra tiene su propio ID Long:
  // void deleteByObra_Id(Long obraId); // Spring Data JPA generaría la consulta
}