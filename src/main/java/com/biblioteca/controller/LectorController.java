package com.biblioteca.controller;
/* 
import java.security.Principal;
import java.util.List;

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

import com.biblioteca.dto.LectorRequestDTO;
import com.biblioteca.dto.LectorResponseDTO;
import com.biblioteca.service.LectorService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/lectores") */
public class LectorController {
/* 
  private final LectorService lectorService;

  public LectorController(LectorService lectorService) {
    this.lectorService = lectorService;
  }

  /**
   * Muestra el formulario para completar o actualizar el perfil de lector
   *
  @GetMapping("/perfil")
  @PreAuthorize("hasAnyRole('USER', 'LECTOR')")
  public String mostrarFormularioLector(Model model, Principal principal) {
    String username = principal.getName();

    // Verificar si ya existe un perfil de lector para este usuario
    if (lectorService.tienePerfilLector(username)) {
      // Si existe, cargar los datos actuales para edición
      LectorResponseDTO lectorDTO = lectorService.obtenerPerfilLectorPorUsername(username)
          .orElseThrow(() -> new IllegalStateException("Error al recuperar datos del lector"));

      // Convertir a RequestDTO para el formulario
      LectorRequestDTO requestDTO = new LectorRequestDTO();
      requestDTO.setNombre(lectorDTO.getNombre());
      requestDTO.setApellido(lectorDTO.getApellido());
      requestDTO.setDireccion(lectorDTO.getDireccion());
      requestDTO.setTelefono(lectorDTO.getTelefono());
      requestDTO.setFechaNacimiento(lectorDTO.getFechaNacimiento());

      model.addAttribute("lectorDTO", requestDTO);
      model.addAttribute("modoEdicion", true);
      model.addAttribute("estadoCuenta", lectorDTO.getEstadoCuenta());
      model.addAttribute("multasPendientes", lectorDTO.getMultasPendientes());
    } else {
      // Si no existe, mostrar formulario vacío para creación
      model.addAttribute("lectorDTO", new LectorRequestDTO());
      model.addAttribute("modoEdicion", false);
    }

    return "lectores/lector-form";
  }

  /**
   * Procesa la creación o actualización del perfil de lector
   *
  @PostMapping("/perfil")
  @PreAuthorize("hasAnyRole('USER', 'LECTOR')")
  public String procesarFormularioLector(
      @Valid @ModelAttribute("lectorDTO") LectorRequestDTO lectorDTO,
      BindingResult result,
      Principal principal,
      RedirectAttributes redirectAttributes,
      Model model) {

    if (result.hasErrors()) {
      model.addAttribute("modoEdicion", lectorService.tienePerfilLector(principal.getName()));
      return "lectores/lector-form";
    }

    try {
      String username = principal.getName();
      LectorResponseDTO responseDTO;

      if (lectorService.tienePerfilLector(username)) {
        responseDTO = lectorService.actualizarPerfilLector(username, lectorDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Perfil de lector actualizado correctamente");
      } else {
        responseDTO = lectorService.crearPerfilLector(username, lectorDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Perfil de lector creado correctamente");
      }

      return "redirect:/mi-cuenta";
    } catch (Exception e) {
      model.addAttribute("errorMessage", "Error al procesar el perfil: " + e.getMessage());
      model.addAttribute("modoEdicion", lectorService.tienePerfilLector(principal.getName()));
      return "lectores/lector-form";
    }
  }

  /**
   * Verifica si el usuario tiene perfil de lector y redirecciona apropiadamente
   *
  @GetMapping("/verificar")
  @PreAuthorize("hasAnyRole('USER', 'LECTOR')")
  public String verificarPerfilLector(
      @RequestParam(required = false) String redirectTo,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal.getName();

    if (!lectorService.tienePerfilLector(username)) {
      redirectAttributes.addFlashAttribute("warningMessage",
          "Para continuar, necesitas completar tu perfil de lector primero.");
      redirectAttributes.addAttribute("redirectTo", redirectTo != null ? redirectTo : "/");
      return "redirect:/lectores/perfil";
    }

    return "redirect:" + (redirectTo != null ? redirectTo : "/");
  }

  /**
   * Listar todos los lectores (admin)
   *
  @GetMapping("/admin/listar")
  @PreAuthorize("hasRole('ADMIN')")
  public String listarLectores(Model model) {
    List<LectorResponseDTO> lectores = lectorService.listarTodosLosLectores();
    model.addAttribute("lectores", lectores);
    return "admin/lista-lectores";
  }

  /**
   * Detalles de un lector específico (admin)
   *
  @GetMapping("/admin/detalle/{username}")
  @PreAuthorize("hasRole('ADMIN')")
  public String detallesLector(@PathVariable String username, Model model, RedirectAttributes redirectAttributes) {
    try {
      LectorResponseDTO lector = lectorService.obtenerPerfilLectorPorUsername(username)
          .orElseThrow(() -> new IllegalArgumentException("Lector no encontrado: " + username));

      model.addAttribute("lector", lector);
      return "admin/detalle-lector";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
      return "redirect:/lectores/admin/listar";
    }
  }

  /**
   * Cambiar estado de un lector (admin)
   *
  @PostMapping("/admin/cambiar-estado/{username}")
  @PreAuthorize("hasRole('ADMIN')")
  public String cambiarEstadoLector(
      @PathVariable String username,
      @RequestParam String nuevoEstado,
      RedirectAttributes redirectAttributes) {

    try {
      lectorService.cambiarEstadoLector(username, nuevoEstado);
      redirectAttributes.addFlashAttribute("successMessage",
          "Estado del lector actualizado a: " + nuevoEstado);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
    }

    return "redirect:/lectores/admin/detalle/" + username;
  }

  /**
   * Actualizar multas pendientes (admin)
   *
  @PostMapping("/admin/actualizar-multas/{username}")
  @PreAuthorize("hasRole('ADMIN')")
  public String actualizarMultas(
      @PathVariable String username,
      @RequestParam Integer nuevoValor,
      RedirectAttributes redirectAttributes) {

    try {
      lectorService.actualizarMultasPendientes(username, nuevoValor);
      redirectAttributes.addFlashAttribute("successMessage",
          "Multas pendientes actualizadas a: " + nuevoValor);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
    }

    return "redirect:/lectores/admin/detalle/" + username;
  } 
  */
}