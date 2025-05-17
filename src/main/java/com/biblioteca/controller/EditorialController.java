package com.biblioteca.controller;

import com.biblioteca.dto.EditorialRequestDTO;
import com.biblioteca.service.EditorialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/editoriales")
@RequiredArgsConstructor
public class EditorialController {

  private final EditorialService editorialService;

  @GetMapping
  public String listarEditoriales(Model model) {
    model.addAttribute("editoriales", editorialService.obtenerTodasLasEditoriales());
    model.addAttribute("activeTab", "editoriales");
    return "admin/editoriales/lista";
  }

  @GetMapping("/nueva")
  public String mostrarFormularioNuevaEditorial(Model model) {
    model.addAttribute("editorialRequestDTO", EditorialRequestDTO.builder().build());
    model.addAttribute("activeTab", "editoriales");
    return "admin/editoriales/formulario";
  }

  @PostMapping("/guardar")
  public String guardarEditorial(@Valid @ModelAttribute("editorialRequestDTO") EditorialRequestDTO editorialRequestDTO,
      BindingResult result, RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
      return "admin/editoriales/formulario";
    }
    editorialService.crearEditorial(editorialRequestDTO);
    redirectAttributes.addFlashAttribute("successMessage", "Editorial creada exitosamente.");
    return "redirect:/admin/editoriales";
  }

  @GetMapping("/editar/{id}")
  public String mostrarFormularioEditarEditorial(@PathVariable Long id, Model model,
      RedirectAttributes redirectAttributes) {
    return editorialService.obtenerEditorialPorId(id)
        .map(editorial -> {
          EditorialRequestDTO requestDTO = EditorialRequestDTO.builder()
              .nombre(editorial.getNombre())
              .pais(editorial.getPais())
              .website(editorial.getWebsite())
              .build();
          model.addAttribute("editorialRequestDTO", requestDTO);
          model.addAttribute("editorialId", id);
          model.addAttribute("activeTab", "editoriales");
          return "admin/editoriales/formulario";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("errorMessage", "Editorial no encontrada.");
          return "redirect:/admin/editoriales";
        });
  }

  @PostMapping("/actualizar/{id}")
  public String actualizarEditorial(@PathVariable Long id,
      @Valid @ModelAttribute("editorialRequestDTO") EditorialRequestDTO editorialRequestDTO,
      BindingResult result, RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
      return "admin/editoriales/formulario";
    }
    editorialService.actualizarEditorial(id, editorialRequestDTO);
    redirectAttributes.addFlashAttribute("successMessage", "Editorial actualizada exitosamente.");
    return "redirect:/admin/editoriales";
  }

  @PostMapping("/eliminar/{id}")
  public String eliminarEditorial(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    editorialService.eliminarEditorial(id);
    redirectAttributes.addFlashAttribute("successMessage", "Editorial eliminada exitosamente.");
    return "redirect:/admin/editoriales";
  }
}