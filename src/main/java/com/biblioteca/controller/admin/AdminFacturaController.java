package com.biblioteca.controller.admin;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca.service.FacturaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/facturas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminFacturaController {

  private final FacturaService facturaService;

  @GetMapping
  public String listarTodasLasFacturas(Model model) {
    model.addAttribute("facturas", facturaService.obtenerTodasLasFacturas());
    model.addAttribute("activeTab", "facturas");
    return "admin/facturas/lista";
  }

  @GetMapping("/buscar")
  public String buscarFacturas(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
      Model model) {

    model.addAttribute("facturas", facturaService.buscarFacturasPorRangoFechas(fechaInicio, fechaFin));
    model.addAttribute("fechaInicio", fechaInicio);
    model.addAttribute("fechaFin", fechaFin);
    model.addAttribute("activeTab", "facturas");
    return "admin/facturas/lista";
  }

  @GetMapping("/{id}")
  public String verFactura(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    return facturaService.obtenerFacturaPorId(id)
        .map(factura -> {
          model.addAttribute("factura", factura);
          model.addAttribute("activeTab", "facturas");
          return "admin/facturas/detalle";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Factura no encontrada");
          return "redirect:/admin/facturas";
        });
  }

  @GetMapping("/{id}/imprimir")
  public String prepararImpresionFactura(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    return facturaService.obtenerFacturaPorId(id)
        .map(factura -> {
          model.addAttribute("factura", factura);
          model.addAttribute("activeTab", "facturas");
          return "admin/facturas/imprimir";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Factura no encontrada");
          return "redirect:/admin/facturas";
        });
  }

  @GetMapping("/reporte")
  public String verReporteVentas(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
      Model model) {

    // Si no se proporcionan fechas, usar el mes actual
    if (fechaInicio == null) {
      fechaInicio = LocalDate.now().withDayOfMonth(1);
    }
    if (fechaFin == null) {
      fechaFin = LocalDate.now();
    }

    model.addAttribute("reporte", facturaService.generarReporteVentas(fechaInicio, fechaFin));
    model.addAttribute("fechaInicio", fechaInicio);
    model.addAttribute("fechaFin", fechaFin);
    model.addAttribute("activeTab", "reportes");
    return "admin/facturas/reporte";
  }

  @GetMapping("/estadisticas")
  public String verEstadisticasVentas(Model model) {
    model.addAttribute("estadisticas", facturaService.obtenerEstadisticasVentas());
    model.addAttribute("activeTab", "reportes");
    return "admin/facturas/estadisticas";
  }

  @PostMapping("/{id}/anular")
  public String anularFactura(
      @PathVariable Long id,
      @RequestParam String motivo,
      RedirectAttributes redirectAttributes) {

    boolean anulada = facturaService.anularFactura(id, motivo);
    if (anulada) {
      redirectAttributes.addFlashAttribute("mensaje", "Factura anulada exitosamente");
    } else {
      redirectAttributes.addFlashAttribute("error", "No se pudo anular la factura");
    }

    return "redirect:/admin/facturas";
  }
}