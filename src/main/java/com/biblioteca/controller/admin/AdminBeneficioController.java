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

import com.biblioteca.dto.comercial.BeneficioRequestDTO;
import com.biblioteca.service.BeneficioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/beneficios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBeneficioController {

  private final BeneficioService beneficioService;

  @GetMapping
  public String listarBeneficios(Model model) {
    model.addAttribute("beneficios", beneficioService.obtenerTodosLosBeneficios());
    model.addAttribute("activeTab", "beneficios");
    return "admin/beneficios/lista";
  }

  @GetMapping("/nuevo")
  public String mostrarFormularioNuevo(Model model) {
    model.addAttribute("beneficioForm", new BeneficioRequestDTO());
    model.addAttribute("activeTab", "beneficios");
    return "admin/beneficios/formulario";
  }

  @PostMapping("/crear")
  public String crearBeneficio(@Valid @ModelAttribute("beneficioForm") BeneficioRequestDTO beneficioForm,
      BindingResult result,
      RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
      return "admin/beneficios/formulario";
    }

    beneficioService.crearBeneficio(beneficioForm);
    redirectAttributes.addFlashAttribute("mensaje", "Beneficio creado exitosamente");
    return "redirect:/admin/beneficios";
  }

  @GetMapping("/{id}/editar")
  public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
    return beneficioService.obtenerBeneficioPorId(id)
        .map(beneficio -> {
          // Convertir a DTO de solicitud para el formulario
          BeneficioRequestDTO beneficioForm = new BeneficioRequestDTO();
          beneficioForm.setNombre(beneficio.getNombre());
          beneficioForm.setDescripcion(beneficio.getDescripcion());
          beneficioForm.setIcono(beneficio.getIcono());
          beneficioForm.setTipoDato(beneficio.getTipoDato());
          beneficioForm.setCategoriaId(beneficio.getCategoriaId());
          beneficioForm.setActivo(beneficio.isActivo());

          model.addAttribute("beneficioForm", beneficioForm);
          model.addAttribute("beneficioId", id);
          model.addAttribute("activeTab", "beneficios");
          return "admin/beneficios/formulario";
        })
        .orElse("redirect:/admin/beneficios?error=Beneficio+no+encontrado");
  }

  @PostMapping("/{id}/actualizar")
  public String actualizarBeneficio(@PathVariable Long id,
      @Valid @ModelAttribute("beneficioForm") BeneficioRequestDTO beneficioForm,
      BindingResult result,
      RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
      return "admin/beneficios/formulario";
    }

    return beneficioService.actualizarBeneficio(id, beneficioForm)
        .map(beneficio -> {
          redirectAttributes.addFlashAttribute("mensaje", "Beneficio actualizado exitosamente");
          return "redirect:/admin/beneficios";
        })
        .orElse("redirect:/admin/beneficios?error=Error+al+actualizar+el+beneficio");
  }

  @PostMapping("/{id}/eliminar")
  public String eliminarBeneficio(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    boolean eliminado = beneficioService.eliminarBeneficio(id);
    if (eliminado) {
      redirectAttributes.addFlashAttribute("mensaje", "Beneficio eliminado exitosamente");
    } else {
      redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el beneficio");
    }
    return "redirect:/admin/beneficios";
  }
}