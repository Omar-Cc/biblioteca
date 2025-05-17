package com.biblioteca.controller.cuenta;

import com.biblioteca.dto.PerfilRequestDTO;
import com.biblioteca.dto.PerfilResponseDTO;
import com.biblioteca.service.PerfilService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mi-cuenta/perfiles")
@RequiredArgsConstructor
public class PerfilesUsuarioController {

  private final PerfilService perfilService;

  private String getCurrentUsername(Principal principal) {
    if (principal == null) {
      throw new UsernameNotFoundException("Usuario no autenticado.");
    }
    return principal.getName();
  }

  @GetMapping
  public String listarPerfiles(Model model, Principal principal, RedirectAttributes redirectAttributes,
      HttpSession session) {
    try {
      // Verificar que haya un perfil activo
      if (session.getAttribute("perfilActivoId") == null) {
        return "redirect:/mi-cuenta/perfiles/seleccionar";
      }

      String username = getCurrentUsername(principal);
      List<PerfilResponseDTO> listaPerfiles = perfilService.obtenerPerfilesPorUsuario(username);
      model.addAttribute("perfiles", listaPerfiles);

      if (listaPerfiles.isEmpty()) {
        model.addAttribute("infoMessage", "Aún no tienes perfiles. ¡Crea uno para empezar!");
      }

      configurarNavbar(model);

      return "public/perfil/lista-perfiles";
    } catch (UsernameNotFoundException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para ver tus perfiles.");
      return "redirect:/login";
    }
  }

  @GetMapping("/nuevo")
  public String mostrarFormularioNuevoPerfil(Model model, Principal principal, RedirectAttributes redirectAttributes) {
    try {
      String username = getCurrentUsername(principal);
      perfilService.contarPerfilesPorUsuario(username);

      // Verificar si ya existe un perfil principal
      boolean existePerfilPrincipal = perfilService.existePerfilPrincipal(username);
      model.addAttribute("existePerfilPrincipal", existePerfilPrincipal);
      model.addAttribute("esEdicionPerfilPrincipal", false);

      model.addAttribute("perfilRequestDTO", new PerfilRequestDTO());
      model.addAttribute("pageTitle", "Crear Nuevo Perfil");
      model.addAttribute("formAction", "/mi-cuenta/perfiles/crear");

      configurarNavbar(model);

      return "public/perfil/form-perfil";
    } catch (UsernameNotFoundException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para crear un perfil.");
      return "redirect:/login";
    }
  }

  @PostMapping("/crear")
  public String crearPerfil(@Valid @ModelAttribute("perfilRequestDTO") PerfilRequestDTO perfilDto,
      BindingResult result, Principal principal, RedirectAttributes redirectAttributes, Model model) {

    String username;

    try {
      username = getCurrentUsername(principal);
      if (result.hasErrors()) {
        // Añadir estas líneas para inicializar las variables necesarias
        boolean existePerfilPrincipal = perfilService.existePerfilPrincipal(username);
        model.addAttribute("existePerfilPrincipal", existePerfilPrincipal);
        model.addAttribute("esEdicionPerfilPrincipal", false);

        model.addAttribute("pageTitle", "Crear Nuevo Perfil");
        model.addAttribute("formAction", "/mi-cuenta/perfiles/crear");
        configurarNavbar(model);

        System.out.println("Errores en el formulario: " + result.getAllErrors());

        return "public/perfil/form-perfil";
      }

      PerfilResponseDTO perfilCreado = perfilService.crearPerfil(perfilDto, username);
      redirectAttributes.addFlashAttribute("successMessage",
          "Perfil '" + perfilCreado.getNombreVisible() + "' creado exitosamente.");

      // Redireccionar a selección de perfiles después de crear uno nuevo
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    } catch (UsernameNotFoundException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error de autenticación.");
      return "redirect:/login";
    } catch (IllegalArgumentException e) {
      // Establecer un valor por defecto para existePerfilPrincipal
      System.out.println("Error: " + e.getMessage());
      model.addAttribute("existePerfilPrincipal", false);
      model.addAttribute("esEdicionPerfilPrincipal", false);
      model.addAttribute("errorMessage", e.getMessage());
      model.addAttribute("pageTitle", "Crear Nuevo Perfil");
      model.addAttribute("formAction", "/mi-cuenta/perfiles/crear");
      configurarNavbar(model);
      return "public/perfil/form-perfil";
    }
  }

  @GetMapping("/{id}/editar")
  public String mostrarFormularioEditarPerfil(@PathVariable Long id, Model model, Principal principal,
      RedirectAttributes redirectAttributes) {
    try {
      String username = getCurrentUsername(principal);
      PerfilResponseDTO perfil = perfilService.obtenerPerfilPorIdYUsuario(id, username)
          .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado o no pertenece al usuario."));

      // Verificar si ya existe un perfil principal
      boolean existePerfilPrincipal = perfilService.existePerfilPrincipal(username);
      model.addAttribute("existePerfilPrincipal", existePerfilPrincipal);
      // Si el perfil que editamos es el principal, permitir editar ese checkbox
      model.addAttribute("esEdicionPerfilPrincipal", perfil.isEsPerfilPrincipal());

      PerfilRequestDTO requestDTO = new PerfilRequestDTO();
      requestDTO.setNombreVisible(perfil.getNombreVisible());
      requestDTO.setFotoPerfil(perfil.getFotoPerfil());
      requestDTO.setIdiomaPreferido(perfil.getIdiomaPreferido());
      requestDTO.setLimitePrestamosDesignado(perfil.getLimitePrestamosDesignado());
      requestDTO.setEsInfantil(perfil.isEsInfantil());
      requestDTO.setEsPerfilPrincipal(perfil.isEsPerfilPrincipal());

      model.addAttribute("perfilRequestDTO", requestDTO);
      model.addAttribute("perfilId", id);
      model.addAttribute("pageTitle", "Editar Perfil: " + perfil.getNombreVisible());
      model.addAttribute("formAction", "/perfil/" + id + "/actualizar");

      return "public/perfil/form-perfil";
    } catch (UsernameNotFoundException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error de autenticación.");
      return "redirect:/login";
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return "redirect:/perfil";
    }
  }

