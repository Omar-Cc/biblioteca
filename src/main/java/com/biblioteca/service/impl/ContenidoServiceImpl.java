package com.biblioteca.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.ContenidoRequestDTO;
import com.biblioteca.dto.ContenidoResponseDTO;
import com.biblioteca.dto.SerieResponseDTO;
import com.biblioteca.enums.TipoContenido;
import com.biblioteca.exceptions.RecursoNoEncontradoException;
import com.biblioteca.mapper.ContenidoMapper;
import com.biblioteca.models.contenido.AudioLibro;
import com.biblioteca.models.contenido.Comic;
import com.biblioteca.models.contenido.Contenido;
import com.biblioteca.models.contenido.ContenidoMultimedia;
import com.biblioteca.models.contenido.ContenidoSerie;
import com.biblioteca.models.contenido.Editorial;
import com.biblioteca.models.contenido.LibroFisico;
import com.biblioteca.models.contenido.Manga;
import com.biblioteca.models.contenido.MaterialEducativo;
import com.biblioteca.models.contenido.Obra;
import com.biblioteca.models.contenido.RevistaPeriodica;
import com.biblioteca.models.contenido.Serie;
import com.biblioteca.repositories.EditorialRepository;
import com.biblioteca.repositories.ObraRepository;
import com.biblioteca.repositories.SerieRepository;
import com.biblioteca.repositories.contenido.ContenidoRepository;
import com.biblioteca.repositories.contenido.ContenidoSerieRepository;
import com.biblioteca.service.ContenidoService;
import com.biblioteca.service.ObraService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContenidoServiceImpl implements ContenidoService {

  private final ContenidoRepository contenidoRepository;
  private final SerieRepository serieRepository;
  private final ContenidoSerieRepository contenidoSerieRepository;
  private final ObraRepository obraRepository;
  private final EditorialRepository editorialRepository;
  private final ContenidoMapper contenidoMapper;
  private final ObraService obraService;

  @Override
  @Transactional
  public ContenidoResponseDTO agregarContenido(ContenidoRequestDTO requestDTO) {
    if (requestDTO == null) {
      throw new IllegalArgumentException("El DTO de contenido no puede ser nulo");
    }

    Obra obra = obraRepository.findById(requestDTO.getObraId())
        .orElseThrow(() -> new RecursoNoEncontradoException("Obra no encontrada con ID: " + requestDTO.getObraId()));
    Editorial editorial = editorialRepository.findById(requestDTO.getEditorialId())
        .orElseThrow(
            () -> new RecursoNoEncontradoException("Editorial no encontrada con ID: " + requestDTO.getEditorialId()));

    Contenido contenido;
    switch (requestDTO.getTipo()) {
      case LIBRO_FISICO:
        LibroFisico libroFisico = new LibroFisico();
        contenidoMapper.updateLibroFisicoFromDto(requestDTO, libroFisico);
        contenido = libroFisico;
        break;
      case MANGA_FISICO:
        Manga manga = new Manga();
        contenidoMapper.updateMangaFromDto(requestDTO, manga);
        contenido = manga;
        break;
      case COMIC_FISICO:
        Comic comic = new Comic();
        contenidoMapper.updateComicFromDto(requestDTO, comic);
        contenido = comic;
        break;
      case REVISTA_FISICA:
        RevistaPeriodica revista = new RevistaPeriodica();
        contenidoMapper.updateRevistaPeriodicaFromDto(requestDTO, revista);
        contenido = revista;
        break;
      case AUDIO_LIBRO:
        AudioLibro audioLibro = new AudioLibro();
        contenidoMapper.updateAudioLibroFromDto(requestDTO, audioLibro);
        if (requestDTO.getDuracionAudioLibro() != null) {
          audioLibro.setDuracion(Duration.parse(requestDTO.getDuracionAudioLibro()));
        }
        contenido = audioLibro;
        break;
      case MATERIAL_EDUCATIVO_DIGITAL:
        MaterialEducativo material = new MaterialEducativo();
        contenidoMapper.updateMaterialEducativoFromDto(requestDTO, material);
        contenido = material;
        break;
      case CONTENIDO_MULTIMEDIA_DIGITAL:
        ContenidoMultimedia multimedia = new ContenidoMultimedia();
        contenidoMapper.updateContenidoMultimediaFromDto(requestDTO, multimedia);
        if (requestDTO.getDuracionMultimedia() != null) {
          multimedia.setDuracion(Duration.parse(requestDTO.getDuracionMultimedia()));
        }
        contenido = multimedia;
        break;
      default:
        throw new IllegalArgumentException("Tipo de contenido no soportado: " + requestDTO.getTipo());
    }

    contenidoMapper.updateContenidoFromDto(requestDTO, contenido);
    contenido.setObra(obra);
    contenido.setEditorial(editorial);
    contenido.setActivo(true);
    contenido.setFechaCreacion(LocalDate.now());
    contenido.setTipo(requestDTO.getTipo());

    contenido = contenidoRepository.save(contenido);

    if (requestDTO.getSerie() != null) {
      Serie serie = serieRepository.findById(requestDTO.getSerie().getId())
          .orElseThrow(
              () -> new RecursoNoEncontradoException("Serie no encontrada con ID: " + requestDTO.getSerie().getId()));
      ContenidoSerie cs = new ContenidoSerie();
      cs.setSerie(serie);
      cs.setContenido(contenido);
      cs.setOrden(requestDTO.getOrdenEnSerie() != null ? requestDTO.getOrdenEnSerie() : 1);
      contenidoSerieRepository.save(cs);
    }

    return mapToResponseDTOWithSerie(contenido);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ContenidoResponseDTO> obtenerContenidoPorId(Long id) {
    return contenidoRepository.findById(id)
        .map(this::mapToResponseDTOWithSerie);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Contenido> obtenerEntidadContenidoPorId(Long id) {
    return contenidoRepository.findById(id)
        .or(() -> {
          log.warn("Contenido no encontrado con ID: {}", id);
          return Optional.empty();
        });
  }

  @Override
  @Transactional(readOnly = true)
  public List<ContenidoResponseDTO> obtenerCatalogoPublico(
      Optional<String> tituloObra,
      Optional<Long> editorialId,
      Optional<TipoContenido> tipo,
      Optional<String> isbn,
      Optional<Long> serieId) {
    return obtenerContenidosFiltrados(tituloObra, editorialId, tipo, isbn, serieId, Optional.of(true));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ContenidoResponseDTO> obtenerContenidosAdministracion(
      Optional<String> tituloObra,
      Optional<Long> editorialId,
      Optional<TipoContenido> tipo,
      Optional<String> isbn,
      Optional<Long> serieId,
      Optional<Boolean> soloActivos) {
    return obtenerContenidosFiltrados(tituloObra, editorialId, tipo, isbn, serieId, soloActivos);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ContenidoResponseDTO> obtenerContenidosPorObra(Long obraId) {
    return contenidoRepository.findByObraIdAndActivoTrue(obraId).stream()
        .map(this::mapToResponseDTOWithSerie)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Optional<ContenidoResponseDTO> actualizarContenido(Long id, ContenidoRequestDTO requestDTO) {
    Contenido existente = contenidoRepository.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Contenido no encontrado con ID: " + id));

    if (existente.getTipo() != requestDTO.getTipo()) {
      throw new IllegalArgumentException("No se puede cambiar el tipo de contenido durante la actualización.");
    }

    Obra obra = obraRepository.findById(requestDTO.getObraId())
        .orElseThrow(() -> new RecursoNoEncontradoException("Obra no encontrada con ID: " + requestDTO.getObraId()));
    Editorial editorial = editorialRepository.findById(requestDTO.getEditorialId())
        .orElseThrow(
            () -> new RecursoNoEncontradoException("Editorial no encontrada con ID: " + requestDTO.getEditorialId()));

    contenidoMapper.updateContenidoFromDto(requestDTO, existente);
    existente.setObra(obra);
    existente.setEditorial(editorial);

    switch (existente.getTipo()) {
      case LIBRO_FISICO:
        contenidoMapper.updateLibroFisicoFromDto(requestDTO, (LibroFisico) existente);
        break;
      case MANGA_FISICO:
        contenidoMapper.updateMangaFromDto(requestDTO, (Manga) existente);
        break;
      case COMIC_FISICO:
        contenidoMapper.updateComicFromDto(requestDTO, (Comic) existente);
        break;
      case REVISTA_FISICA:
        contenidoMapper.updateRevistaPeriodicaFromDto(requestDTO, (RevistaPeriodica) existente);
        break;
      case AUDIO_LIBRO:
        contenidoMapper.updateAudioLibroFromDto(requestDTO, (AudioLibro) existente);
        if (requestDTO.getDuracionAudioLibro() != null) {
          ((AudioLibro) existente).setDuracion(Duration.parse(requestDTO.getDuracionAudioLibro()));
        }
        break;
      case MATERIAL_EDUCATIVO_DIGITAL:
        contenidoMapper.updateMaterialEducativoFromDto(requestDTO, (MaterialEducativo) existente);
        break;
      case CONTENIDO_MULTIMEDIA_DIGITAL:
        contenidoMapper.updateContenidoMultimediaFromDto(requestDTO, (ContenidoMultimedia) existente);
        if (requestDTO.getDuracionMultimedia() != null) {
          ((ContenidoMultimedia) existente).setDuracion(Duration.parse(requestDTO.getDuracionMultimedia()));
        }
        break;
      default:
        throw new IllegalArgumentException("Tipo de contenido no soportado para actualización: " + existente.getTipo());
    }

    Contenido contenidoActualizado = contenidoRepository.save(existente);

    Optional<ContenidoSerie> csExistenteOpt = contenidoSerieRepository.findByContenidoId(id);

    if (requestDTO.getSerie() != null) {
      Serie serie = serieRepository.findById(requestDTO.getSerie().getId())
          .orElseThrow(
              () -> new RecursoNoEncontradoException("Serie no encontrada con ID: " + requestDTO.getSerie().getId()));
      ContenidoSerie cs = csExistenteOpt.orElseGet(ContenidoSerie::new);
      cs.setSerie(serie);
      cs.setContenido(contenidoActualizado);
      cs.setOrden(requestDTO.getOrdenEnSerie() != null ? requestDTO.getOrdenEnSerie() : 1);
      contenidoSerieRepository.save(cs);
    } else {
      csExistenteOpt.ifPresent(contenidoSerieRepository::delete);
    }

    return Optional.of(mapToResponseDTOWithSerie(contenidoActualizado));
  }

  @Override
  @Transactional
  public boolean cambiarEstadoContenido(Long id, boolean estado) {
    Optional<Contenido> contenidoOpt = contenidoRepository.findById(id);
    if (contenidoOpt.isPresent()) {
      Contenido contenido = contenidoOpt.get();
      contenido.setActivo(estado);
      contenidoRepository.save(contenido);
      return true;
    }
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ContenidoResponseDTO> obtenerContenidosDestacados() {
    return contenidoRepository.findTop8ByActivoTrueOrderByIdDesc().stream()
        .map(this::mapToResponseDTOWithSerie)
        .collect(Collectors.toList());
  }

  @Override
  public List<ContenidoResponseDTO> buscarContenido(String query, Optional<TipoContenido> tipo,
      Optional<Long> editorialId, int page, int size) {

    try {
      // Validación simple de entrada
      if (query != null && query.length() > 200) {
        query = query.substring(0, 200);
      }

      // Usar el método simplificado del repositorio
      List<Contenido> todosLosResultados = contenidoRepository.findBySearchQuerySimple(query);

      // Aplicar filtros adicionales en memoria
      java.util.stream.Stream<Contenido> stream = todosLosResultados.stream();

      if (tipo.isPresent()) {
        stream = stream.filter(c -> c.getTipo().equals(tipo.get()));
      }

      if (editorialId.isPresent()) {
        stream = stream.filter(c -> c.getEditorial() != null &&
            c.getEditorial().getId().equals(editorialId.get()));
      }

      // Ordenar por título
      List<Contenido> contenidosOrdenados = stream
          .sorted((c1, c2) -> {
            String titulo1 = c1.getObra() != null ? c1.getObra().getTitulo() : "";
            String titulo2 = c2.getObra() != null ? c2.getObra().getTitulo() : "";
            return titulo1.compareToIgnoreCase(titulo2);
          })
          .collect(Collectors.toList());

      // Aplicar paginación en memoria
      int start = page * size;
      int end = Math.min(start + size, contenidosOrdenados.size());

      if (start >= contenidosOrdenados.size()) {
        return List.of();
      }

      List<Contenido> paginados = contenidosOrdenados.subList(start, end);

      return paginados.stream()
          .map(contenidoMapper::toResponseDTO)
          .collect(Collectors.toList());

    } catch (Exception e) {
      log.error("Error en búsqueda: ", e);
      return List.of();
    }
  }

  @Override
  public long contarResultadosBusqueda(String query, Optional<TipoContenido> tipo,
      Optional<Long> editorialId) {

    try {
      // Validación simple
      if (query != null && query.length() > 200) {
        query = query.substring(0, 200);
      }

      // Para hacer el conteo más simple, usar la misma lógica
      List<Contenido> todosLosResultados = contenidoRepository.findBySearchQuerySimple(query);

      java.util.stream.Stream<Contenido> stream = todosLosResultados.stream();

      if (tipo.isPresent()) {
        stream = stream.filter(c -> c.getTipo().equals(tipo.get()));
      }

      if (editorialId.isPresent()) {
        stream = stream.filter(c -> c.getEditorial() != null &&
            c.getEditorial().getId().equals(editorialId.get()));
      }

      return stream.count();

    } catch (Exception e) {
      log.error("Error en conteo: ", e);
      return 0;
    }
  }

  @Override
  public List<Map<String, Object>> obtenerSugerenciasBusqueda(String query) {
    try {
      if (query == null || query.trim().length() < 3) {
        return List.of();
      }

      if (query.length() > 100) {
        query = query.substring(0, 100);
      }

      // Usar PageRequest para limitar resultados
      PageRequest pageRequest = PageRequest.of(0, 5);
      List<Contenido> contenidos = contenidoRepository.findSuggestionsByQuery(query.trim(), pageRequest);

      return contenidos.stream()
          .map(contenido -> {
            Map<String, Object> suggestion = new HashMap<>();
            suggestion.put("titulo", contenido.getObra().getTitulo());

            // Obtener autores de forma segura
            String autores = "Autor desconocido";
            try {
              if (contenido.getObra() != null && contenido.getObra().getAutoresObras() != null) {
                autores = contenido.getObra().getAutoresObras().stream()
                    .filter(ao -> ao != null && ao.getAutor() != null)
                    .map(ao -> ao.getAutor().getNombre())
                    .filter(nombre -> nombre != null && !nombre.trim().isEmpty())
                    .collect(Collectors.joining(", "));
                if (autores.isEmpty()) {
                  autores = "Autor desconocido";
                }
              }
            } catch (Exception e) {
              autores = "Autor desconocido";
            }
            suggestion.put("autor", autores);

            suggestion.put("tipo", contenido.getTipo().toString());
            suggestion.put("id", contenido.getId());
            return suggestion;
          })
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Error en sugerencias: ", e);
      return List.of();
    }
  }

  private List<ContenidoResponseDTO> obtenerContenidosFiltrados(
      Optional<String> tituloObra,
      Optional<Long> editorialId,
      Optional<TipoContenido> tipo,
      Optional<String> isbn,
      Optional<Long> serieIdFiltro,
      Optional<Boolean> soloActivos) {

    List<Contenido> contenidosFiltrados;

    if (serieIdFiltro.isPresent()) {
      List<Long> contenidoIdsEnSerie = contenidoSerieRepository.findBySerieId(serieIdFiltro.get())
          .stream()
          .map(cs -> cs.getContenido().getId())
          .collect(Collectors.toList());

      if (contenidoIdsEnSerie.isEmpty()) {
        return List.of();
      }
      contenidosFiltrados = contenidoRepository.findByIdInAndFiltros(
          contenidoIdsEnSerie,
          tituloObra.orElse(null),
          editorialId.orElse(null),
          tipo.orElse(null),
          isbn.orElse(null),
          soloActivos.orElse(null));
    } else {
      contenidosFiltrados = contenidoRepository.findByFiltros(
          tituloObra.orElse(null),
          editorialId.orElse(null),
          tipo.orElse(null),
          isbn.orElse(null),
          soloActivos.orElse(null));
    }

    return contenidosFiltrados.stream()
        .map(this::mapToResponseDTOWithSerie)
        .collect(Collectors.toList());
  }

  private ContenidoResponseDTO mapToResponseDTOWithSerie(Contenido contenido) {
    ContenidoResponseDTO dto = contenidoMapper.toResponseDTO(contenido);

    // Mapear la obra con autores correctamente usando ObraService
    if (contenido.getObra() != null) {
      obraService.obtenerObraPorId(contenido.getObra().getId())
          .ifPresent(obraConAutores -> dto.setObra(obraConAutores));
    }

    Optional<ContenidoSerie> csOpt = contenidoSerieRepository.findByContenidoId(contenido.getId());
    csOpt.ifPresent(cs -> {
      Serie serie = cs.getSerie();
      if (serie != null) {
        dto.setSerie(SerieResponseDTO.builder()
            .id(serie.getId())
            .nombre(serie.getNombre())
            .descripcion(serie.getDescripcion())
            .numeroVolumenes(serie.getNumeroVolumenes())
            .completa(serie.isCompleta())
            .ordenEnSerie(cs.getOrden())
            .build());
        dto.setOrdenEnSerie(cs.getOrden());
      }
    });
    return dto;
  }
}
