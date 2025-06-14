package com.biblioteca.mapper.actividades;

import org.mapstruct.*;

import com.biblioteca.dto.actividades.PrestamoRequestDTO;
import com.biblioteca.dto.actividades.PrestamoResponseDTO;
import com.biblioteca.models.actividades.Prestamo;
import com.biblioteca.models.acceso.Perfil;
import com.biblioteca.models.contenido.Contenido;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface PrestamosMapper {

  // Request DTO a Entidad
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "contenido", ignore = true)
  @Mapping(target = "perfil", ignore = true)
  @Mapping(target = "fechaCreacion", ignore = true)
  @Mapping(target = "fechaActualizacion", ignore = true)
  Prestamo toEntity(PrestamoRequestDTO dto);

  // Post-mapeo para relaciones
  @AfterMapping
  default void setRelations(PrestamoRequestDTO dto, @MappingTarget Prestamo prestamo) {
    if (dto.getContenidoId() != null) {
      Contenido contenido = createContenidoProxy(dto.getContenidoId());
      prestamo.setContenido(contenido);
    }

    if (dto.getPerfilId() != null) {
      Perfil perfil = createPerfilProxy(dto.getPerfilId());
      prestamo.setPerfil(perfil);
    }
  }

  // Entidad a Response DTO
  @Mapping(target = "contenido", source = "contenido", qualifiedByName = "mapContenidoBasico")
  @Mapping(target = "perfil", source = "perfil", qualifiedByName = "mapPerfilBasico")
  @Mapping(target = "estadoDescripcion", source = "estado.descripcion")
  @Mapping(target = "vencido", expression = "java(prestamo.isVencido())")
  @Mapping(target = "devuelto", expression = "java(prestamo.isDevuelto())")
  @Mapping(target = "diasRetraso", expression = "java(prestamo.getDiasRetraso())")
  @Mapping(target = "diasRestantes", expression = "java(prestamo.getDiasRestantes())")
  @Mapping(target = "proximoAVencer", expression = "java(prestamo.isProximoAVencer(3))")
  PrestamoResponseDTO toResponseDTO(Prestamo prestamo);

  // Lista de entidades a lista de Response DTOs
  List<PrestamoResponseDTO> toResponseDTOList(List<Prestamo> prestamos);

  // Actualizar entidad desde Request DTO
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "contenido", ignore = true)
  @Mapping(target = "perfil", ignore = true)
  @Mapping(target = "fechaCreacion", ignore = true)
  @Mapping(target = "fechaActualizacion", ignore = true)
  void updateEntityFromDTO(PrestamoRequestDTO dto, @MappingTarget Prestamo prestamo);

  // ========== MÉTODOS AUXILIARES ==========
  default Contenido createContenidoProxy(Long contenidoId) {
    if (contenidoId == null) {
      return null;
    }

    return new Contenido() {
      @Override
      public Long getId() {
        return contenidoId;
      }

      @Override
      public void setId(Long id) {
        /* No-op para proxy */ }
    };
  }

  default Perfil createPerfilProxy(Long perfilId) {
    if (perfilId == null) {
      return null;
    }
    Perfil perfil = new Perfil();
    perfil.setId(perfilId);
    return perfil;
  }

  default String mapearAutores(com.biblioteca.models.contenido.Obra obra) {
    if (obra == null) {
      return "Autor no disponible";
    }

    try {
      if (obra.getAutoresObras() == null || obra.getAutoresObras().isEmpty()) {
        return "Autor no disponible";
      }

      List<String> nombresAutores = obra.getAutoresObras().stream()
          .filter(autorObra -> autorObra != null && autorObra.getAutor() != null)
          .map(autorObra -> autorObra.getAutor().getNombre())
          .filter(nombre -> nombre != null && !nombre.trim().isEmpty())
          .collect(java.util.stream.Collectors.toList());

      if (nombresAutores.isEmpty()) {
        return "Autor no disponible";
      }

      if (nombresAutores.size() > 3) {
        return String.join(", ", nombresAutores.subList(0, 3)) + " y otros";
      }

      return String.join(", ", nombresAutores);

    } catch (Exception e) {
      System.out.println("Error obteniendo autores para obra "+obra.getId() +": "+ e.getMessage());
      return "Autor no disponible";
    }
  }

  @Named("mapContenidoBasico")
  default PrestamoResponseDTO.ContenidoBasicoDTO mapContenidoBasico(Contenido contenido) {
    if (contenido == null) {
      return null;
    }

    // Mapear información de la obra si existe
    PrestamoResponseDTO.ContenidoBasicoDTO.ObraBasicaDTO obraDTO = null;
    if (contenido.getObra() != null) {
      String autoresString = mapearAutores(contenido.getObra());

      obraDTO = PrestamoResponseDTO.ContenidoBasicoDTO.ObraBasicaDTO.builder()
          .id(contenido.getObra().getId())
          .titulo(contenido.getObra().getTitulo())
          .autor(autoresString)
          .anioPublicacion(contenido.getObra().getAnioPublicacion())
          .version(contenido.getObra().getVersion())
          .build();
    }

    return PrestamoResponseDTO.ContenidoBasicoDTO.builder()
        .id(contenido.getId())
        .titulo(contenido.getObra() != null ? contenido.getObra().getTitulo() : "Título no disponible")
        .tipoContenido(contenido.getTipo().toString())
        .isbn(contenido.getIsbn() != null ? contenido.getIsbn()
            : (contenido.getObra() != null ? contenido.getObra().getIsbn() : null))
        .portadaUrl(contenido.getPortadaUrl())
        .enVenta(contenido.isEnVenta())
        .puedeSerPrestado(contenido.isPuedeSerPrestado())
        .precio(contenido.getPrecio())
        .obra(obraDTO)
        .build();
  }

  @Named("mapPerfilBasico")
  default PrestamoResponseDTO.PerfilBasicoDTO mapPerfilBasico(Perfil perfil) {
    if (perfil == null) {
      return null;
    }
    return PrestamoResponseDTO.PerfilBasicoDTO.builder()
        .id(perfil.getId())
        .nombreVisible(perfil.getNombreVisible())
        .fotoPerfil(perfil.getFotoPerfil())
        .esInfantil(perfil.getEsInfantil())
        .activo(perfil.getActivo())
        .idiomaPreferido(perfil.getIdiomaPreferido())
        .build();
  }

  // Método auxiliar para mapear tipo de contenido
  default String mapTipoContenido(Contenido contenido) {
    if (contenido == null)
      return "DESCONOCIDO";

    // Determinar el tipo basado en la clase concreta
    String className = contenido.getClass().getSimpleName();
    switch (className) {
      case "LibroFisico":
        return "LIBRO_FISICO";
      case "AudioLibro":
        return "AUDIOLIBRO";
      case "Manga":
        return "MANGA";
      case "Comic":
        return "COMIC";
      case "RevistaPeriodica":
        return "REVISTA";
      case "MaterialEducativo":
        return "MATERIAL_EDUCATIVO";
      case "ContenidoMultimedia":
        return "MULTIMEDIA";
      default:
        return className.toUpperCase();
    }
  }

}