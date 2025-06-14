package com.biblioteca.controller.admin;

import java.util.List;
import java.util.Optional;

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

import com.biblioteca.dto.comercial.PlanBeneficioRequestDTO;
import com.biblioteca.models.comercial.PlanBeneficio;
import com.biblioteca.service.BeneficioService;
import com.biblioteca.service.PlanBeneficioService;
import com.biblioteca.service.PlanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/planes-beneficios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPlanBeneficioController {

  private final PlanBeneficioService planBeneficioService;
  private final PlanService planService;
  private final BeneficioService beneficioService;

  @GetMapping("/plan/{planId}")
  public String listarBeneficiosPorPlan(@PathVariable Long planId, Model model, RedirectAttributes redirectAttributes) {
    // Verificar que el plan existe
    if (planService.obtenerPlanPorId(planId).isEmpty()) {
      redirectAttributes.addFlashAttribute("errorMessage", "Plan no encontrado con ID: " + planId);
      return "redirect:/admin/planes";
    }

    model.addAttribute("plan", planService.obtenerPlanPorId(planId).get());
    model.addAttribute("beneficiosAsociados", planBeneficioService.obtenerBeneficiosPorPlanId(planId));
    model.addAttribute("beneficiosDisponibles", beneficioService.obtenerBeneficiosActivos());
    model.addAttribute("activeTab", "planes");
    return "admin/planes-beneficios/lista-por-plan";
  }

  @GetMapping("/beneficio/{beneficioId}")
  public String listarPlanesPorBeneficio(@PathVariable Long beneficioId, Model model,
      RedirectAttributes redirectAttributes) {
    // Verificar que el beneficio existe
    if (beneficioService.obtenerBeneficioPorId(beneficioId).isEmpty()) {
      redirectAttributes.addFlashAttribute("errorMessage", "Beneficio no encontrado con ID: " + beneficioId);
      return "redirect:/admin/beneficios";
    }

    model.addAttribute("beneficio", beneficioService.obtenerBeneficioPorId(beneficioId).get());
    model.addAttribute("planesAsociados", planBeneficioService.obtenerPlanesPorBeneficioId(beneficioId));
    model.addAttribute("activeTab", "beneficios");
    return "admin/planes-beneficios/lista-por-beneficio";
  }

  @GetMapping("/asociar")
  public String mostrarFormularioAsociar(
      @RequestParam(required = false) Long planId,
      @RequestParam(required = false) Long beneficioId,
      Model model) {

    PlanBeneficioRequestDTO planBeneficioForm = new PlanBeneficioRequestDTO();

    // Si se proporciona planId, precargar el formulario
    if (planId != null) {
      model.addAttribute("planSeleccionado", planService.obtenerPlanPorId(planId).orElse(null));
      planBeneficioForm.setPlanId(planId);
    }

    // Si se proporciona beneficioId, precargar el formulario
    if (beneficioId != null) {
      model.addAttribute("beneficioSeleccionado", beneficioService.obtenerBeneficioPorId(beneficioId).orElse(null));
      planBeneficioForm.setBeneficioId(beneficioId);
    }

    model.addAttribute("planes", planService.obtenerPlanesActivos());
    model.addAttribute("beneficios", beneficioService.obtenerBeneficiosActivos());
    model.addAttribute("planBeneficioForm", planBeneficioForm);
    model.addAttribute("activeTab", planId != null ? "planes" : "beneficios");

    return "admin/planes-beneficios/formulario";
  }

  @PostMapping("/asociar")
  public String asociarBeneficioAPlan(
      @Valid @ModelAttribute("planBeneficioForm") PlanBeneficioRequestDTO planBeneficioForm,
      BindingResult result,
      Model model,
      RedirectAttributes redirectAttributes) {

    if (result.hasErrors()) {
      model.addAttribute("planes", planService.obtenerPlanesActivos());
      model.addAttribute("beneficios", beneficioService.obtenerBeneficiosActivos());
      return "admin/planes-beneficios/formulario";
    }

    try {
      planBeneficioService.asociarBeneficioAPlan(planBeneficioForm);
      redirectAttributes.addFlashAttribute("mensaje", "Beneficio asociado al plan exitosamente");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error al asociar: " + e.getMessage());
    }

    return "redirect:/admin/planes-beneficios/plan/" + planBeneficioForm.getPlanId();
  }

  @PostMapping("/asociar-varios/{planId}")
  public String asociarVariosBeneficios(
      @PathVariable Long planId,
      @RequestParam List<Long> beneficiosIds,
      @RequestParam(defaultValue = "true") boolean activo,
      RedirectAttributes redirectAttributes) {

    try {
      // Crear DTOs para cada beneficio seleccionado
      List<PlanBeneficioRequestDTO> dtos = beneficiosIds.stream()
          .map(beneficioId -> {
            PlanBeneficioRequestDTO dto = new PlanBeneficioRequestDTO();
            dto.setPlanId(planId);
            dto.setBeneficioId(beneficioId);
            dto.setActivo(activo);
            return dto;
          })
          .toList();

      planBeneficioService.asociarVariosBeneficiosAPlan(planId, dtos);
      redirectAttributes.addFlashAttribute("mensaje",
          "Se asociaron " + beneficiosIds.size() + " beneficios al plan exitosamente");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error al asociar beneficios: " + e.getMessage());
    }

    return "redirect:/admin/planes-beneficios/plan/" + planId;
  }

  @GetMapping("/{planId}/{beneficioId}/editar")
  public String mostrarFormularioEditar(
      @PathVariable Long planId,
      @PathVariable Long beneficioId,
      Model model) {

    return planBeneficioService.obtenerAsociacionPorPlanIdYBeneficioId(planId, beneficioId)
        .map(planBeneficio -> {
          PlanBeneficioRequestDTO planBeneficioForm = new PlanBeneficioRequestDTO();
          planBeneficioForm.setPlanId(planId);
          planBeneficioForm.setBeneficioId(beneficioId);
          planBeneficioForm.setValor(planBeneficio.getValor());
          planBeneficioForm.setActivo(planBeneficio.isActivo());

          model.addAttribute("plan", planService.obtenerPlanPorId(planId).orElse(null));
          model.addAttribute("beneficio", beneficioService.obtenerBeneficioPorId(beneficioId).orElse(null));
          model.addAttribute("planBeneficioForm", planBeneficioForm);
          model.addAttribute("activeTab", "planes");

          return "admin/planes-beneficios/editar";
        })
        .orElse("redirect:/admin/planes-beneficios/plan/" + planId + "?error=Asociación+no+encontrada");
  }

  @PostMapping("/{planId}/{beneficioId}/actualizar")
  public String actualizarAsociacion(
      @PathVariable Long planId,
      @PathVariable Long beneficioId,
      @Valid @ModelAttribute("planBeneficioForm") PlanBeneficioRequestDTO planBeneficioForm,
      BindingResult result,
      Model model,
      RedirectAttributes redirectAttributes) {

    if (result.hasErrors()) {
      model.addAttribute("plan", planService.obtenerPlanPorId(planId).orElse(null));
      model.addAttribute("beneficio", beneficioService.obtenerBeneficioPorId(beneficioId).orElse(null));
      return "admin/planes-beneficios/editar";
    }

    Optional<PlanBeneficio> pbOpt = planBeneficioService.obtenerEntidadAsociacionPorPlanIdYBeneficioId(planId,
        beneficioId);
    if (pbOpt.isEmpty()) {
      redirectAttributes.addFlashAttribute("errorMessage", "Asociación no encontrada para actualizar.");
      return "redirect:/admin/planes-beneficios/plan/" + planId;
    }

    try {
      planBeneficioService.actualizarAsociacion(pbOpt.get().getId(), planBeneficioForm);
      redirectAttributes.addFlashAttribute("successMessage", "Asociación actualizada exitosamente.");
      return "redirect:/admin/planes-beneficios/plan/" + planId;
    } catch (Exception e) {
      model.addAttribute("errorMessage", "Error al actualizar la asociación: " + e.getMessage());
      model.addAttribute("planNombre", planService.obtenerPlanPorId(planId).map(p -> p.getNombre()).orElse("N/A"));
      model.addAttribute("beneficioNombre",
          beneficioService.obtenerBeneficioPorId(beneficioId).map(b -> b.getNombre()).orElse("N/A"));
      model.addAttribute("isEdit", true);
      model.addAttribute("activeTab", "planes");
      return "admin/planes-beneficios/form-edit";
    }
  }

  @PostMapping("/{planId}/{beneficioId}/eliminar")
  public String eliminarAsociacion(
      @PathVariable Long planId,
      @PathVariable Long beneficioId,
      RedirectAttributes redirectAttributes) {

    try {
      boolean eliminado = planBeneficioService.eliminarAsociacionPorPlanIdYBeneficioId(planId, beneficioId);
      if (eliminado) {
        redirectAttributes.addFlashAttribute("successMessage", "Asociación eliminada exitosamente.");
      } else {
        redirectAttributes.addFlashAttribute("errorMessage", "No se pudo encontrar la asociación para eliminar.");
      }
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la asociación: " + e.getMessage());
    }

    return "redirect:/admin/planes-beneficios/plan/" + planId;
  }

  @PostMapping("/{planId}/{beneficioId}/cambiar-estado")
  public String cambiarEstadoAsociacion(
      @PathVariable Long planId,
      @PathVariable Long beneficioId,
      @RequestParam boolean activar,
      RedirectAttributes redirectAttributes) {

    Optional<PlanBeneficio> pbOpt = planBeneficioService.obtenerEntidadAsociacionPorPlanIdYBeneficioId(planId,
        beneficioId);
    if (pbOpt.isEmpty()) {
      redirectAttributes.addFlashAttribute("errorMessage", "Asociación no encontrada.");
      return "redirect:/admin/planes-beneficios/plan/" + planId;
    }
    Long asociacionId = pbOpt.get().getId();
    try {
      boolean exito;
      if (activar) {
        exito = planBeneficioService.activarAsociacion(asociacionId);
      } else {
        exito = planBeneficioService.desactivarAsociacion(asociacionId);
      }

      if (exito) {
        redirectAttributes.addFlashAttribute("successMessage", "Estado de la asociación cambiado exitosamente.");
      } else {
        redirectAttributes.addFlashAttribute("errorMessage", "No se pudo cambiar el estado de la asociación.");
      }
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error al cambiar estado: " + e.getMessage());
    }
    return "redirect:/admin/planes-beneficios/plan/" + planId;
  }
}