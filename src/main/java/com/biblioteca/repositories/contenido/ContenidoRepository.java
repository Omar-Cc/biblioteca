package com.biblioteca.repositories.contenido;

import com.biblioteca.models.contenido.Contenido;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.biblioteca.enums.TipoContenido;

@Repository
public interface ContenidoRepository extends JpaRepository<Contenido, Long>, JpaSpecificationExecutor<Contenido> {

	List<Contenido> findByObraIdAndActivoTrue(Long obraId);

	List<Contenido> findTop8ByActivoTrueOrderByIdDesc();

	@Query("SELECT c FROM Contenido c " +
			"LEFT JOIN FETCH c.obra o " +
			"LEFT JOIN FETCH c.editorial e " +
			"WHERE (:titulo IS NULL OR :titulo = '' OR LOWER(o.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) AND " +
			"(:editorialId IS NULL OR e.id = :editorialId) AND " +
			"(:tipo IS NULL OR c.tipo = :tipo) AND " +
			"(:isbn IS NULL OR :isbn = '' OR LOWER(c.isbn) LIKE LOWER(CONCAT('%', :isbn, '%'))) AND " +
			"(:activo IS NULL OR c.activo = :activo)")
	List<Contenido> findByFiltros(
			@Param("titulo") String titulo,
			@Param("editorialId") Long editorialId,
			@Param("tipo") TipoContenido tipo,
			@Param("isbn") String isbn,
			@Param("activo") Boolean activo);

	@Query("SELECT c FROM Contenido c " +
			"LEFT JOIN FETCH c.obra o " +
			"LEFT JOIN FETCH c.editorial e " +
			"WHERE c.id IN :ids AND " +
			"(:titulo IS NULL OR :titulo = '' OR LOWER(o.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) AND " +
			"(:editorialId IS NULL OR e.id = :editorialId) AND " +
			"(:tipo IS NULL OR c.tipo = :tipo) AND " +
			"(:isbn IS NULL OR :isbn = '' OR LOWER(c.isbn) LIKE LOWER(CONCAT('%', :isbn, '%'))) AND " +
			"(:activo IS NULL OR c.activo = :activo)")
	List<Contenido> findByIdInAndFiltros(
			@Param("ids") List<Long> ids,
			@Param("titulo") String titulo,
			@Param("editorialId") Long editorialId,
			@Param("tipo") TipoContenido tipo,
			@Param("isbn") String isbn,
			@Param("activo") Boolean activo);

	@Query("SELECT DISTINCT c FROM Contenido c " +
			"LEFT JOIN FETCH c.obra o " +
			"LEFT JOIN FETCH c.editorial e " +
			"LEFT JOIN o.autoresObras ao " +
			"LEFT JOIN ao.autor a " +
			"WHERE c.activo = true AND " +
			"(:query IS NULL OR :query = '' OR " +
			"LOWER(o.titulo) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
			"LOWER(a.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
			"LOWER(c.isbn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
			"LOWER(c.sinopsis) LIKE LOWER(CONCAT('%', :query, '%')))")
	List<Contenido> findBySearchQuerySimple(@Param("query") String query);

	@Query("SELECT COUNT(DISTINCT c.id) FROM Contenido c " +
			"LEFT JOIN c.obra o " +
			"LEFT JOIN o.autoresObras ao " +
			"LEFT JOIN ao.autor a " +
			"WHERE c.activo = true AND " +
			"(:query IS NULL OR :query = '' OR " +
			"LOWER(o.titulo) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
			"LOWER(a.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
			"LOWER(c.isbn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
			"LOWER(c.sinopsis) LIKE LOWER(CONCAT('%', :query, '%')))")
	Long countBySearchQuery(@Param("query") String query);

	@Query("SELECT DISTINCT c FROM Contenido c " +
			"LEFT JOIN FETCH c.obra o " +
			"LEFT JOIN FETCH c.editorial e " +
			"LEFT JOIN o.autoresObras ao " +
			"LEFT JOIN ao.autor a " +
			"WHERE c.activo = true AND " +
			"(:query IS NULL OR :query = '' OR " +
			"LOWER(o.titulo) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
			"LOWER(a.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
			"LOWER(c.isbn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
			"LOWER(c.sinopsis) LIKE LOWER(CONCAT('%', :query, '%')))")
	Page<Contenido> findBySearchQuery(@Param("query") String query, Pageable pageable);

	@Query("SELECT DISTINCT c FROM Contenido c " +
			"LEFT JOIN FETCH c.obra o " +
			"LEFT JOIN o.autoresObras ao " +
			"LEFT JOIN ao.autor a " +
			"WHERE c.activo = true AND " +
			"(:query IS NULL OR :query = '' OR " +
			"LOWER(o.titulo) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
			"LOWER(a.nombre) LIKE LOWER(CONCAT('%', :query, '%'))) " +
			"ORDER BY o.titulo")
	List<Contenido> findSuggestionsByQuery(@Param("query") String query, Pageable pageable);

	@Query("SELECT DISTINCT c FROM Contenido c " +
			"LEFT JOIN c.obra.autoresObras ao " +
			"LEFT JOIN ao.autor a " +
			"WHERE c.activo = true AND " +
			"(:titulo IS NULL OR :titulo = '' OR LOWER(c.obra.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) AND " +
			"(:autores IS NULL OR :autores = '' OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :autores, '%')))")
	List<Contenido> findTop10ByActivoTrueAndObraTituloContainingIgnoreCaseOrObraAutoresContainingIgnoreCase(
			@Param("titulo") String titulo,
			@Param("autores") String autores,
			Sort sort);

	@Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
			"FROM Contenido c " +
			"WHERE c.activo = true AND " +
			"LOWER(c.isbn) = LOWER(:isbn)")
	boolean existsByIsbnIgnoreCase(@Param("isbn") String isbn);
}