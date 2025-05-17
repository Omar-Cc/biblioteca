package com.biblioteca.controller.cuenta;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca.dto.LectorRequestDTO;
import com.biblioteca.dto.LectorResponseDTO;
import com.biblioteca.service.LectorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mi-cuenta/lector")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'LECTOR')")
public class DatosLectorController {

  private final LectorService lectorService;

  /**
   * Muestra el formulario para completar o actualizar el perfil de lector
   */
  @GetMapping
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
   */
  @PostMapping
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

      if (lectorService.tienePerfilLector(username)) {
        lectorService.actualizarPerfilLector(username, lectorDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Perfil de lector actualizado correctamente");
      } else {
        lectorService.crearPerfilLector(username, lectorDTO);
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
   */
  @GetMapping("/verificar")
  public String verificarPerfilLector(
      @RequestParam(required = false) String redirectTo,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal.getName();

    if (!lectorService.tienePerfilLector(username)) {
      redirectAttributes.addFlashAttribute("warningMessage",
          "Para continuar, necesitas completar tu perfil de lector primero.");
      redirectAttributes.addAttribute("redirectTo", redirectTo != null ? redirectTo : "/");
      return "redirect:/mi-cuenta/lector";
    }

    return "redirect:" + (redirectTo != null ? redirectTo : "/");
  }
}