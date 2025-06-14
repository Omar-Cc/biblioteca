package com.biblioteca.controller;

import com.biblioteca.dto.ContenidoRequestDTO;
import com.biblioteca.dto.ContenidoResponseDTO;
import com.biblioteca.dto.PerfilResponseDTO;
import com.biblioteca.dto.actividades.PrestamoRequestDTO;
import com.biblioteca.dto.actividades.PrestamoResponseDTO;
import com.biblioteca.dto.validacion.prestamos.ValidacionPrestamoResult;
import com.biblioteca.enums.EstadoPrestamo;
import com.biblioteca.enums.FormatoFisico;
import com.biblioteca.enums.TipoContenido;
import com.biblioteca.enums.TipoLicenciaDigital;
import com.biblioteca.models.acceso.Perfil;
import com.biblioteca.service.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ContenidoController {

  private final ContenidoService contenidoService;
  private final EditorialService editorialService;
  private final GeneroService generoService;
  private final ObraService obraService;
  private final PrestamoService prestamoService;
  private final PerfilService perfilService;

  // --- Endpoints para Usuarios (Catálogo público) ---

  @GetMapping("/catalogo")
  public String mostrarCatalogo(
      @RequestParam(required = false) String tituloObra,
      @RequestParam(required = false) Long editorialId,
      @RequestParam(required = false) TipoContenido tipo,
      @RequestParam(required = false) String isbn,
      @RequestParam(required = false) Long serieId,
      Model model,
      HttpSession session) {

    List<ContenidoResponseDTO> contenidos = contenidoService.obtenerCatalogoPublico(
        Optional.ofNullable(tituloObra),
        Optional.ofNullable(editorialId),
        Optional.ofNullable(tipo),
        Optional.ofNullable(isbn),
        Optional.ofNullable(serieId));

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
      Perfil perfilActual = obtenerPerfilActual(session);
      if (perfilActual != null) {
        // Verificar qué contenidos ya están prestados por este perfil
        for (ContenidoResponseDTO contenido : contenidos) {
          boolean yaPrestado = prestamoService.perfilTieneContenidoPrestado(perfilActual.getId(), contenido.getId());
          contenido.setYaPrestadoPorPerfil(yaPrestado);

          if (yaPrestado) {
            // Obtener información del préstamo activo
            Optional<PrestamoResponseDTO> prestamoActivo = prestamoService.obtenerPrestamoActivoPorPerfilYContenido(
                perfilActual.getId(), contenido.getId());
            prestamoActivo.ifPresent(prestamo -> {
              contenido.setPrestamoActivo(prestamo);
            });
          }
        }

        model.addAttribute("perfilActual", perfilActual);
      }
    }

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
  public String mostrarDetalleContenido(@PathVariable Long id, Model model, HttpSession session) {
    Optional<ContenidoResponseDTO> contenidoOpt = contenidoService.obtenerContenidoPorId(id);
    if (contenidoOpt.isPresent() && contenidoOpt.get().isActivo()) {
      ContenidoResponseDTO contenido = contenidoOpt.get();

      // Verificar estado de préstamo si está logueado
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
        Perfil perfilActual = obtenerPerfilActual(session);
        if (perfilActual != null) {
          boolean yaPrestado = prestamoService.perfilTieneContenidoPrestado(perfilActual.getId(), contenido.getId());
          contenido.setYaPrestadoPorPerfil(yaPrestado);

          if (yaPrestado) {
            Optional<PrestamoResponseDTO> prestamoActivo = prestamoService.obtenerPrestamoActivoPorPerfilYContenido(
                perfilActual.getId(), contenido.getId());
            prestamoActivo.ifPresent(prestamo -> {
              contenido.setPrestamoActivo(prestamo);
            });
          }

          // Verificar si puede realizar préstamo
          ValidacionPrestamoResult validacion = validarDisponibilidadPrestamo(perfilActual.getId(), contenido.getId());
          model.addAttribute("validacionPrestamo", validacion);
          model.addAttribute("perfilActual", perfilActual);
        }
      }

      model.addAttribute("contenido", contenido);
      model.addAttribute("activeTab", "catalogo");
      return "public/catalogo/contenido-detalle";
    }
    return "redirect:/catalogo?error=ContenidoNoEncontrado";
  }

  // ========== ENDPOINTS PARA PRÉSTAMOS ==========

  /**
   * Página para solicitar préstamo
   */
  @GetMapping("/catalogo/prestamo/{contenidoId}")
  @PreAuthorize("hasRole('LECTOR')")
  public String mostrarSolicitarPrestamo(@PathVariable Long contenidoId, Model model,
      HttpSession session, RedirectAttributes redirectAttributes) {

    Perfil perfilActual = obtenerPerfilActual(session);
    if (perfilActual == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para realizar préstamos");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      Optional<ContenidoResponseDTO> contenidoOpt = contenidoService.obtenerContenidoPorId(contenidoId);
      if (contenidoOpt.isEmpty() || !contenidoOpt.get().isActivo()) {
        redirectAttributes.addFlashAttribute("error", "Contenido no encontrado o no disponible");
        return "redirect:/catalogo";
      }

      ContenidoResponseDTO contenido = contenidoOpt.get();

      // Validar disponibilidad
      ValidacionPrestamoResult validacion = validarDisponibilidadPrestamo(perfilActual.getId(), contenidoId);

      if (!validacion.isEsValidoSafe()) {
        redirectAttributes.addFlashAttribute("error", "No se puede realizar el préstamo: " +
            String.join(", ", validacion.getErroresSafe()));
        return "redirect:/catalogo/contenido/" + contenidoId;
      }

      Long prestamosActivos = prestamoService.contarPrestamosActivosPorPerfil(perfilActual.getId());
      model.addAttribute("prestamosActivos", prestamosActivos);

      model.addAttribute("esPerfilPrincipal", perfilActual.getEsPerfilPrincipal());
      model.addAttribute("limitePrestamos", perfilActual.getLimitePrestamosDesignado());
      model.addAttribute("contenido", contenido);
      model.addAttribute("perfilActual", perfilActual);
      model.addAttribute("validacion", validacion);
      model.addAttribute("fechaPrestamo", LocalDate.now());

      return "public/catalogo/solicitar-prestamo";

    } catch (Exception e) {
      log.error("Error mostrando formulario de préstamo: {}", e.getMessage());
      redirectAttributes.addFlashAttribute("error", "Error procesando la solicitud");
      return "redirect:/catalogo";
    }
  }

  /**
   * Confirmar préstamo
   */
  @PostMapping("/catalogo/prestamo/{contenidoId}/confirmar")
  @PreAuthorize("hasRole('LECTOR')")
  public String confirmarPrestamo(@PathVariable Long contenidoId, HttpSession session,
      RedirectAttributes redirectAttributes) {

    Perfil perfilActual = obtenerPerfilActual(session);
    if (perfilActual == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      PrestamoRequestDTO prestamoRequest = PrestamoRequestDTO.builder()
          .perfilId(perfilActual.getId())
          .contenidoId(contenidoId)
          .fechaPrestamo(LocalDate.now())
          .build();

      PrestamoResponseDTO prestamo = prestamoService.crearPrestamo(prestamoRequest);

      redirectAttributes.addFlashAttribute("success",
          "¡Préstamo realizado exitosamente! Ya puede acceder al contenido.");
      redirectAttributes.addFlashAttribute("prestamoId", prestamo.getId());

      return "redirect:/mi-cuenta/prestamos/activos";

    } catch (Exception e) {
      log.error("Error confirmando préstamo: {}", e.getMessage());
      redirectAttributes.addFlashAttribute("error", "Error realizando el préstamo: " + e.getMessage());
      return "redirect:/catalogo/contenido/" + contenidoId;
    }
  }

  /**
   * Devolver préstamo desde catálogo
   */
  @PostMapping("/catalogo/prestamo/{prestamoId}/devolver")
  @PreAuthorize("hasRole('LECTOR')")
  public String devolverPrestamoDesdeCalago(@PathVariable Long prestamoId, HttpSession session,
      RedirectAttributes redirectAttributes) {

    Perfil perfilActual = obtenerPerfilActual(session);
    if (perfilActual == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      // Validar que el préstamo pertenece al perfil actual
      PrestamoResponseDTO prestamo = prestamoService.obtenerPrestamoPorId(prestamoId);
      if (!prestamo.getPerfil().getId().equals(perfilActual.getId())) {
        redirectAttributes.addFlashAttribute("error", "No tiene permisos para devolver este préstamo");
        return "redirect:/catalogo";
      }

      prestamoService.devolverPrestamo(prestamoId);

      redirectAttributes.addFlashAttribute("success", "Préstamo devuelto exitosamente");
      return "redirect:/catalogo/contenido/" + prestamo.getContenido().getId();

    } catch (Exception e) {
      log.error("Error devolviendo préstamo: {}", e.getMessage());
      redirectAttributes.addFlashAttribute("error", "Error devolviendo el préstamo: " + e.getMessage());
      return "redirect:/mi-cuenta/prestamos";
    }
  }

  /**
   * API para validar disponibilidad de préstamo
   */
  @GetMapping("/api/catalogo/validar-prestamo/{contenidoId}")
  @PreAuthorize("hasRole('LECTOR')")
  @ResponseBody
  public ResponseEntity<ValidacionPrestamoResult> validarPrestamo(@PathVariable Long contenidoId,
      HttpSession session) {

    Perfil perfilActual = obtenerPerfilActual(session);
    if (perfilActual == null) {
      ValidacionPrestamoResult error = ValidacionPrestamoResult.error("ERROR", "Debe seleccionar un perfil");
      return ResponseEntity.badRequest().body(error);
    }

    try {
      ValidacionPrestamoResult validacion = validarDisponibilidadPrestamo(perfilActual.getId(), contenidoId);
      return ResponseEntity.ok(validacion);
    } catch (Exception e) {
      log.error("Error validando préstamo: {}", e.getMessage());
      ValidacionPrestamoResult error = ValidacionPrestamoResult.error("ERROR", "Error validando disponibilidad");
      return ResponseEntity.internalServerError().body(error);
    }
  }

  // ========== MÉTODOS AUXILIARES ==========

  /**
   * Obtener perfil actual desde la sesión
   */
  private Perfil obtenerPerfilActual(HttpSession session) {
    Long perfilActivoId = (Long) session.getAttribute("perfilActivoId");
    if (perfilActivoId == null) {
      return null;
    }
    return perfilService.obtenerEntidadPerfilPorId(perfilActivoId).orElse(null);
  }

  /**
   * Validar disponibilidad de préstamo
   */
  private ValidacionPrestamoResult validarDisponibilidadPrestamo(Long perfilId, Long contenidoId) {
    ValidacionPrestamoResult resultado = ValidacionPrestamoResult.exito("VALIDAR");

    try {
      // Verificar si el contenido está disponible para préstamo
      Optional<ContenidoResponseDTO> contenidoOpt = contenidoService.obtenerContenidoPorId(contenidoId);
      if (contenidoOpt.isEmpty() || !contenidoOpt.get().isPuedeSerPrestado()) {
        resultado.agregarError("Este contenido no está disponible para préstamo");
        return resultado;
      }

      // Verificar si ya tiene este contenido prestado
      if (prestamoService.perfilTieneContenidoPrestado(perfilId, contenidoId)) {
        resultado.agregarError("Ya tiene este contenido en préstamo activo");
        return resultado;
      }

      // Verificar límites del perfil
      if (!prestamoService.puedeRealizarPrestamo(perfilId)) {
        resultado.agregarError("Ha alcanzado el límite de préstamos simultáneos");
        resultado.agregarAdvertencia("Considere devolver algún contenido o mejorar su plan");
      }

    } catch (Exception e) {
      log.error("Error validando préstamo: {}", e.getMessage());
      resultado.agregarError("Error validando disponibilidad");
    }

    return resultado;
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
      requestDTO.setSerie(contenidoResponse.getSerie());
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