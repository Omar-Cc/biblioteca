package com.biblioteca.controller.cuenta;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca.dto.actividades.PrestamoRequestDTO;
import com.biblioteca.dto.actividades.PrestamoResponseDTO;
import com.biblioteca.dto.validacion.prestamos.ValidacionPrestamoResult;
import com.biblioteca.enums.EstadoPrestamo;
import com.biblioteca.models.acceso.Perfil;
import com.biblioteca.service.PerfilService;
import com.biblioteca.service.PrestamoService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/mi-cuenta/prestamos")
@PreAuthorize("hasRole('LECTOR')")
public class MiPrestamoController {

  private final PrestamoService prestamoService;
  private final PerfilService perfilService;

  // ========== VISTAS PRINCIPALES ==========

  /**
   * Página principal de mis préstamos
   */
  @GetMapping
  public String misPrestamos(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "fechaPrestamo") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir,
      @RequestParam(required = false) String estado,
      Model model,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    // Obtener el perfil activo desde la sesión
    Perfil perfilActual = obtenerPerfilActual(session);

    if (perfilActual == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para ver los préstamos");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      // Configurar paginación
      Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
      Pageable pageable = PageRequest.of(page, size, sort);

      // Obtener préstamos del perfil activo
      Page<PrestamoResponseDTO> prestamos;
      if (estado != null && !estado.isEmpty()) {
        EstadoPrestamo estadoEnum = EstadoPrestamo.valueOf(estado.toUpperCase());
        // Usar método que filtra por perfil Y estado directamente en la BD
        prestamos = prestamoService.obtenerPrestamosPorPerfilYEstado(perfilActual.getId(), estadoEnum, pageable);
      } else {
        prestamos = prestamoService.obtenerPrestamosPorPerfil(perfilActual.getId(), pageable);
      }

      // Datos para la vista
      model.addAttribute("prestamos", prestamos);
      model.addAttribute("estadosFiltro", EstadoPrestamo.values());
      model.addAttribute("estadoSeleccionado", estado);
      model.addAttribute("perfilActual", perfilActual);
      model.addAttribute("currentPage", page);
      model.addAttribute("sortBy", sortBy);
      model.addAttribute("sortDir", sortDir);

      // Estadísticas del perfil actual
      agregarEstadisticasPerfil(model, perfilActual);

      return "mi-cuenta/prestamos/mis-prestamos";

    } catch (Exception e) {
      log.error("Error cargando préstamos para perfil {}: {}", perfilActual.getId(), e.getMessage());
      redirectAttributes.addFlashAttribute("error", "Error cargando los préstamos");
      return "redirect:/mi-cuenta/prestamos";
    }
  }

  /**
   * Vista de préstamos activos
   */
  @GetMapping("/activos")
  public String prestamosActivos(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    Perfil perfilActual = obtenerPerfilActual(session);

    if (perfilActual == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para ver los préstamos activos");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      List<PrestamoResponseDTO> prestamosActivos = prestamoService
          .obtenerPrestamosActivosPorPerfil(perfilActual.getId());
      Long totalActivos = prestamoService.contarPrestamosActivosPorPerfil(perfilActual.getId());

      model.addAttribute("prestamosActivos", prestamosActivos);
      model.addAttribute("totalActivos", totalActivos);
      model.addAttribute("perfilActual", perfilActual);
      model.addAttribute("limitePrestamos", perfilActual.getLimitePrestamosDesignado());
      model.addAttribute("prestamosDisponibles",
          Math.max(0, perfilActual.getLimitePrestamosDesignado() - totalActivos));

      // Verificar si está cerca del límite
      double porcentajeUso = totalActivos > 0 ? (totalActivos * 100.0 / perfilActual.getLimitePrestamosDesignado()) : 0;
      model.addAttribute("porcentajeUso", porcentajeUso);
      model.addAttribute("cercaDelLimite", porcentajeUso >= 80);

      return "mi-cuenta/prestamos/prestamos-activos";

    } catch (Exception e) {
      log.error("Error cargando préstamos activos para perfil {}: {}", perfilActual.getId(), e.getMessage());
      redirectAttributes.addFlashAttribute("error", "Error cargando préstamos activos");
      return "redirect:/mi-cuenta/prestamos";
    }
  }

  /**
   * Vista de historial de préstamos
   */
  @GetMapping("/historial")
  public String historialPrestamos(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "15") int size,
      Model model,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    Perfil perfilActual = obtenerPerfilActual(session);

    if (perfilActual == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para ver el historial");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      Pageable pageable = PageRequest.of(page, size,
          Sort.by(Sort.Direction.DESC, "fechaPrestamo"));

      Page<PrestamoResponseDTO> historial = prestamoService.obtenerHistorialPrestamosPerfil(perfilActual.getId(),
          pageable);

      model.addAttribute("historial", historial);
      model.addAttribute("perfilActual", perfilActual);
      model.addAttribute("currentPage", page);

      return "mi-cuenta/prestamos/historial";

    } catch (Exception e) {
      log.error("Error cargando historial para perfil {}: {}", perfilActual.getId(), e.getMessage());
      redirectAttributes.addFlashAttribute("error", "Error cargando historial");
      return "redirect:/mi-cuenta/prestamos";
    }
  }

  // ========== OPERACIONES DE PRÉSTAMO ==========

  /**
   * Realizar nuevo préstamo
   */
  @PostMapping("/crear")
  @ResponseBody
  public ResponseEntity<?> crearPrestamo(
      @Valid @RequestBody PrestamoRequestDTO prestamoRequest,
      HttpSession session) {

    Perfil perfilActual = obtenerPerfilActual(session);

    if (perfilActual == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("error", "Debe seleccionar un perfil"));
    }

    try {
      // Forzar el perfil actual en la request
      prestamoRequest.setPerfilId(perfilActual.getId());

      // Validar disponibilidad antes del préstamo
      ValidacionPrestamoResult validacion = validarDisponibilidadPrestamo(prestamoRequest);
      if (!validacion.isEsValidoSafe()) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "No se puede realizar el préstamo",
            "detalles", validacion.getErroresSafe(),
            "validacion", validacion));
      }

      PrestamoResponseDTO prestamo = prestamoService.crearPrestamo(prestamoRequest);
      log.info("Préstamo creado exitosamente: {} para perfil: {}", prestamo.getId(), perfilActual.getId());

      return ResponseEntity.ok(Map.of(
          "success", true,
          "mensaje", "Préstamo realizado exitosamente",
          "prestamo", prestamo));

    } catch (Exception e) {
      log.error("Error creando préstamo para perfil {}: {}", perfilActual.getId(), e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", e.getMessage()));
    }
  }

  /**
   * Devolver préstamo
   */
  @PostMapping("/{prestamoId}/devolver")
  public String devolverPrestamo(@PathVariable Long prestamoId,
      HttpSession session,
      RedirectAttributes redirectAttributes) {
    Perfil perfilActual = obtenerPerfilActual(session);

    if (perfilActual == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      // Validar que el préstamo pertenece al perfil actual
      if (!prestamoPertenecePerfil(prestamoId, perfilActual.getId())) {
        redirectAttributes.addFlashAttribute("error", "No tiene permisos para devolver este préstamo");
        return "redirect:/mi-cuenta/prestamos/activos";
      }

      prestamoService.devolverPrestamo(prestamoId);
      log.info("Préstamo devuelto: {} por perfil: {}", prestamoId, perfilActual.getId());

      redirectAttributes.addFlashAttribute("success", "Préstamo devuelto exitosamente");
      return "redirect:/mi-cuenta/prestamos/activos";

    } catch (Exception e) {
      log.error("Error devolviendo préstamo {}: {}", prestamoId, e.getMessage());
      redirectAttributes.addFlashAttribute("error", "Error devolviendo el préstamo: " + e.getMessage());
      return "redirect:/mi-cuenta/prestamos/activos";
    }
  }

  /**
   * Renovar préstamo
   */
  @PostMapping("/{prestamoId}/renovar")
  public String renovarPrestamo(@PathVariable Long prestamoId,
      @RequestParam(defaultValue = "14") Integer diasExtension,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    Perfil perfilActual = obtenerPerfilActual(session);

    if (perfilActual == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      if (!prestamoPertenecePerfil(prestamoId, perfilActual.getId())) {
        redirectAttributes.addFlashAttribute("error", "No tiene permisos para renovar este préstamo");
        return "redirect:/mi-cuenta/prestamos/activos";
      }

      prestamoService.renovarPrestamo(prestamoId, diasExtension);
      log.info("Préstamo renovado: {} por perfil: {}", prestamoId, perfilActual.getId());

      redirectAttributes.addFlashAttribute("success",
          "Préstamo renovado exitosamente por " + diasExtension + " días más");
      return "redirect:/mi-cuenta/prestamos/activos";

    } catch (Exception e) {
      log.error("Error renovando préstamo {}: {}", prestamoId, e.getMessage());
      redirectAttributes.addFlashAttribute("error", "Error renovando el préstamo: " + e.getMessage());
      return "redirect:/mi-cuenta/prestamos/activos";
    }
  }

  // ========== CONSULTAS Y VALIDACIONES ==========

  /**
   * Validar disponibilidad para préstamo
   */
  @GetMapping("/validar-disponibilidad")
  @ResponseBody
  public ResponseEntity<ValidacionPrestamoResult> validarDisponibilidadPrestamo(
      @RequestParam Long contenidoId,
      HttpSession session) {

    Perfil perfilActual = obtenerPerfilActual(session);

    if (perfilActual == null) {
      ValidacionPrestamoResult error = ValidacionPrestamoResult.error("VALIDACION",
          "Debe seleccionar un perfil");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    try {
      PrestamoRequestDTO request = PrestamoRequestDTO.builder()
          .perfilId(perfilActual.getId())
          .contenidoId(contenidoId)
          .fechaPrestamo(LocalDate.now())
          .build();

      ValidacionPrestamoResult validacion = validarDisponibilidadPrestamo(request);
      return ResponseEntity.ok(validacion);

    } catch (Exception e) {
      log.error("Error validando disponibilidad: {}", e.getMessage());
      ValidacionPrestamoResult error = ValidacionPrestamoResult.error("ERROR",
          "Error validando disponibilidad");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Obtener límites de préstamo del perfil actual
   */
  @GetMapping("/limites")
  @ResponseBody
  public ResponseEntity<?> obtenerLimitesPerfilActual(HttpSession session) {
    Perfil perfilActual = obtenerPerfilActual(session);

    if (perfilActual == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("error", "Debe seleccionar un perfil"));
    }

    try {
      Long prestamosActivos = prestamoService.contarPrestamosActivosPorPerfil(perfilActual.getId());
      Integer limitePrestamos = perfilActual.getLimitePrestamosDesignado();

      Map<String, Object> respuesta = new HashMap<>();
      respuesta.put("perfilId", perfilActual.getId());
      respuesta.put("nombrePerfil", perfilActual.getNombreVisible());
      respuesta.put("prestamosActivos", prestamosActivos);
      respuesta.put("limitePrestamos", limitePrestamos);
      respuesta.put("prestamosDisponibles", Math.max(0, limitePrestamos - prestamosActivos));
      respuesta.put("porcentajeUso", limitePrestamos > 0 ? (prestamosActivos * 100.0 / limitePrestamos) : 0);
      respuesta.put("cercaDelLimite", limitePrestamos > 0 && (prestamosActivos * 100.0 / limitePrestamos) >= 80);

      return ResponseEntity.ok(respuesta);

    } catch (Exception e) {
      log.error("Error obteniendo límites del perfil {}: {}", perfilActual.getId(), e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Error obteniendo información"));
    }
  }

  /**
   * Buscar préstamos del perfil actual
   */
  @GetMapping("/buscar")
  @ResponseBody
  public ResponseEntity<Page<PrestamoResponseDTO>> buscarPrestamos(
      @RequestParam(required = false) String titulo,
      @RequestParam(required = false) String estado,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      HttpSession session) {

    Perfil perfilActual = obtenerPerfilActual(session);

    if (perfilActual == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    try {
      Pageable pageable = PageRequest.of(page, size,
          Sort.by(Sort.Direction.DESC, "fechaPrestamo"));

      EstadoPrestamo estadoEnum = null;
      if (estado != null && !estado.isEmpty()) {
        try {
          estadoEnum = EstadoPrestamo.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
          log.warn("Estado de préstamo inválido: {}", estado);
        }
      }

      Page<PrestamoResponseDTO> resultados = prestamoService.buscarPrestamosConFiltros(
          perfilActual.getId(), null, estadoEnum, null, null, null, pageable);

      return ResponseEntity.ok(resultados);

    } catch (Exception e) {
      log.error("Error buscando préstamos: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  // ========== MÉTODOS AUXILIARES ==========

  /**
   * Obtener el perfil actual desde la sesión (igual que en MiFacturaController)
   */
  private Perfil obtenerPerfilActual(HttpSession session) {
    // Obtener el ID del perfil activo desde la sesión
    Long perfilActivoId = (Long) session.getAttribute("perfilActivoId");

    if (perfilActivoId == null) {
      return null;
    }

    // Obtener el perfil desde la base de datos usando el ID
    return perfilService.obtenerEntidadPerfilPorId(perfilActivoId)
        .orElse(null);
  }

  /**
   * Verificar que un préstamo pertenece al perfil actual (igual que
   * facturaPertenecePerfil)
   */
  private boolean prestamoPertenecePerfil(Long prestamoId, Long perfilId) {
    try {
      PrestamoResponseDTO prestamo = prestamoService.obtenerPrestamoPorId(prestamoId);
      return prestamo.getPerfil().getId().equals(perfilId);
    } catch (Exception e) {
      log.error("Error verificando préstamo {}: {}", prestamoId, e.getMessage());
      return false;
    }
  }

  /**
   * Validar disponibilidad completa para préstamo
   */
  private ValidacionPrestamoResult validarDisponibilidadPrestamo(PrestamoRequestDTO request) {
    ValidacionPrestamoResult resultado = ValidacionPrestamoResult.exito("CREAR");

    try {
      // Validar contenido disponible
      if (!prestamoService.verificarDisponibilidadContenido(request.getContenidoId())) {
        resultado.agregarError("El contenido no está disponible para préstamo");
      }

      // Validar que no tenga el mismo contenido prestado
      if (prestamoService.perfilTieneContenidoPrestado(request.getPerfilId(), request.getContenidoId())) {
        resultado.agregarError("Ya tiene este contenido en préstamo activo");
      }

      // Validar límites del perfil
      if (!prestamoService.puedeRealizarPrestamo(request.getPerfilId())) {
        resultado.agregarError("Ha alcanzado el límite de préstamos simultáneos");
        resultado.agregarAdvertencia("Considere mejorar su plan o devolver algún contenido");
      }

    } catch (Exception e) {
      log.error("Error en validación de préstamo: {}", e.getMessage());
      resultado.agregarError("Error validando el préstamo");
    }

    return resultado;
  }

  /**
   * Agregar estadísticas del perfil actual al modelo
   */
  private void agregarEstadisticasPerfil(Model model, Perfil perfil) {
    try {
      List<PrestamoResponseDTO> todosLosPrestamos = prestamoService.obtenerPrestamosPorPerfil(perfil.getId());
      List<PrestamoResponseDTO> prestamosActivos = prestamoService.obtenerPrestamosActivosPorPerfil(perfil.getId());

      long prestamosVencidos = prestamosActivos.stream()
          .mapToLong(p -> p.isVencido() ? 1 : 0)
          .sum();

      model.addAttribute("totalPrestamos", todosLosPrestamos.size());
      model.addAttribute("totalPrestamosActivos", prestamosActivos.size());
      model.addAttribute("totalPrestamosVencidos", prestamosVencidos);
      model.addAttribute("totalHistorial", todosLosPrestamos.size() - prestamosActivos.size());

    } catch (Exception e) {
      log.error("Error calculando estadísticas para perfil {}: {}", perfil.getId(), e.getMessage());
      model.addAttribute("totalPrestamos", 0);
      model.addAttribute("totalPrestamosActivos", 0);
      model.addAttribute("totalPrestamosVencidos", 0);
      model.addAttribute("totalHistorial", 0);
    }
  }
}