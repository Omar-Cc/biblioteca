package com.biblioteca.controller;

import com.biblioteca.dto.ContenidoRequestDTO;
import com.biblioteca.dto.ContenidoResponseDTO;
import com.biblioteca.enums.FormatoFisico;
import com.biblioteca.enums.TipoContenido;
import com.biblioteca.enums.TipoLicenciaDigital;
import com.biblioteca.service.ContenidoService;
import com.biblioteca.service.EditorialService;
import com.biblioteca.service.GeneroService;
import com.biblioteca.service.ObraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ContenidoController {

  private final ContenidoService contenidoService;
  private final EditorialService editorialService;
  private final GeneroService generoService;
  private final ObraService obraService;

  // --- Endpoints para Usuarios (Catálogo público) ---

  @GetMapping("/catalogo")
  public String mostrarCatalogo(
      @RequestParam(required = false) String tituloObra,
      @RequestParam(required = false) Long editorialId,
      @RequestParam(required = false) TipoContenido tipo,
      @RequestParam(required = false) String isbn,
      @RequestParam(required = false) Long serieId,
      Model model) {

    List<ContenidoResponseDTO> contenidos = contenidoService.obtenerCatalogoPublico(
        Optional.ofNullable(tituloObra),
        Optional.ofNullable(editorialId),
        Optional.ofNullable(tipo),
        Optional.ofNullable(isbn),
        Optional.ofNullable(serieId));

    model.addAttribute("contenidos", contenidos);
    model.addAttribute("editoriales", editorialService.obtenerTodasLasEditoriales());
    model.addAttribute("generos", generoService.obtenerTodosLosGeneros());
    model.addAttribute("tiposContenido", TipoContenido.values());

    model.addAttribute("filtroTituloObra", tituloObra);
    model.addAttribute("filtroEditorialId", editorialId);
    model.addAttribute("filtroTipo", tipo);
    model.addAttribute("filtroIsbn", isbn);
    model.addAttribute("filtroSerieId", serieId);

    model.addAttribute("activeTab", "catalogo");

    return "public/catalogo/catalogo";
  }

  @GetMapping("/catalogo/contenido/{id}")
  public String mostrarDetalleContenido(@PathVariable Long id, Model model) {
    Optional<ContenidoResponseDTO> contenidoOpt = contenidoService.obtenerContenidoPorId(id);
    if (contenidoOpt.isPresent() && contenidoOpt.get().isActivo()) {
      model.addAttribute("contenido", contenidoOpt.get());
      model.addAttribute("activeTab", "catalogo");
      return "public/catalogo/contenido-detalle";
    }
    return "redirect:/catalogo?error=ContenidoNoEncontrado";
  }

  // --- Endpoints para Administración ---

  @GetMapping("/admin/contenidos")
  public String mostrarCatalogoAdmin(
      @RequestParam(required = false) String tituloObra,
      @RequestParam(required = false) Long editorialId,
      @RequestParam(required = false) TipoContenido tipo,
      @RequestParam(required = false) String isbn,
      @RequestParam(required = false) Long serieId,
      @RequestParam(required = false) Boolean soloActivos,
      Model model) {

    List<ContenidoResponseDTO> contenidos = contenidoService.obtenerContenidosAdministracion(
        Optional.ofNullable(tituloObra),
        Optional.ofNullable(editorialId),
        Optional.ofNullable(tipo),
        Optional.ofNullable(isbn),
        Optional.ofNullable(serieId),
        Optional.ofNullable(soloActivos));

    model.addAttribute("contenidos", contenidos);
    model.addAttribute("editoriales", editorialService.obtenerTodasLasEditoriales());
    model.addAttribute("generos", generoService.obtenerTodosLosGeneros());
    model.addAttribute("tiposContenido", TipoContenido.values());

    model.addAttribute("filtroTituloObra", tituloObra);
    model.addAttribute("filtroEditorialId", editorialId);
    model.addAttribute("filtroTipo", tipo);
    model.addAttribute("filtroIsbn", isbn);
    model.addAttribute("filtroSerieId", serieId);
    model.addAttribute("filtroSoloActivos", soloActivos);
    model.addAttribute("activeTab", "contenidos");

    return "admin/contenidos/lista";
  }

  @GetMapping("/admin/contenidos/nuevo")
  public String mostrarFormularioNuevoContenido(Model model) {
    model.addAttribute("contenidoRequestDTO", ContenidoRequestDTO.builder().build());
    model.addAttribute("activeTab", "contenidos");
    cargarDatosFormulario(model);
    return "admin/contenidos/formulario";
  }

  @PostMapping("/admin/contenidos/guardar")
  public String guardarContenido(@Valid @ModelAttribute("contenidoRequestDTO") ContenidoRequestDTO contenidoRequestDTO,
      BindingResult result, Model model, RedirectAttributes redirectAttributes) {

    if (result.hasErrors()) {
      cargarDatosFormulario(model);
      return "admin/contenidos/formulario";
    }

    try {
      contenidoService.agregarContenido(contenidoRequestDTO);
      redirectAttributes.addFlashAttribute("successMessage", "Contenido creado exitosamente.");
      return "redirect:/admin/contenidos";
    } catch (IllegalArgumentException e) {
      model.addAttribute("errorMessage", e.getMessage());
      cargarDatosFormulario(model);
      return "admin/contenidos/formulario";
    }
  }

  @GetMapping("/admin/contenidos/editar/{id}")
  public String mostrarFormularioEditarContenido(@PathVariable Long id, Model model,
      RedirectAttributes redirectAttributes) {
    Optional<ContenidoResponseDTO> contenidoOpt = contenidoService.obtenerContenidoPorId(id);

    if (contenidoOpt.isEmpty()) {
      redirectAttributes.addFlashAttribute("errorMessage", "Contenido no encontrado.");
      return "redirect:/admin/contenidos";
    }

    ContenidoResponseDTO contenidoResponse = contenidoOpt.get();

    // Construir el DTO para edición a partir de la respuesta
    ContenidoRequestDTO requestDTO = ContenidoRequestDTO.builder()
        .obraId(contenidoResponse.getObra().getId())
        .editorialId(contenidoResponse.getEditorial().getId())
        .portadaUrl(contenidoResponse.getPortadaUrl())
        .sinopsis(contenidoResponse.getSinopsis())
        .precio(contenidoResponse.getPrecio())
        .enVenta(contenidoResponse.isEnVenta())
        .tipo(contenidoResponse.getTipo())
        .puedeSerPrestado(contenidoResponse.isPuedeSerPrestado())
        .isbn(contenidoResponse.getIsbn())

        // Propiedades específicas por tipo
        .stockDisponible(contenidoResponse.getStockDisponible())
        .minStock(contenidoResponse.getMinStock())
        .ubicacionFisica(contenidoResponse.getUbicacionFisica())
        .formatoFisico(contenidoResponse.getFormatoFisico())
        .tamanioArchivo(contenidoResponse.getTamanioArchivo())
        .formatoDigital(contenidoResponse.getFormatoDigital())
        .permiteDescarga(contenidoResponse.getPermiteDescarga())
        .tipoLicencia(contenidoResponse.getTipoLicencia())
        .licencias(contenidoResponse.getLicencias())
        .paginas(contenidoResponse.getPaginas())
        .ilustrador(contenidoResponse.getIlustrador())
        .volumen(contenidoResponse.getVolumen())
        .sentidoLectura(contenidoResponse.getSentidoLectura())
        .colorido(contenidoResponse.getColorido())
        .periodicidad(contenidoResponse.getPeriodicidad())
        .edicion(contenidoResponse.getEdicion())
        .issn(contenidoResponse.getIssn())
        .duracionAudioLibro(contenidoResponse.getDuracionAudioLibro())
        .narrador(contenidoResponse.getNarrador())
        .calidadAudio(contenidoResponse.getCalidadAudio())
        .nivelEducativo(contenidoResponse.getNivelEducativo())
        .asignatura(contenidoResponse.getAsignatura())
        .recursosEducativos(contenidoResponse.getRecursosEducativos())
        .duracionMultimedia(contenidoResponse.getDuracionMultimedia())
        .calidadMultimedia(contenidoResponse.getCalidadMultimedia())
        .requisitosReproduccion(contenidoResponse.getRequisitosReproduccion())
        .build();

    if (contenidoResponse.getSerie() != null) {
      requestDTO.setSerieId(contenidoResponse.getSerie().getId());
      requestDTO.setOrdenEnSerie(contenidoResponse.getSerie().getOrdenEnSerie());
    }

    model.addAttribute("contenidoRequestDTO", requestDTO);
    model.addAttribute("contenidoId", id);
    model.addAttribute("activeTab", "contenidos");
    cargarDatosFormulario(model);

    return "admin/contenidos/formulario";
  }

  @PostMapping("/admin/contenidos/actualizar/{id}")
  public String actualizarContenido(@PathVariable Long id,
      @Valid @ModelAttribute("contenidoRequestDTO") ContenidoRequestDTO contenidoRequestDTO,
      BindingResult result, Model model, RedirectAttributes redirectAttributes) {

    if (result.hasErrors()) {
      model.addAttribute("contenidoId", id);
      cargarDatosFormulario(model);
      return "admin/contenidos/formulario";
    }

    try {
      contenidoService.actualizarContenido(id, contenidoRequestDTO);
      redirectAttributes.addFlashAttribute("successMessage", "Contenido actualizado exitosamente.");
      return "redirect:/admin/contenidos";
    } catch (IllegalArgumentException e) {
      model.addAttribute("errorMessage", e.getMessage());
      model.addAttribute("contenidoId", id);
      cargarDatosFormulario(model);
      return "admin/contenidos/formulario";
    }
  }

  @PostMapping("/admin/contenidos/cambiar-estado/{id}")
  public String cambiarEstadoContenido(@PathVariable Long id, @RequestParam boolean nuevoEstado,
      RedirectAttributes redirectAttributes) {

    boolean actualizado = contenidoService.cambiarEstadoContenido(id, nuevoEstado);

    if (actualizado) {
      String accion = nuevoEstado ? "activado" : "desactivado";
      redirectAttributes.addFlashAttribute("successMessage", "Contenido " + accion + " exitosamente.");
    } else {
      redirectAttributes.addFlashAttribute("errorMessage", "No se pudo cambiar el estado del contenido.");
    }

    return "redirect:/admin/contenidos";
  }

  private void cargarDatosFormulario(Model model) {
    model.addAttribute("obras", obraService.obtenerTodasLasObras());
    model.addAttribute("editoriales", editorialService.obtenerTodasLasEditoriales());
    model.addAttribute("tiposContenido", TipoContenido.values());
    model.addAttribute("formatosFisicos", FormatoFisico.values());
    model.addAttribute("tiposLicencia", TipoLicenciaDigital.values());
  }
}