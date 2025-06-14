package com.biblioteca.repositories.contenido;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biblioteca.models.contenido.ContenidoSerie;
import com.biblioteca.models.contenido.ContenidoSerieId;

@Repository
public interface ContenidoSerieRepository extends JpaRepository<ContenidoSerie, ContenidoSerieId> {
    
    List<ContenidoSerie> findBySerieId(Long serieId);
    
    Optional<ContenidoSerie> findByContenidoId(Long contenidoId);
}