package com.biblioteca.controller;

import com.biblioteca.dto.GeneroRequestDTO;
import com.biblioteca.service.GeneroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/generos")
@RequiredArgsConstructor
public class GeneroController {

  private final GeneroService generoService;

  @GetMapping
  public String listarGeneros(Model model) {
    model.addAttribute("generos", generoService.obtenerTodosLosGeneros());
    model.addAttribute("activeTab", "generos");
    return "admin/generos/lista";
  }

  @GetMapping("/nuevo")
  public String mostrarFormularioNuevoGenero(Model model) {
    model.addAttribute("generoRequestDTO", GeneroRequestDTO.builder().build());
    model.addAttribute("activeTab", "generos");
    return "admin/generos/formulario";
  }

  @PostMapping("/guardar")
  public String guardarGenero(@Valid @ModelAttribute("generoRequestDTO") GeneroRequestDTO generoRequestDTO,
      BindingResult result, RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
      return "admin/generos/formulario";
    }
    generoService.crearGenero(generoRequestDTO);
    redirectAttributes.addFlashAttribute("successMessage", "Género creado exitosamente.");
    return "redirect:/admin/generos";
  }

  @GetMapping("/editar/{id}")
  public String mostrarFormularioEditarGenero(@PathVariable Long id, Model model,
      RedirectAttributes redirectAttributes) {
    return generoService.obtenerGeneroPorId(id)
        .map(genero -> {
          GeneroRequestDTO requestDTO = GeneroRequestDTO.builder()
              .nombre(genero.getNombre())
              .descripcion(genero.getDescripcion())
              .build();
          model.addAttribute("generoRequestDTO", requestDTO);
          model.addAttribute("generoId", id);
          model.addAttribute("activeTab", "generos");
          return "admin/generos/formulario";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("errorMessage", "Género no encontrado.");
          return "redirect:/admin/generos";
        });
  }

  @PostMapping("/actualizar/{id}")
  public String actualizarGenero(@PathVariable Long id,
      @Valid @ModelAttribute("generoRequestDTO") GeneroRequestDTO generoRequestDTO,
      BindingResult result, RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
      return "admin/generos/formulario";
    }
    generoService.actualizarGenero(id, generoRequestDTO);
    redirectAttributes.addFlashAttribute("successMessage", "Género actualizado exitosamente.");
    return "redirect:/admin/generos";
  }

  @PostMapping("/eliminar/{id}")
  public String eliminarGenero(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    generoService.eliminarGenero(id);
    redirectAttributes.addFlashAttribute("successMessage", "Género eliminado exitosamente.");
    return "redirect:/admin/generos";
  }
}