  @PostMapping("/{id}/actualizar")
  public String actualizarPerfil(@PathVariable Long id,
      @Valid @ModelAttribute("perfilRequestDTO") PerfilRequestDTO perfilDto,
      BindingResult result, Principal principal, RedirectAttributes redirectAttributes, Model model) {
    try {
      String username = getCurrentUsername(principal);
      if (result.hasErrors()) {
        // Obtener el perfil actual para saber si es principal
        PerfilResponseDTO perfil = perfilService.obtenerPerfilPorIdYUsuario(id, username)
            .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado"));

        boolean existePerfilPrincipal = perfilService.existePerfilPrincipal(username);
        model.addAttribute("existePerfilPrincipal", existePerfilPrincipal);
        model.addAttribute("esEdicionPerfilPrincipal", perfil.isEsPerfilPrincipal());
        model.addAttribute("perfilId", id);
        model.addAttribute("pageTitle", "Editar Perfil");
        model.addAttribute("formAction", "/perfil/" + id + "/actualizar");
        configurarNavbar(model);
        return "public/perfil/form-perfil";
      }
      perfilService.actualizarPerfil(id, perfilDto, username);
      redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado exitosamente.");
      return "redirect:/perfil";
    } catch (UsernameNotFoundException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error de autenticación.");
      return "redirect:/login";
    } catch (IllegalArgumentException e) {
      model.addAttribute("errorMessage", e.getMessage());
      model.addAttribute("perfilId", id);
      model.addAttribute("pageTitle", "Editar Perfil");
      model.addAttribute("formAction", "/perfil/" + id + "/actualizar");
      return "perfil/form-perfil";
    }
  }

  @PostMapping("/{id}/eliminar")
  public String eliminarPerfil(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
    try {
      String username = getCurrentUsername(principal);
      if (perfilService.eliminarPerfil(id, username)) {
        redirectAttributes.addFlashAttribute("successMessage", "Perfil eliminado exitosamente.");
      } else {
        redirectAttributes.addFlashAttribute("errorMessage", "No se pudo eliminar el perfil o no fue encontrado.");
      }
    } catch (UsernameNotFoundException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error de autenticación.");
      return "redirect:/login";
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    }
    return "redirect:/perfil";
  }

  // Método para mostrar la página de selección de perfiles estilo Netflix
  @GetMapping("/seleccionar")
  public String seleccionarPerfil(Model model, Principal principal, RedirectAttributes redirectAttributes) {
    try {
      String username = getCurrentUsername(principal);
      List<PerfilResponseDTO> listaPerfiles = perfilService.obtenerPerfilesPorUsuario(username);
      model.addAttribute("perfiles", listaPerfiles);

      boolean noHayPerfiles = listaPerfiles.isEmpty();
      model.addAttribute("sinPerfiles", noHayPerfiles);

      // Si no hay perfiles y quieres redirigir a crear uno
      if (noHayPerfiles) {
        model.addAttribute("infoMessage", "Necesitas crear al menos un perfil para usar la plataforma.");
        /*
         * redirectAttributes.addFlashAttribute("infoMessage",
         * "Necesitas crear al menos un perfil para usar la plataforma.");
         * return "redirect:/perfil/nuevo";
         */
      }

      configurarNavbar(model);

      return "public/perfil/seleccionar-perfil";
    } catch (UsernameNotFoundException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para ver tus perfiles.");
      return "redirect:/login";
    }
  }

  // Método para activar un perfil
  @PostMapping("/{id}/activar")
  public String activarPerfil(@PathVariable Long id, Principal principal,
      HttpSession session, RedirectAttributes redirectAttributes) {
    try {
      String username = getCurrentUsername(principal);
      PerfilResponseDTO perfil = perfilService.obtenerPerfilPorIdYUsuario(id, username)
          .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado o no pertenece al usuario."));

      // Actualizar la fecha de última actividad
      perfilService.actualizarFechaUltimoActividad(id);

      // Guardar el perfil activo en la sesión
      session.setAttribute("perfilActivo", perfil);
      session.setAttribute("perfilActivoId", perfil.getId());
      session.setAttribute("perfilActivoNombre", perfil.getNombreVisible());
      session.setAttribute("esPerfilPrincipal", perfil.isEsPerfilPrincipal());

      return "redirect:/?perfilActivado=true";
    } catch (UsernameNotFoundException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error de autenticación.");
      return "redirect:/login";
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }
  }

  // Método auxiliar para configurar navbar en todas las vistas
  private void configurarNavbar(Model model) {
    model.addAttribute("activeTab", "perfil");
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
      model.addAttribute("carritoItems", 0);
    }
  }
}