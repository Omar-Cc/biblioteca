package com.biblioteca.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.biblioteca.dto.ContenidoRequestDTO;
import com.biblioteca.dto.ContenidoResponseDTO;
import com.biblioteca.enums.TipoContenido;
import com.biblioteca.models.contenido.Contenido;

public interface ContenidoService {
	ContenidoResponseDTO agregarContenido(ContenidoRequestDTO requestDTO); // Devuelve el contenido con ID

	Optional<ContenidoResponseDTO> obtenerContenidoPorId(Long id);

	Optional<Contenido> obtenerEntidadContenidoPorId(Long id);

	/**
	 * Obtiene contenidos para el catálogo público, solo aquellos que están activos.
	 * Permite aplicar filtros adicionales.
	 *
	 * @param tituloObra  Optional para filtrar por título de la obra.
	 * @param editorialId Optional para filtrar por ID de la editorial.
	 * @param tipo        Optional para filtrar por TipoContenido.
	 * @param isbn        Optional para filtrar por ISBN.
	 * @param serieId     Optional para filtrar por ID de la serie.
	 * @return Lista de DTOs de contenido activos que coinciden con los filtros.
	 */
	List<ContenidoResponseDTO> obtenerCatalogoPublico(
			Optional<String> tituloObra,
			Optional<Long> editorialId,
			Optional<TipoContenido> tipo,
			Optional<String> isbn,
			Optional<Long> serieId
	// Podrías añadir más filtros públicos aquí si son diferentes a los de admin
	);

	/**
	 * Obtiene contenidos para la vista de administración.
	 * No filtra por estado activo por defecto (muestra todos).
	 * Permite aplicar filtros adicionales.
	 *
	 * @param tituloObra  Optional para filtrar por título de la obra.
	 * @param editorialId Optional para filtrar por ID de la editorial.
	 * @param tipo        Optional para filtrar por TipoContenido.
	 * @param isbn        Optional para filtrar por ISBN.
	 * @param serieId     Optional para filtrar por ID de la serie.
	 * @param soloActivos Optional para filtrar adicionalmente por estado activo si
	 *                    es necesario para admin.
	 * @return Lista de DTOs de contenido que coinciden con los filtros.
	 */
	List<ContenidoResponseDTO> obtenerContenidosAdministracion(
			Optional<String> tituloObra,
			Optional<Long> editorialId,
			Optional<TipoContenido> tipo,
			Optional<String> isbn,
			Optional<Long> serieId,
			Optional<Boolean> soloActivos // Nuevo filtro opcional para admin
	);

	Optional<ContenidoResponseDTO> actualizarContenido(Long id, ContenidoRequestDTO requestDTO);

	boolean cambiarEstadoContenido(Long id, boolean estado); // true para activar, false para desactivar

	List<ContenidoResponseDTO> obtenerContenidosPorObra(Long obraId);

	/**
	 * Obtiene una lista de contenidos destacados para mostrar en la página
	 * principal.
	 * 
	 * @return Lista de contenidos destacados
	 */
	List<ContenidoResponseDTO> obtenerContenidosDestacados();

	/**
	 * Busca contenidos por texto en múltiples campos
	 */
	List<ContenidoResponseDTO> buscarContenido(String query, Optional<TipoContenido> tipo,
			Optional<Long> editorialId,
			int page, int size);

	/**
	 * Cuenta el total de resultados para una búsqueda
	 */
	long contarResultadosBusqueda(String query, Optional<TipoContenido> tipo, Optional<Long> editorialId);

	/**
	 * Obtiene sugerencias para búsqueda en tiempo real
	 */
	List<Map<String, Object>> obtenerSugerenciasBusqueda(String query);
}