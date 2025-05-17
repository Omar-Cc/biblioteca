package com.biblioteca.service.impl;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.biblioteca.dto.ContenidoRequestDTO;
import com.biblioteca.dto.ContenidoResponseDTO;
import com.biblioteca.dto.SerieResponseDTO;
import com.biblioteca.enums.TipoContenido;
import com.biblioteca.mapper.ContenidoMapper;
import com.biblioteca.models.Editorial;
import com.biblioteca.models.Obra;
import com.biblioteca.models.Serie;
import com.biblioteca.models.contenido.AudioLibro;
import com.biblioteca.models.contenido.Comic;
import com.biblioteca.models.contenido.Contenido;
import com.biblioteca.models.contenido.ContenidoMultimedia;
import com.biblioteca.models.contenido.ContenidoSerie;
import com.biblioteca.models.contenido.LibroFisico;
import com.biblioteca.models.contenido.Manga;
import com.biblioteca.models.contenido.MaterialEducativo;
import com.biblioteca.models.contenido.RevistaPeriodica;
import com.biblioteca.service.ContenidoService;
import com.biblioteca.service.EditorialService;
import com.biblioteca.service.ObraService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContenidoServiceImpl implements ContenidoService {
  private final List<Contenido> contenidos = new ArrayList<>();
  private final AtomicLong idCounter = new AtomicLong(0);
  private final List<ContenidoSerie> contenidosSeries = new ArrayList<>();
  private final AtomicLong serieIdCounter = new AtomicLong(0);
  private final List<Serie> series = new ArrayList<>();

  private final ObraService obraService;
  private final EditorialService editorialService;
  private final ContenidoMapper contenidoMapper;

  private final ObjectMapper objectMapper; // Para leer JSON
  private final ResourceLoader resourceLoader; // Para cargar el archivo

  @Override
  public ContenidoResponseDTO agregarContenido(ContenidoRequestDTO requestDTO) {
    if (requestDTO == null) {
      throw new IllegalArgumentException("El DTO de contenido no puede ser nulo");
    }

    Obra obra = obraService.obtenerEntidadObraPorId(requestDTO.getObraId())
        .orElseThrow(() -> new IllegalArgumentException("Obra no encontrada con ID: " + requestDTO.getObraId()));
    Editorial editorial = editorialService.obtenerEntidadEditorialPorId(requestDTO.getEditorialId())
        .orElseThrow(
            () -> new IllegalArgumentException("Editorial no encontrada con ID: " + requestDTO.getEditorialId()));

    Contenido contenido;
    // Instanciar el subtipo correcto basado en requestDTO.getTipo()
    switch (requestDTO.getTipo()) {
      case LIBRO_FISICO:
        LibroFisico libroFisico = LibroFisico.builder()
            .portadaUrl(requestDTO.getPortadaUrl())
            .sinopsis(requestDTO.getSinopsis())
            .precio(requestDTO.getPrecio())
            .enVenta(requestDTO.isEnVenta())
            .puedeSerPrestado(requestDTO.isPuedeSerPrestado())
            .isbn(requestDTO.getIsbn())
            .stockDisponible(requestDTO.getStockDisponible())
            .minStock(requestDTO.getMinStock())
            .ubicacionFisica(requestDTO.getUbicacionFisica())
            .formatoFisico(requestDTO.getFormatoFisico())
            .paginas(requestDTO.getPaginas())
            .build();
        contenido = libroFisico;
        break;

      case MANGA_FISICO:
        Manga manga = Manga.builder()
            .portadaUrl(requestDTO.getPortadaUrl())
            .sinopsis(requestDTO.getSinopsis())
            .precio(requestDTO.getPrecio())
            .enVenta(requestDTO.isEnVenta())
            .puedeSerPrestado(requestDTO.isPuedeSerPrestado())
            .isbn(requestDTO.getIsbn())
            .stockDisponible(requestDTO.getStockDisponible())
            .minStock(requestDTO.getMinStock())
            .ubicacionFisica(requestDTO.getUbicacionFisica())
            .formatoFisico(requestDTO.getFormatoFisico())
            .ilustrador(requestDTO.getIlustrador())
            .volumen(requestDTO.getVolumen())
            .sentidoLectura(requestDTO.getSentidoLectura())
            .build();
        contenido = manga;
        break;

      case COMIC_FISICO:
        Comic comic = Comic.builder()
            .portadaUrl(requestDTO.getPortadaUrl())
            .sinopsis(requestDTO.getSinopsis())
            .precio(requestDTO.getPrecio())
            .enVenta(requestDTO.isEnVenta())
            .puedeSerPrestado(requestDTO.isPuedeSerPrestado())
            .isbn(requestDTO.getIsbn())
            .stockDisponible(requestDTO.getStockDisponible())
            .minStock(requestDTO.getMinStock())
            .ubicacionFisica(requestDTO.getUbicacionFisica())
            .formatoFisico(requestDTO.getFormatoFisico())
            .ilustrador(requestDTO.getIlustrador())
            .volumen(requestDTO.getVolumen())
            .colorido(requestDTO.getColorido())
            .build();
        contenido = comic;
        break;

      case REVISTA_FISICA:
        RevistaPeriodica revista = RevistaPeriodica.builder()
            .portadaUrl(requestDTO.getPortadaUrl())
            .sinopsis(requestDTO.getSinopsis())
            .precio(requestDTO.getPrecio())
            .enVenta(requestDTO.isEnVenta())
            .puedeSerPrestado(requestDTO.isPuedeSerPrestado())
            .isbn(requestDTO.getIsbn())
            .stockDisponible(requestDTO.getStockDisponible())
            .minStock(requestDTO.getMinStock())
            .ubicacionFisica(requestDTO.getUbicacionFisica())
            .formatoFisico(requestDTO.getFormatoFisico())
            .periodicidad(requestDTO.getPeriodicidad())
            .edicion(requestDTO.getEdicion())
            .issn(requestDTO.getIssn())
            .build();
        contenido = revista;
        break;

      case AUDIO_LIBRO:
        AudioLibro audioLibro = AudioLibro.builder()
            .portadaUrl(requestDTO.getPortadaUrl())
            .sinopsis(requestDTO.getSinopsis())
            .precio(requestDTO.getPrecio())
            .enVenta(requestDTO.isEnVenta())
            .puedeSerPrestado(requestDTO.isPuedeSerPrestado())
            .isbn(requestDTO.getIsbn())
            .tamanioArchivo(requestDTO.getTamanioArchivo())
            .formato(requestDTO.getFormatoDigital())
            .permiteDescarga(requestDTO.getPermiteDescarga())
            .tipoLicencia(requestDTO.getTipoLicencia())
            .licencias(requestDTO.getLicencias())
            .duracion(
                requestDTO.getDuracionAudioLibro() != null ? Duration.parse(requestDTO.getDuracionAudioLibro()) : null)
            .narrador(requestDTO.getNarrador())
            .calidad(requestDTO.getCalidadAudio())
            .build();
        contenido = audioLibro;
        break;

      case MATERIAL_EDUCATIVO_DIGITAL:
        MaterialEducativo material = MaterialEducativo.builder()
            .portadaUrl(requestDTO.getPortadaUrl())
            .sinopsis(requestDTO.getSinopsis())
            .precio(requestDTO.getPrecio())
            .enVenta(requestDTO.isEnVenta())
            .puedeSerPrestado(requestDTO.isPuedeSerPrestado())
            .isbn(requestDTO.getIsbn())
            .tamanioArchivo(requestDTO.getTamanioArchivo())
            .formato(requestDTO.getFormatoDigital())
            .permiteDescarga(requestDTO.getPermiteDescarga())
            .tipoLicencia(requestDTO.getTipoLicencia())
            .licencias(requestDTO.getLicencias())
            .nivelEducativo(requestDTO.getNivelEducativo())
            .asignatura(requestDTO.getAsignatura())
            .recursos(requestDTO.getRecursosEducativos())
            .build();
        contenido = material;
        break;

      case CONTENIDO_MULTIMEDIA_DIGITAL:
        ContenidoMultimedia multimedia = ContenidoMultimedia.builder()
            .portadaUrl(requestDTO.getPortadaUrl())
            .sinopsis(requestDTO.getSinopsis())
            .precio(requestDTO.getPrecio())
            .enVenta(requestDTO.isEnVenta())
            .puedeSerPrestado(requestDTO.isPuedeSerPrestado())
            .isbn(requestDTO.getIsbn())
            .tamanioArchivo(requestDTO.getTamanioArchivo())
            .formato(requestDTO.getFormatoDigital())
            .permiteDescarga(requestDTO.getPermiteDescarga())
            .tipoLicencia(requestDTO.getTipoLicencia())
            .licencias(requestDTO.getLicencias())
            .duracion(
                requestDTO.getDuracionMultimedia() != null ? Duration.parse(requestDTO.getDuracionMultimedia()) : null)
            .calidad(requestDTO.getCalidadMultimedia())
            .requisitosReproduccion(requestDTO.getRequisitosReproduccion())
            .build();
        contenido = multimedia;
        break;

      default:
        throw new IllegalArgumentException("Tipo de contenido no soportado: " + requestDTO.getTipo());
    }

    contenido.setId(idCounter.incrementAndGet());
    contenido.setObra(obra);
    contenido.setEditorial(editorial);
    contenido.setActivo(true);
    contenido.setFechaCreacion(LocalDate.now());
    contenido.setTipo(requestDTO.getTipo());

    this.contenidos.add(contenido);
    System.out.println("Contenido agregado: ID " + contenido.getId() + ", Obra: "
        + obra.getTitulo() + ", Tipo: " + contenido.getTipo());
    return contenidoMapper.toResponseDTO(contenido);
  }

  @Override
  public Optional<ContenidoResponseDTO> obtenerContenidoPorId(Long id) {
    Optional<Contenido> contenidoOpt = contenidos.stream()
        .filter(c -> c.getId().equals(id))
        .findFirst();

    if (contenidoOpt.isEmpty()) {
      return Optional.empty();
    }

    ContenidoResponseDTO dto = contenidoMapper.toResponseDTO(contenidoOpt.get());

    // Buscar si este contenido pertenece a alguna serie
    contenidosSeries.stream()
        .filter(cs -> cs.getContenidoId().equals(id))
        .findFirst()
        .ifPresent(cs -> {
          Serie serie = series.stream()
              .filter(s -> s.getId().equals(cs.getSerieId()))
              .findFirst()
              .orElse(null);
          if (serie != null) {
            dto.setSerie(SerieResponseDTO.builder()
                .id(serie.getId())
                .nombre(serie.getNombre())
                .descripcion(serie.getDescripcion())
                .numeroVolumenes(serie.getNumeroVolumenes())
                .completa(serie.isCompleta())
                .ordenEnSerie(cs.getOrden())
                .build());
          }
        });

    return Optional.of(dto);
  }

  @Override
  public List<ContenidoResponseDTO> obtenerCatalogoPublico(
      Optional<String> tituloObra,
      Optional<Long> editorialId,
      Optional<TipoContenido> tipo,
      Optional<String> isbn,
      Optional<Long> serieId) {

    // Siempre activos para el público
    Stream<Contenido> streamContenidos = contenidos.stream().filter(Contenido::isActivo);

    return filtrarContenidos(streamContenidos, tituloObra, editorialId, tipo, isbn, serieId);
  }

  @Override
  public List<ContenidoResponseDTO> obtenerContenidosAdministracion(
      Optional<String> tituloObra,
      Optional<Long> editorialId,
      Optional<TipoContenido> tipo,
      Optional<String> isbn,
      Optional<Long> serieId,
      Optional<Boolean> soloActivos) {

    Stream<Contenido> streamContenidos = contenidos.stream();

    // Filtro adicional por activo si se especifica para la vista de admin
    if (soloActivos.isPresent()) {
      streamContenidos = streamContenidos.filter(c -> c.isActivo() == soloActivos.get());
    }
    // Si soloActivos no está presente, se devuelven todos (activos e inactivos) que
    // coincidan con otros filtros.

    return filtrarContenidos(streamContenidos, tituloObra, editorialId, tipo, isbn, serieId);
  }

  @Override
  public List<ContenidoResponseDTO> obtenerContenidosPorObra(Long obraId) {
    return contenidos.stream()
        .filter(c -> c.getObra().getId().equals(obraId) && c.isActivo())
        .map(contenidoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<ContenidoResponseDTO> actualizarContenido(Long id, ContenidoRequestDTO requestDTO) {
    Optional<Contenido> existenteOpt = contenidos.stream().filter(c -> c.getId().equals(id)).findFirst();
    if (existenteOpt.isEmpty()) {
      return Optional.empty();
    }
    Contenido existente = existenteOpt.get();

    // Validar que el tipo no cambie o manejarlo si se permite
    if (existente.getTipo() != requestDTO.getTipo()) {
      throw new IllegalArgumentException("No se puede cambiar el tipo de contenido durante la actualización.");
    }

    Obra obra = obraService.obtenerEntidadObraPorId(requestDTO.getObraId())
        .orElseThrow(() -> new IllegalArgumentException("Obra no encontrada con ID: " + requestDTO.getObraId()));
    Editorial editorial = editorialService.obtenerEntidadEditorialPorId(requestDTO.getEditorialId())
        .orElseThrow(
            () -> new IllegalArgumentException("Editorial no encontrada con ID: " + requestDTO.getEditorialId()));

    // Mapear campos comunes
    contenidoMapper.updateContenidoFromDto(requestDTO, existente);
    existente.setObra(obra);
    existente.setEditorial(editorial);

    // Mapear campos específicos del subtipo
    if (existente instanceof LibroFisico) {
      contenidoMapper.updateLibroFisicoFromDto(requestDTO, (LibroFisico) existente);
    } else if (existente instanceof Manga) {
      contenidoMapper.updateMangaFromDto(requestDTO, (Manga) existente);
    } else if (existente instanceof Comic) {
      contenidoMapper.updateComicFromDto(requestDTO, (Comic) existente);
    } else if (existente instanceof RevistaPeriodica) {
      contenidoMapper.updateRevistaPeriodicaFromDto(requestDTO, (RevistaPeriodica) existente);
    } else if (existente instanceof AudioLibro) {
      contenidoMapper.updateAudioLibroFromDto(requestDTO, (AudioLibro) existente);
    } else if (existente instanceof MaterialEducativo) {
      contenidoMapper.updateMaterialEducativoFromDto(requestDTO, (MaterialEducativo) existente);
    } else if (existente instanceof ContenidoMultimedia) {
      contenidoMapper.updateContenidoMultimediaFromDto(requestDTO, (ContenidoMultimedia) existente);
    }
    // Añadir más 'else if' para otros subtipos

    return Optional.of(contenidoMapper.toResponseDTO(existente));
  }

  @Override
  public boolean cambiarEstadoContenido(Long id, boolean estado) {
    Optional<Contenido> contenidoOpt = contenidos.stream().filter(c -> c.getId().equals(id)).findFirst();
    if (contenidoOpt.isPresent()) {
      contenidoOpt.get().setActivo(estado);
      System.out.println("Estado del contenido ID " + id + " cambiado a: " + (estado ? "ACTIVO" : "INACTIVO"));
      return true;
    }
    return false;
  }

  @Override
  public List<ContenidoResponseDTO> obtenerContenidosDestacados() {
    return contenidos.stream()
        .filter(Contenido::isActivo) // Solo contenidos activos
        .sorted((c1, c2) -> c2.getId().compareTo(c1.getId())) // Ordenar por ID descendente
        .limit(8) // Limitar a 8 resultados
        .map(contenidoMapper::toResponseDTO) // Mapear a DTO de respuesta
        .collect(Collectors.toList());
  }

  private List<ContenidoResponseDTO> filtrarContenidos(
      Stream<Contenido> streamContenidos,
      Optional<String> tituloObra,
      Optional<Long> editorialId,
      Optional<TipoContenido> tipo,
      Optional<String> isbn,
      Optional<Long> serieId) {

    if (tituloObra.isPresent() && StringUtils.hasText(tituloObra.get())) {
      String tituloLower = tituloObra.get().toLowerCase();
      streamContenidos = streamContenidos.filter(c -> c.getObra() != null &&
          c.getObra().getTitulo() != null &&
          c.getObra().getTitulo().toLowerCase().contains(tituloLower));
    }

    if (editorialId.isPresent()) {
      streamContenidos = streamContenidos.filter(c -> c.getEditorial() != null &&
          c.getEditorial().getId().equals(editorialId.get()));
    }

    if (tipo.isPresent()) {
      streamContenidos = streamContenidos.filter(c -> c.getTipo() == tipo.get());
    }

    if (isbn.isPresent() && StringUtils.hasText(isbn.get())) {
      String isbnVal = isbn.get();
      streamContenidos = streamContenidos.filter(c -> isbnVal.equals(c.getIsbn()));
    }

    if (serieId.isPresent()) {
      List<Long> contenidoIdsEnSerie = contenidosSeries.stream()
          .filter(cs -> cs.getSerieId().equals(serieId.get()))
          .map(ContenidoSerie::getContenidoId)
          .collect(Collectors.toList());
      streamContenidos = streamContenidos.filter(c -> contenidoIdsEnSerie.contains(c.getId()));
    }

    List<ContenidoResponseDTO> dtos = streamContenidos
        .map(contenidoMapper::toResponseDTO)
        .collect(Collectors.toList());

    // Asignar información de serie a cada contenido
    dtos.forEach(dto -> {
      contenidosSeries.stream()
          .filter(cs -> cs.getContenidoId().equals(dto.getId()))
          .findFirst()
          .ifPresent(cs -> {
            Serie serie = series.stream()
                .filter(s -> s.getId().equals(cs.getSerieId()))
                .findFirst()
                .orElse(null);
            if (serie != null) {
              dto.setSerie(SerieResponseDTO.builder()
                  .id(serie.getId())
                  .nombre(serie.getNombre())
                  .descripcion(serie.getDescripcion())
                  .numeroVolumenes(serie.getNumeroVolumenes())
                  .completa(serie.isCompleta())
                  .ordenEnSerie(cs.getOrden())
                  .build());
            }
          });
    });

    return dtos;
  }

  // Método para inicializar con datos de ejemplo
  @PostConstruct
  public void initData() {
    try {
      // Primero cargar series
      InputStream seriesInputStream = resourceLoader.getResource("classpath:data/series-data.json").getInputStream();
      List<Map<String, Object>> seriesData = objectMapper.readValue(seriesInputStream,
          new TypeReference<List<Map<String, Object>>>() {
          });

      for (Map<String, Object> serieData : seriesData) {
        Serie serie = Serie.builder()
            .id(serieIdCounter.incrementAndGet())
            .nombre((String) serieData.get("nombre"))
            .descripcion((String) serieData.get("descripcion"))
            .numeroVolumenes(((Integer) serieData.get("numeroVolumenes")))
            .completa((Boolean) serieData.get("completa"))
            .build();
        series.add(serie);
        System.out.println("Serie cargada: " + serie.getNombre());
      }

      // Luego cargar contenidos
      InputStream contenidosInputStream = resourceLoader.getResource("classpath:data/contenidos-data.json")
          .getInputStream();
      List<ContenidoRequestDTO> contenidosDTOs = objectMapper.readValue(contenidosInputStream,
          new TypeReference<List<ContenidoRequestDTO>>() {
          });

      for (ContenidoRequestDTO dto : contenidosDTOs) {
        try {
          ContenidoResponseDTO respuesta = agregarContenido(dto);

          // Si tiene serieId, crear la relación con la serie
          if (dto.getSerieId() != null) {
            contenidosSeries.add(new ContenidoSerie(
                dto.getSerieId(),
                respuesta.getId(),
                dto.getOrdenEnSerie() != null ? dto.getOrdenEnSerie() : 1));
          }
        } catch (Exception e) {
          System.err.println("Error al crear contenido: " + e.getMessage());
        }
      }

      System.out
          .println("Datos iniciales cargados: " + contenidos.size() + " contenidos y " + series.size() + " series");
    } catch (Exception e) {
      System.err.println("Error al cargar datos iniciales: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
