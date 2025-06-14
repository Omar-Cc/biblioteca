package com.biblioteca.controller.admin;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca.dto.comercial.PagoResponseDTO;
import com.biblioteca.enums.EstadoPago;
import com.biblioteca.service.PagoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/pagos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPagoController {

  private final PagoService pagoService;

  // ========== ENDPOINTS PRINCIPALES DE VISUALIZACIÓN ==========

  @GetMapping
  public String listarTodosLosPagos(Model model) {
    log.info("Listando todos los pagos para administración");
    List<PagoResponseDTO> pagos = pagoService.obtenerTodosLosPagos();
    model.addAttribute("pagos", pagos);
    model.addAttribute("activeTab", "pagos");
    return "admin/pagos/listado-pagos-admin";
  }

  @GetMapping("/{id}")
  public String verDetallePago(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    log.info("Obteniendo detalle del pago ID: {}", id);
    return pagoService.obtenerPagoPorId(id)
        .map(pago -> {
          model.addAttribute("pago", pago);
          model.addAttribute("activeTab", "pagos");
          return "admin/pagos/detalle-pago-admin";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("errorMessage", "Pago no encontrado");
          return "redirect:/admin/pagos";
        });
  }

  // ========== FILTROS Y BÚSQUEDAS ==========

  @GetMapping("/pendientes")
  public String listarPagosPendientes(Model model) {
    log.info("Listando pagos pendientes");
    List<PagoResponseDTO> pagos = pagoService.obtenerPagosPorEstado(EstadoPago.PENDIENTE);
    model.addAttribute("pagos", pagos);
    model.addAttribute("activeTab", "pagos");
    model.addAttribute("filtroActual", "Pendientes");
    return "admin/pagos/listado-pagos-admin";
  }

  @GetMapping("/exitosos")
  public String listarPagosExitosos(Model model) {
    log.info("Listando pagos exitosos");
    List<PagoResponseDTO> pagos = pagoService.obtenerPagosPorEstado(EstadoPago.EXITOSO);
    model.addAttribute("pagos", pagos);
    model.addAttribute("activeTab", "pagos");
    model.addAttribute("filtroActual", "Exitosos");
    return "admin/pagos/listado-pagos-admin";
  }

  @GetMapping("/fallidos")
  public String listarPagosFallidos(Model model) {
    log.info("Listando pagos fallidos");
    List<PagoResponseDTO> pagos = pagoService.obtenerPagosPorEstado(EstadoPago.FALLIDO);
    model.addAttribute("pagos", pagos);
    model.addAttribute("activeTab", "pagos");
    model.addAttribute("filtroActual", "Fallidos");
    return "admin/pagos/listado-pagos-admin";
  }

  @GetMapping("/cancelados")
  public String listarPagosCancelados(Model model) {
    log.info("Listando pagos cancelados");
    List<PagoResponseDTO> pagos = pagoService.obtenerPagosPorEstado(EstadoPago.CANCELADO);
    model.addAttribute("pagos", pagos);
    model.addAttribute("activeTab", "pagos");
    model.addAttribute("filtroActual", "Cancelados");
    return "admin/pagos/listado-pagos-admin";
  }

  @GetMapping("/reembolsados")
  public String listarPagosReembolsados(Model model) {
    log.info("Listando pagos reembolsados");
    List<PagoResponseDTO> pagos = pagoService.obtenerPagosPorEstado(EstadoPago.REEMBOLSADO);
    model.addAttribute("pagos", pagos);
    model.addAttribute("activeTab", "pagos");
    model.addAttribute("filtroActual", "Reembolsados");
    return "admin/pagos/listado-pagos-admin";
  }

  @GetMapping("/estado/{estado}")
  public String listarPagosPorEstado(@PathVariable String estado, Model model, RedirectAttributes redirectAttributes) {
    try {
      EstadoPago estadoPago = EstadoPago.valueOf(estado.toUpperCase());
      List<PagoResponseDTO> pagos = pagoService.obtenerPagosPorEstado(estadoPago);
      model.addAttribute("pagos", pagos);
      model.addAttribute("activeTab", "pagos");
      model.addAttribute("filtroActual", estadoPago.getDescripcion());
      return "admin/pagos/listado-pagos-admin";
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Estado de pago inválido: " + estado);
      return "redirect:/admin/pagos";
    }
  }

  @GetMapping("/por-fechas")
  public String listarPagosPorRangoFechas(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
      Model model) {

    log.info("Listando pagos entre {} y {}", fechaInicio, fechaFin);
    List<PagoResponseDTO> pagos = pagoService.obtenerPagosPorRangoFechas(fechaInicio, fechaFin);
    model.addAttribute("pagos", pagos);
    model.addAttribute("activeTab", "pagos");
    model.addAttribute("filtroActual", "Rango: " + fechaInicio + " - " + fechaFin);
    model.addAttribute("fechaInicio", fechaInicio);
    model.addAttribute("fechaFin", fechaFin);
    return "admin/pagos/listado-pagos-admin";
  }

  @GetMapping("/metodo-pago/{metodoPagoId}")
  public String listarPagosPorMetodoPago(@PathVariable Long metodoPagoId, Model model) {
    log.info("Listando pagos por método de pago ID: {}", metodoPagoId);
    List<PagoResponseDTO> pagos = pagoService.obtenerPagosPorMetodoPago(metodoPagoId);
    model.addAttribute("pagos", pagos);
    model.addAttribute("activeTab", "pagos");
    model.addAttribute("filtroActual", "Método de Pago #" + metodoPagoId);
    return "admin/pagos/listado-pagos-admin";
  }

  // ========== OPERACIONES DE CAMBIO DE ESTADO ==========

  @PostMapping("/{id}/aprobar")
  public String aprobarPago(
      @PathVariable Long id,
      @RequestParam(required = false) String referencia,
      RedirectAttributes redirectAttributes) {

    log.info("Aprobando pago ID: {} con referencia: {}", id, referencia);
    try {
      PagoResponseDTO pagoAprobado = pagoService.aprobarPago(id, referencia);
      redirectAttributes.addFlashAttribute("successMessage",
          "Pago aprobado correctamente. Nuevo estado: " + pagoAprobado.getEstado());
    } catch (Exception e) {
      log.error("Error al aprobar pago {}: {}", id, e.getMessage());
      redirectAttributes.addFlashAttribute("errorMessage", "Error al aprobar el pago: " + e.getMessage());
    }

    return "redirect:/admin/pagos/" + id;
  }

  @PostMapping("/{id}/rechazar")
  public String rechazarPago(
      @PathVariable Long id,
      @RequestParam(required = false) String motivo,
      RedirectAttributes redirectAttributes) {

    log.info("Rechazando pago ID: {} con motivo: {}", id, motivo);
    try {
      PagoResponseDTO pagoRechazado = pagoService.rechazarPago(id, motivo);
      redirectAttributes.addFlashAttribute("successMessage",
          "Pago rechazado correctamente. Nuevo estado: " + pagoRechazado.getEstado());
    } catch (Exception e) {
      log.error("Error al rechazar pago {}: {}", id, e.getMessage());
      redirectAttributes.addFlashAttribute("errorMessage", "Error al rechazar el pago: " + e.getMessage());
    }

    return "redirect:/admin/pagos/" + id;
  }

  @PostMapping("/{id}/procesar")
  public String procesarPago(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    log.info("Procesando pago ID: {}", id);
    try {
      PagoResponseDTO pagoProcesado = pagoService.procesarPago(id);
      redirectAttributes.addFlashAttribute("successMessage",
          "Pago puesto en procesamiento. Estado: " + pagoProcesado.getEstado());
    } catch (Exception e) {
      log.error("Error al procesar pago {}: {}", id, e.getMessage());
      redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar el pago: " + e.getMessage());
    }

    return "redirect:/admin/pagos/" + id;
  }

  @PostMapping("/{id}/verificar-pasarela")
  public String verificarEstadoConPasarela(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    log.info("Verificando estado con pasarela para pago ID: {}", id);
    try {
      PagoResponseDTO pagoVerificado = pagoService.verificarEstadoConPasarela(id);
      redirectAttributes.addFlashAttribute("successMessage",
          "Estado verificado con pasarela. Estado actual: " + pagoVerificado.getEstado());
    } catch (Exception e) {
      log.error("Error al verificar pago {} con pasarela: {}", id, e.getMessage());
      redirectAttributes.addFlashAttribute("errorMessage", "Error al verificar con pasarela: " + e.getMessage());
    }

    return "redirect:/admin/pagos/" + id;
  }

  // ========== ENDPOINTS ESPECÍFICOS PARA SUSCRIPCIONES ==========

  @GetMapping("/suscripcion/{suscripcionId}")
  public String listarPagosPorSuscripcion(@PathVariable Long suscripcionId, Model model) {
    log.info("Listando pagos de suscripción ID: {}", suscripcionId);
    List<PagoResponseDTO> pagos = pagoService.obtenerPagosPorSuscripcion(suscripcionId);
    model.addAttribute("pagos", pagos);
    model.addAttribute("activeTab", "pagos");
    model.addAttribute("filtroActual", "Suscripción #" + suscripcionId);
    model.addAttribute("suscripcionId", suscripcionId);
    return "admin/pagos/listado-pagos-admin";
  }

  @GetMapping("/usuario/{usuarioId}")
  public String listarPagosUnificadosPorUsuario(@PathVariable Long usuarioId, Model model) {
    log.info("Listando pagos unificados del usuario ID: {}", usuarioId);
    List<PagoResponseDTO> pagos = pagoService.obtenerPagosUnificadosPorUsuario(usuarioId);
    model.addAttribute("pagos", pagos);
    model.addAttribute("activeTab", "pagos");
    model.addAttribute("filtroActual", "Usuario #" + usuarioId);
    model.addAttribute("usuarioId", usuarioId);
    return "admin/pagos/listado-pagos-admin";
  }

  @PostMapping("/suscripcion/{suscripcionId}/simular-renovacion")
  public String simularRenovacionAutomatica(@PathVariable Long suscripcionId, RedirectAttributes redirectAttributes) {
    log.info("Simulando renovación automática para suscripción ID: {}", suscripcionId);
    try {
      PagoResponseDTO pagoRenovacion = pagoService.simularRenovacionAutomatica(suscripcionId);
      redirectAttributes.addFlashAttribute("successMessage",
          "Renovación automática simulada. Pago ID: " + pagoRenovacion.getId());
      return "redirect:/admin/pagos/" + pagoRenovacion.getId();
    } catch (Exception e) {
      log.error("Error al simular renovación para suscripción {}: {}", suscripcionId, e.getMessage());
      redirectAttributes.addFlashAttribute("errorMessage", "Error al simular renovación: " + e.getMessage());
      return "redirect:/admin/pagos/suscripcion/" + suscripcionId;
    }
  }

  // ========== ENDPOINTS PARA REPORTES Y ESTADÍSTICAS ==========

  @GetMapping("/reportes")
  public String verReportes(Model model) {
    log.info("Accediendo a reportes de pagos");
    model.addAttribute("activeTab", "pagos");
    return "admin/pagos/reportes-pagos-admin";
  }

  @GetMapping("/api/total-por-fechas")
  @ResponseBody
  public ResponseEntity<Double> calcularTotalPorRangoFechas(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

    try {
      double total = pagoService.calcularTotalPagosPorRangoFechas(fechaInicio, fechaFin);
      return ResponseEntity.ok(total);
    } catch (Exception e) {
      log.error("Error calculando total por fechas: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/api/total-metodo-pago")
  @ResponseBody
  public ResponseEntity<Double> calcularTotalPorMetodoPago(
      @RequestParam Long metodoPagoId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

    try {
      double total = pagoService.calcularTotalPagosPorMetodoPago(metodoPagoId, fechaInicio, fechaFin);
      return ResponseEntity.ok(total);
    } catch (Exception e) {
      log.error("Error calculando total por método de pago: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  // ========== ENDPOINTS PARA BÚSQUEDA Y FILTRADO AVANZADO ==========

  @GetMapping("/buscar")
  public String buscarPagos(
      @RequestParam(required = false) String estado,
      @RequestParam(required = false) Long metodoPagoId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
      @RequestParam(required = false) Long suscripcionId,
      @RequestParam(required = false) Long usuarioId,
      Model model) {

    log.info(
        "Búsqueda avanzada de pagos con parámetros: estado={}, metodoPago={}, fechas={}-{}, suscripción={}, usuario={}",
        estado, metodoPagoId, fechaInicio, fechaFin, suscripcionId, usuarioId);

    List<PagoResponseDTO> pagos;
    String filtroDescripcion = "Búsqueda personalizada";

    // Priorizar filtros más específicos
    if (suscripcionId != null) {
      pagos = pagoService.obtenerPagosPorSuscripcion(suscripcionId);
      filtroDescripcion = "Suscripción #" + suscripcionId;
    } else if (usuarioId != null) {
      pagos = pagoService.obtenerPagosUnificadosPorUsuario(usuarioId);
      filtroDescripcion = "Usuario #" + usuarioId;
    } else if (fechaInicio != null && fechaFin != null) {
      pagos = pagoService.obtenerPagosPorRangoFechas(fechaInicio, fechaFin);
      filtroDescripcion = "Fechas: " + fechaInicio + " - " + fechaFin;
    } else if (metodoPagoId != null) {
      pagos = pagoService.obtenerPagosPorMetodoPago(metodoPagoId);
      filtroDescripcion = "Método de Pago #" + metodoPagoId;
    } else if (estado != null && !estado.isEmpty()) {
      try {
        EstadoPago estadoPago = EstadoPago.valueOf(estado.toUpperCase());
        pagos = pagoService.obtenerPagosPorEstado(estadoPago);
        filtroDescripcion = "Estado: " + estadoPago.getDescripcion();
      } catch (IllegalArgumentException e) {
        pagos = pagoService.obtenerTodosLosPagos();
        filtroDescripcion = "Todos (estado inválido)";
      }
    } else {
      pagos = pagoService.obtenerTodosLosPagos();
      filtroDescripcion = "Todos los pagos";
    }

    model.addAttribute("pagos", pagos);
    model.addAttribute("activeTab", "pagos");
    model.addAttribute("filtroActual", filtroDescripcion);

    // Mantener parámetros de búsqueda para el formulario
    model.addAttribute("estadoBusqueda", estado);
    model.addAttribute("metodoPagoIdBusqueda", metodoPagoId);
    model.addAttribute("fechaInicioBusqueda", fechaInicio);
    model.addAttribute("fechaFinBusqueda", fechaFin);
    model.addAttribute("suscripcionIdBusqueda", suscripcionId);
    model.addAttribute("usuarioIdBusqueda", usuarioId);

    return "admin/pagos/listado-pagos-admin";
  }

  // ========== ENDPOINT PARA ELIMINAR PAGOS (CON PRECAUCIÓN) ==========

  @PostMapping("/{id}/eliminar")
  public String eliminarPago(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    log.warn("Intentando eliminar pago ID: {}", id);
    try {
      boolean eliminado = pagoService.eliminarPago(id);
      if (eliminado) {
        redirectAttributes.addFlashAttribute("successMessage", "Pago eliminado correctamente");
      } else {
        redirectAttributes.addFlashAttribute("errorMessage", "No se pudo eliminar el pago");
      }
    } catch (Exception e) {
      log.error("Error al eliminar pago {}: {}", id, e.getMessage());
      redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el pago: " + e.getMessage());
    }

    return "redirect:/admin/pagos";
  }
}