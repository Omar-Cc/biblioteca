package com.biblioteca.mapper;

import com.biblioteca.dto.ContenidoRequestDTO;
import com.biblioteca.dto.ContenidoResponseDTO;
import com.biblioteca.models.contenido.AudioLibro;
import com.biblioteca.models.contenido.Comic;
import com.biblioteca.models.contenido.Contenido;
import com.biblioteca.models.contenido.ContenidoDigital;
import com.biblioteca.models.contenido.ContenidoFisico;
import com.biblioteca.models.contenido.ContenidoMultimedia;
import com.biblioteca.models.contenido.LibroFisico;
import com.biblioteca.models.contenido.Manga;
import com.biblioteca.models.contenido.MaterialEducativo;
import com.biblioteca.models.contenido.PublicacionIlustradaFisica;
import com.biblioteca.models.contenido.RevistaPeriodica;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.time.Duration;
import java.util.List;

@Mapper(componentModel = "spring", uses = { ObraMapper.class, EditorialMapper.class })
public interface ContenidoMapper {

  @Mappings({
      @Mapping(source = "obra", target = "obra"),
      @Mapping(source = "editorial", target = "editorial")
  })
  ContenidoResponseDTO toResponseDTO(Contenido contenido);

  List<ContenidoResponseDTO> toResponseDTOs(List<Contenido> contenidos);

  // Métodos de condición para mapeo selectivo
  @Named("isLibroFisico")
  default boolean isLibroFisico(Contenido c) {
    return c instanceof LibroFisico;
  }

  @Named("isPublicacionIlustrada")
  default boolean isPublicacionIlustrada(Contenido c) {
    return c instanceof PublicacionIlustradaFisica;
  }

  @Named("isManga")
  default boolean isManga(Contenido c) {
    return c instanceof Manga;
  }

  @Named("isComic")
  default boolean isComic(Contenido c) {
    return c instanceof Comic;
  }

  @Named("isRevistaPeriodica")
  default boolean isRevistaPeriodica(Contenido c) {
    return c instanceof RevistaPeriodica;
  }

  @Named("isAudioLibro")
  default boolean isAudioLibro(Contenido c) {
    return c instanceof AudioLibro;
  }

  @Named("isMaterialEducativo")
  default boolean isMaterialEducativo(Contenido c) {
    return c instanceof MaterialEducativo;
  }

  @Named("isContenidoMultimedia")
  default boolean isContenidoMultimedia(Contenido c) {
    return c instanceof ContenidoMultimedia;
  }

  @Named("isContenidoMultimediaAndNotAudioLibro")
  default boolean isContenidoMultimediaAndNotAudioLibro(Contenido c) {
    return c instanceof ContenidoMultimedia && !(c instanceof AudioLibro);
  }

  @Mappings({
      @Mapping(target = "id", ignore = true),
      @Mapping(target = "obra", ignore = true), // Se asignará en el servicio
      @Mapping(target = "editorial", ignore = true), // Se asignará en el servicio
      @Mapping(target = "activo", ignore = true),
      @Mapping(target = "fechaCreacion", ignore = true),
      @Mapping(target = "tipo", source = "tipo")
  })
  void updateContenidoFromDto(ContenidoRequestDTO dto, @MappingTarget Contenido entity);

  @Mappings({
      // Mapeos de ContenidoFisico
      @Mapping(target = "stockDisponible", source = "stockDisponible"),
      @Mapping(target = "minStock", source = "minStock"),
      @Mapping(target = "ubicacionFisica", source = "ubicacionFisica"),
      @Mapping(target = "formatoFisico", source = "formatoFisico"),
      // Mapeos de LibroFisico
      @Mapping(target = "paginas", source = "paginas")
  })
  void updateLibroFisicoFromDto(ContenidoRequestDTO dto, @MappingTarget LibroFisico entity);

