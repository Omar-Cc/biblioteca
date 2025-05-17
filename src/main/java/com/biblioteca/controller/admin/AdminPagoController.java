package com.biblioteca.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca.service.PagoService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/pagos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPagoController {

  private final PagoService pagoService;

  @GetMapping
  public String listarTodosLosPagos(Model model) {
    model.addAttribute("pagos", pagoService.obtenerTodosLosPagos());
    model.addAttribute("activeTab", "pagos");
    return "admin/pagos/lista";
  }

  @GetMapping("/{id}")
  public String verDetallePago(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    return pagoService.obtenerPagoPorId(id)
        .map(pago -> {
          model.addAttribute("pago", pago);
          model.addAttribute("activeTab", "pagos");
          return "admin/pagos/detalle";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Pago no encontrado");
          return "redirect:/admin/pagos";
        });
  }

  @PostMapping("/{id}/aprobar")
  public String aprobarPago(
      @PathVariable Long id,
      @RequestParam(required = false) String referencia,
      RedirectAttributes redirectAttributes) {

    try {
      pagoService.aprobarPago(id, referencia);
      redirectAttributes.addFlashAttribute("mensaje", "Pago aprobado correctamente");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error al aprobar el pago: " + e.getMessage());
    }

    return "redirect:/admin/pagos/" + id;
  }

  @PostMapping("/{id}/rechazar")
  public String rechazarPago(
      @PathVariable Long id,
      @RequestParam(required = false) String motivo,
      RedirectAttributes redirectAttributes) {

    try {
      pagoService.rechazarPago(id, motivo);
      redirectAttributes.addFlashAttribute("mensaje", "Pago rechazado correctamente");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error al rechazar el pago: " + e.getMessage());
    }

    return "redirect:/admin/pagos/" + id;
  }

  @GetMapping("/pendientes")
  public String listarPagosPendientes(Model model) {
    model.addAttribute("pagos", pagoService.obtenerPagosPorEstado("Pendiente"));
    model.addAttribute("activeTab", "pagos");
    model.addAttribute("filtroActual", "Pendientes");
    return "admin/pagos/lista";
  }
}