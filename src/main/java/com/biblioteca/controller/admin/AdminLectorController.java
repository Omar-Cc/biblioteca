package com.biblioteca.controller.admin;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca.dto.LectorResponseDTO;
import com.biblioteca.service.LectorService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/lectores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLectorController {

  private final LectorService lectorService;

  /**
   * Listar todos los lectores
   */
  @GetMapping
  public String listarLectores(Model model) {
    List<LectorResponseDTO> lectores = lectorService.listarTodosLosLectores();
    model.addAttribute("lectores", lectores);
    model.addAttribute("activeTab", "lectores");
    return "admin/lista-lectores";
  }

  /**
   * Detalles de un lector específico
   */
  @GetMapping("/{username}")
  public String detallesLector(@PathVariable String username, Model model, RedirectAttributes redirectAttributes) {
    try {
      LectorResponseDTO lector = lectorService.obtenerPerfilLectorPorUsername(username)
          .orElseThrow(() -> new IllegalArgumentException("Lector no encontrado: " + username));

      model.addAttribute("lector", lector);
      model.addAttribute("activeTab", "lectores");
      return "admin/detalle-lector";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
      return "redirect:/admin/lectores";
    }
  }

  /**
   * Cambiar estado de un lector
   */
  @PostMapping("/{username}/cambiar-estado")
  public String cambiarEstadoLector(
      @PathVariable String username,
      @RequestParam String nuevoEstado,
      RedirectAttributes redirectAttributes) {

    try {
      lectorService.cambiarEstadoLector(username, nuevoEstado);
      redirectAttributes.addFlashAttribute("mensaje",
          "Estado del lector actualizado a: " + nuevoEstado);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
    }

    return "redirect:/admin/lectores/" + username;
  }

  /**
   * Actualizar multas pendientes
   */
  @PostMapping("/{username}/actualizar-multas")
  public String actualizarMultas(
      @PathVariable String username,
      @RequestParam Integer nuevoValor,
      RedirectAttributes redirectAttributes) {

    try {
      lectorService.actualizarMultasPendientes(username, nuevoValor);
      redirectAttributes.addFlashAttribute("mensaje",
          "Multas pendientes actualizadas a: " + nuevoValor);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
    }

    return "redirect:/admin/lectores/" + username;
  }
}