  @Mappings({
      // Mapeos de ContenidoFisico y PublicacionIlustradaFisica
      @Mapping(target = "stockDisponible", source = "stockDisponible"),
      @Mapping(target = "minStock", source = "minStock"),
      @Mapping(target = "ubicacionFisica", source = "ubicacionFisica"),
      @Mapping(target = "formatoFisico", source = "formatoFisico"),
      @Mapping(target = "ilustrador", source = "ilustrador"),
      @Mapping(target = "volumen", source = "volumen"),
      // Mapeos de Manga
      @Mapping(target = "sentidoLectura", source = "sentidoLectura")
  })
  void updateMangaFromDto(ContenidoRequestDTO dto, @MappingTarget Manga entity);

  @Mappings({
      // Mapeos de ContenidoFisico y PublicacionIlustradaFisica
      @Mapping(target = "stockDisponible", source = "stockDisponible"),
      @Mapping(target = "minStock", source = "minStock"),
      @Mapping(target = "ubicacionFisica", source = "ubicacionFisica"),
      @Mapping(target = "formatoFisico", source = "formatoFisico"),
      @Mapping(target = "ilustrador", source = "ilustrador"),
      @Mapping(target = "volumen", source = "volumen"),
      // Mapeos de Comic
      @Mapping(target = "colorido", source = "colorido")
  })
  void updateComicFromDto(ContenidoRequestDTO dto, @MappingTarget Comic entity);

  @Mappings({
      // Mapeos de ContenidoFisico
      @Mapping(target = "stockDisponible", source = "stockDisponible"),
      @Mapping(target = "minStock", source = "minStock"),
      @Mapping(target = "ubicacionFisica", source = "ubicacionFisica"),
      @Mapping(target = "formatoFisico", source = "formatoFisico"),
      // Mapeos de RevistaPeriodica
      @Mapping(target = "periodicidad", source = "periodicidad"),
      @Mapping(target = "edicion", source = "edicion"),
      @Mapping(target = "issn", source = "issn")
  })
  void updateRevistaPeriodicaFromDto(ContenidoRequestDTO dto, @MappingTarget RevistaPeriodica entity);

  @Mappings({
      // Mapeos de ContenidoDigital
      @Mapping(target = "tamanioArchivo", source = "tamanioArchivo"),
      @Mapping(target = "formato", source = "formatoDigital"),
      @Mapping(target = "permiteDescarga", source = "permiteDescarga"),
      @Mapping(target = "tipoLicencia", source = "tipoLicencia"),
      @Mapping(target = "licencias", source = "licencias"),
      // Mapeos de AudioLibro
      @Mapping(source = "duracionAudioLibro", target = "duracion", qualifiedByName = "stringToDuration"),
      @Mapping(target = "narrador", source = "narrador"),
      @Mapping(target = "calidad", source = "calidadAudio")
  })
  void updateAudioLibroFromDto(ContenidoRequestDTO dto, @MappingTarget AudioLibro entity);

  // Agregar métodos update...FromDto para MaterialEducativo y ContenidoMultimedia
  @Mappings({
      // Mapeos de ContenidoDigital
      @Mapping(target = "tamanioArchivo", source = "tamanioArchivo"),
      @Mapping(target = "formato", source = "formatoDigital"),
      @Mapping(target = "permiteDescarga", source = "permiteDescarga"),
      @Mapping(target = "tipoLicencia", source = "tipoLicencia"),
      @Mapping(target = "licencias", source = "licencias"),
      // Mapeos de MaterialEducativo
      @Mapping(target = "nivelEducativo", source = "nivelEducativo"),
      @Mapping(target = "asignatura", source = "asignatura"),
      @Mapping(target = "recursos", source = "recursosEducativos")
  })
  void updateMaterialEducativoFromDto(ContenidoRequestDTO dto, @MappingTarget MaterialEducativo entity);

