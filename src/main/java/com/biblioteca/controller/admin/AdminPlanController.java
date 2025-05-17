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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca.dto.comercial.PlanRequestDTO;
import com.biblioteca.service.PlanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/planes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPlanController {

  private final PlanService planService;

  @GetMapping
  public String listarPlanes(Model model) {
    model.addAttribute("planes", planService.obtenerTodosLosPlanes());
    model.addAttribute("activeTab", "planes");
    return "admin/planes/lista";
  }

  @GetMapping("/nuevo")
  public String mostrarFormularioNuevo(Model model) {
    model.addAttribute("planForm", new PlanRequestDTO());
    model.addAttribute("activeTab", "planes");
    return "admin/planes/formulario";
  }

  @PostMapping("/crear")
  public String crearPlan(@Valid @ModelAttribute("planForm") PlanRequestDTO planForm,
      BindingResult result,
      RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
      return "admin/planes/formulario";
    }

    planService.crearPlan(planForm);
    redirectAttributes.addFlashAttribute("mensaje", "Plan creado exitosamente");
    return "redirect:/admin/planes";
  }

  @GetMapping("/{id}/editar")
  public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
    return planService.obtenerPlanPorId(id)
        .map(plan -> {
          PlanRequestDTO planForm = new PlanRequestDTO();
          planForm.setNombre(plan.getNombre());
          planForm.setPrecioMensual(plan.getPrecioMensual());
          planForm.setPrecioAnual(plan.getPrecioAnual());
          planForm.setDiasPrueba(plan.getDiasPrueba());
          planForm.setActivo(plan.isActivo());

          model.addAttribute("planForm", planForm);
          model.addAttribute("planId", id);
          model.addAttribute("activeTab", "planes");
          return "admin/planes/formulario";
        })
        .orElse("redirect:/admin/planes?error=Plan+no+encontrado");
  }

  @PostMapping("/{id}/actualizar")
  public String actualizarPlan(@PathVariable Long id,
      @Valid @ModelAttribute("planForm") PlanRequestDTO planForm,
      BindingResult result,
      RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
      return "admin/planes/formulario";
    }

    return planService.actualizarPlan(id, planForm)
        .map(plan -> {
          redirectAttributes.addFlashAttribute("mensaje", "Plan actualizado exitosamente");
          return "redirect:/admin/planes";
        })
        .orElse("redirect:/admin/planes?error=Error+al+actualizar+el+plan");
  }

  @PostMapping("/{id}/eliminar")
  public String eliminarPlan(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    boolean eliminado = planService.eliminarPlan(id);
    if (eliminado) {
      redirectAttributes.addFlashAttribute("mensaje", "Plan eliminado exitosamente");
    } else {
      redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el plan");
    }
    return "redirect:/admin/planes";
  }

  @PostMapping("/{id}/cambiar-estado")
  public String cambiarEstadoPlan(@PathVariable Long id,
      @RequestParam boolean activar,
      RedirectAttributes redirectAttributes) {
    boolean resultado = activar ? planService.activarPlan(id) : planService.desactivarPlan(id);

    if (resultado) {
      redirectAttributes.addFlashAttribute("mensaje",
          "Plan " + (activar ? "activado" : "desactivado") + " exitosamente");
    } else {
      redirectAttributes.addFlashAttribute("error",
          "No se pudo " + (activar ? "activar" : "desactivar") + " el plan");
    }
    return "redirect:/admin/planes";
  }
}