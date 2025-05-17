package com.biblioteca.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca.dto.comercial.MetodoPagoRequestDTO;
import com.biblioteca.service.MetodoPagoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/metodos-pago")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMetodoPagoController {

  private final MetodoPagoService metodoPagoService;

  @GetMapping
  public String listarMetodosPago(Model model) {
    model.addAttribute("metodosPago", metodoPagoService.obtenerTodosLosMetodosPago());
    model.addAttribute("activeTab", "metodosPago");
    return "admin/metodos-pago/lista";
  }

  @GetMapping("/activos")
  public String listarMetodosPagoActivos(Model model) {
    model.addAttribute("metodosPago", metodoPagoService.obtenerMetodosPagoActivos());
    model.addAttribute("soloActivos", true);
    model.addAttribute("activeTab", "metodosPago");
    return "admin/metodos-pago/lista";
  }

  @GetMapping("/nuevo")
  public String mostrarFormularioNuevo(Model model) {
    model.addAttribute("metodoPagoForm", new MetodoPagoRequestDTO());
    model.addAttribute("activeTab", "metodosPago");
    return "admin/metodos-pago/formulario";
  }

  @PostMapping("/crear")
  public String crearMetodoPago(
      @Valid @ModelAttribute("metodoPagoForm") MetodoPagoRequestDTO metodoPagoForm,
      BindingResult result,
      RedirectAttributes redirectAttributes) {

    if (result.hasErrors()) {
      return "admin/metodos-pago/formulario";
    }

    metodoPagoService.crearMetodoPago(metodoPagoForm);
    redirectAttributes.addFlashAttribute("mensaje", "Método de pago creado exitosamente");
    return "redirect:/admin/metodos-pago";
  }

  @GetMapping("/{id}")
  public String verDetalleMetodoPago(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    return metodoPagoService.obtenerMetodoPagoPorId(id)
        .map(metodoPago -> {
          model.addAttribute("metodoPago", metodoPago);
          model.addAttribute("activeTab", "metodosPago");
          return "admin/metodos-pago/detalle";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Método de pago no encontrado");
          return "redirect:/admin/metodos-pago";
        });
  }

  @GetMapping("/{id}/editar")
  public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    return metodoPagoService.obtenerMetodoPagoPorId(id)
        .map(metodoPago -> {
          MetodoPagoRequestDTO metodoPagoForm = new MetodoPagoRequestDTO();
          metodoPagoForm.setNombre(metodoPago.getNombre());
          metodoPagoForm.setDescripcion(metodoPago.getDescripcion());
          metodoPagoForm.setRequiereAutorizacion(metodoPago.isRequiereAutorizacion());
          metodoPagoForm.setActivo(metodoPago.isActivo());

          model.addAttribute("metodoPagoForm", metodoPagoForm);
          model.addAttribute("metodoPagoId", id);
          model.addAttribute("activeTab", "metodosPago");
          return "admin/metodos-pago/formulario";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Método de pago no encontrado");
          return "redirect:/admin/metodos-pago";
        });
  }

  @PostMapping("/{id}/actualizar")
  public String actualizarMetodoPago(
      @PathVariable Long id,
      @Valid @ModelAttribute("metodoPagoForm") MetodoPagoRequestDTO metodoPagoForm,
      BindingResult result,
      RedirectAttributes redirectAttributes) {

    if (result.hasErrors()) {
      return "admin/metodos-pago/formulario";
    }

    return metodoPagoService.actualizarMetodoPago(id, metodoPagoForm)
        .map(metodoPago -> {
          redirectAttributes.addFlashAttribute("mensaje", "Método de pago actualizado exitosamente");
          return "redirect:/admin/metodos-pago";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Error al actualizar el método de pago");
          return "redirect:/admin/metodos-pago";
        });
  }

  @PostMapping("/{id}/eliminar")
  public String eliminarMetodoPago(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    boolean eliminado = metodoPagoService.eliminarMetodoPago(id);
    if (eliminado) {
      redirectAttributes.addFlashAttribute("mensaje", "Método de pago eliminado exitosamente");
    } else {
      redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el método de pago");
    }
    return "redirect:/admin/metodos-pago";
  }

  @PostMapping("/{id}/activar")
  public String activarMetodoPago(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    boolean activado = metodoPagoService.activarMetodoPago(id);
    if (activado) {
      redirectAttributes.addFlashAttribute("mensaje", "Método de pago activado exitosamente");
    } else {
      redirectAttributes.addFlashAttribute("error", "No se pudo activar el método de pago");
    }
    return "redirect:/admin/metodos-pago/" + id;
  }

  @PostMapping("/{id}/desactivar")
  public String desactivarMetodoPago(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    boolean desactivado = metodoPagoService.desactivarMetodoPago(id);
    if (desactivado) {
      redirectAttributes.addFlashAttribute("mensaje", "Método de pago desactivado exitosamente");
    } else {
      redirectAttributes.addFlashAttribute("error", "No se pudo desactivar el método de pago");
    }
    return "redirect:/admin/metodos-pago/" + id;
  }
}