  @Mappings({
      // Mapeos de ContenidoDigital
      @Mapping(target = "tamanioArchivo", source = "tamanioArchivo"),
      @Mapping(target = "formato", source = "formatoDigital"),
      @Mapping(target = "permiteDescarga", source = "permiteDescarga"),
      @Mapping(target = "tipoLicencia", source = "tipoLicencia"),
      @Mapping(target = "licencias", source = "licencias"),
      // Mapeos de ContenidoMultimedia
      @Mapping(source = "duracionMultimedia", target = "duracion", qualifiedByName = "stringToDuration"),
      @Mapping(source = "calidadMultimedia", target = "calidad"),
      @Mapping(target = "requisitosReproduccion", source = "requisitosReproduccion")
  })
  void updateContenidoMultimediaFromDto(ContenidoRequestDTO dto, @MappingTarget ContenidoMultimedia entity);

  // Conversores para Duration
  @Named("durationToString")
  default String durationToString(Duration duration) {
    return duration == null ? null : duration.toString();
  }

  @Named("stringToDuration")
  default Duration stringToDuration(String durationString) {
    return durationString == null || durationString.isEmpty() ? null : Duration.parse(durationString);
  }

  @AfterMapping
  default void mapSubclassFieldsToResponseDTO(Contenido contenido,
      @MappingTarget ContenidoResponseDTO.ContenidoResponseDTOBuilder dtoBuilder) {
    
    if (contenido instanceof ContenidoFisico) {
      ContenidoFisico cf = (ContenidoFisico) contenido;
      dtoBuilder.stockDisponible(cf.getStockDisponible());
      dtoBuilder.minStock(cf.getMinStock());
      dtoBuilder.ubicacionFisica(cf.getUbicacionFisica());
      dtoBuilder.formatoFisico(cf.getFormatoFisico());
    }
    if (contenido instanceof ContenidoDigital) {
      ContenidoDigital cd = (ContenidoDigital) contenido;
      dtoBuilder.tamanioArchivo(cd.getTamanioArchivo());
      dtoBuilder.formatoDigital(cd.getFormato()); // Mapeo explícito de nombre
      dtoBuilder.permiteDescarga(cd.isPermiteDescarga());
      dtoBuilder.tipoLicencia(cd.getTipoLicencia());
      dtoBuilder.licencias(cd.getLicencias());
    }
    if (contenido instanceof LibroFisico) {
      dtoBuilder.paginas(((LibroFisico) contenido).getPaginas());
    }
    if (contenido instanceof PublicacionIlustradaFisica) {
      PublicacionIlustradaFisica pif = (PublicacionIlustradaFisica) contenido;
      dtoBuilder.ilustrador(pif.getIlustrador());
      dtoBuilder.volumen(pif.getVolumen());
    }
    if (contenido instanceof Manga) {
      dtoBuilder.sentidoLectura(((Manga) contenido).getSentidoLectura());
    }
    if (contenido instanceof Comic) {
      dtoBuilder.colorido(((Comic) contenido).isColorido());
    }
    if (contenido instanceof RevistaPeriodica) {
      RevistaPeriodica rp = (RevistaPeriodica) contenido;
      dtoBuilder.periodicidad(rp.getPeriodicidad());
      dtoBuilder.edicion(rp.getEdicion());
      dtoBuilder.issn(rp.getIssn());
    }
    if (contenido instanceof AudioLibro) {
      AudioLibro al = (AudioLibro) contenido;
      dtoBuilder.duracionAudioLibro(durationToString(al.getDuracion()));
      dtoBuilder.narrador(al.getNarrador());
      dtoBuilder.calidadAudio(al.getCalidad());
    }
    if (contenido instanceof MaterialEducativo) {
      MaterialEducativo me = (MaterialEducativo) contenido;
      dtoBuilder.nivelEducativo(me.getNivelEducativo());
      dtoBuilder.asignatura(me.getAsignatura());
      dtoBuilder.recursosEducativos(me.getRecursos()); // Mapeo explícito de nombre
    }
    if (contenido instanceof ContenidoMultimedia) {
      ContenidoMultimedia cm = (ContenidoMultimedia) contenido;
      dtoBuilder.duracionMultimedia(durationToString(cm.getDuracion()));
      dtoBuilder.calidadMultimedia(cm.getCalidad());
      dtoBuilder.requisitosReproduccion(cm.getRequisitosReproduccion());
    }
  }
}