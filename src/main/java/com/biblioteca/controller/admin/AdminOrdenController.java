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

import com.biblioteca.service.OrdenService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/ordenes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrdenController {

  private final OrdenService ordenService;

  @GetMapping
  public String listarTodasLasOrdenes(Model model) {
    model.addAttribute("ordenes", ordenService.obtenerTodasLasOrdenes());
    model.addAttribute("activeTab", "ordenes");
    return "admin/ordenes/lista-ordenes-admin";
  }

  @GetMapping("/{id}")
  public String verDetalleOrden(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    return ordenService.obtenerOrdenPorId(id)
        .map(orden -> {
          model.addAttribute("orden", orden);
          model.addAttribute("items", ordenService.obtenerItemsDeOrden(id));
          model.addAttribute("activeTab", "ordenes");
          return "admin/ordenes/detalle-orden-admin";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Orden no encontrada");
          return "redirect:/admin/ordenes";
        });
  }

  @PostMapping("/{id}/cambiar-estado")
  public String cambiarEstadoOrden(
      @PathVariable Long id,
      @RequestParam String nuevoEstado,
      @RequestParam(required = false) String motivo,
      RedirectAttributes redirectAttributes) {

    try {
      switch (nuevoEstado) {
        case "Procesada":
          ordenService.procesarOrden(id);
          break;
        case "Completada":
          ordenService.completarOrden(id);
          break;
        case "Cancelada":
          ordenService.cancelarOrden(id, motivo);
          break;
        default:
          redirectAttributes.addFlashAttribute("error", "Estado no válido");
          return "redirect:/admin/ordenes/" + id;
      }

      redirectAttributes.addFlashAttribute("mensaje", "Estado de la orden actualizado a " + nuevoEstado);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error al cambiar el estado: " + e.getMessage());
    }

    return "redirect:/admin/ordenes/" + id;
  }
}