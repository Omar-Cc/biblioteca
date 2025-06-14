package com.biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.biblioteca.dto.SerieRequestDTO;
import com.biblioteca.dto.SerieResponseDTO;
import com.biblioteca.models.contenido.Serie;

public interface SerieService {
    /**
     * Crea una nueva serie.
     *
     * @param serieRequestDTO Objeto que contiene los datos de la serie a crear.
     * @return Objeto que representa la serie creada.
     */
    SerieResponseDTO crearSerie(SerieRequestDTO serieRequestDTO);

    /**
     * Obtiene una serie por su ID.
     *
     * @param id ID de la serie a buscar.
     * @return Objeto que representa la serie encontrada, o un Optional vacío si no se encuentra.
     */
    Optional<SerieResponseDTO> obtenerSeriePorId(Long id);

    /**
     * Obtiene todas las series.
     *
     * @return Lista de objetos que representan todas las series.
     */
    List<SerieResponseDTO> obtenerTodasLasSeries();

    /**
     * Actualiza una serie existente.
     *
     * @param id ID de la serie a actualizar.
     * @param serieRequestDTO Objeto que contiene los nuevos datos de la serie.
     * @return Objeto que representa la serie actualizada, o un Optional vacío si no se encuentra.
     */
    Optional<SerieResponseDTO> actualizarSerie(Long id, SerieRequestDTO serieRequestDTO);

    /**
     * Elimina una serie por su ID.
     *
     * @param id ID de la serie a eliminar.
     * @return true si la serie fue eliminada exitosamente, false en caso contrario.
     */
    boolean eliminarSerie(Long id);

    /**
     * Obtiene la entidad Serie por su ID (para uso interno).
     *
     * @param id ID de la serie a buscar.
     * @return Objeto Serie encontrado, o un Optional vacío si no se encuentra.
     */
    Optional<Serie> obtenerEntidadSeriePorId(Long id);

    /**
     * Obtiene series completas.
     *
     * @return Lista de series que están completas.
     */
    List<SerieResponseDTO> obtenerSeriesCompletas();

    /**
     * Obtiene series en progreso.
     *
     * @return Lista de series que están en progreso.
     */
    List<SerieResponseDTO> obtenerSeriesEnProgreso();
}