package com.biblioteca.controller.admin;

import com.biblioteca.dto.UsuarioAdminDTO;
import com.biblioteca.models.Usuario;
import com.biblioteca.service.LectorService;
import com.biblioteca.service.RolService;
import com.biblioteca.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

  private final UsuarioService usuarioService;
  private final RolService rolService;
  private final LectorService lectorService;

  @GetMapping
  public String listarUsuarios(Model model) {
    try {
      model.addAttribute("usuarios", usuarioService.listarTodosLosUsuarios());
      model.addAttribute("activeTab", "usuarios");
      return "admin/lista-usuarios";
    } catch (Exception e) {
      model.addAttribute("error", "Error al cargar la lista de usuarios: " + e.getMessage());
      return "error";
    }
  }

  @PostMapping("/toggle-estado/{id}")
  public String toggleEstadoUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      boolean nuevoEstado = usuarioService.toggleEstadoUsuario(id);
      String mensaje = nuevoEstado ? "Usuario activado correctamente" : "Usuario desactivado correctamente";
      redirectAttributes.addFlashAttribute("successMessage", mensaje);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error al cambiar estado: " + e.getMessage());
    }
    return "redirect:/admin/usuarios";
  }

  @GetMapping("/nuevo")
  public String mostrarFormularioNuevoUsuario(Model model) {
    model.addAttribute("usuarioAdminDTO", new UsuarioAdminDTO());
    model.addAttribute("roles", rolService.obtenerTodosLosRoles());
    model.addAttribute("activeTab", "usuarios");
    return "admin/crear-usuario";
  }

  @PostMapping("/nuevo")
  public String procesarNuevoUsuario(
      @Valid @ModelAttribute("usuarioAdminDTO") UsuarioAdminDTO usuarioDTO,
      BindingResult result,
      RedirectAttributes redirectAttributes,
      Model model) {

    if (result.hasErrors()) {
      model.addAttribute("roles", rolService.obtenerTodosLosRoles());
      return "admin/crear-usuario";
    }

    try {
      usuarioService.crearUsuarioConRoles(usuarioDTO);
      redirectAttributes.addFlashAttribute("successMessage", "Usuario creado exitosamente");
      return "redirect:/admin/usuarios";
    } catch (Exception e) {
      model.addAttribute("errorMessage", e.getMessage());
      model.addAttribute("roles", rolService.obtenerTodosLosRoles());
      return "admin/crear-usuario";
    }
  }

  @GetMapping("/detalle/{id}")
  public String mostrarDetalleUsuario(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    try {
      Usuario usuario = usuarioService.listarTodosLosUsuarios().stream()
          .filter(u -> u.getId().equals(id))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

      model.addAttribute("usuario", usuario);
      model.addAttribute("roles", usuario.getRoles());

      // Verificar si el usuario tiene perfil de lector
      String username = usuario.getUsername();
      if (lectorService.tienePerfilLector(username)) {
        // Obtener datos de lector y añadirlos al modelo
        lectorService.obtenerPerfilLectorPorUsername(username)
            .ifPresent(lectorDTO -> {
              model.addAttribute("lector", lectorDTO);
              model.addAttribute("esLector", true);
            });
      } else {
        model.addAttribute("esLector", false);
      }

      model.addAttribute("activeTab", "usuarios");
      return "admin/detalle-usuario";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error al mostrar detalles: " + e.getMessage());
      return "redirect:/admin/usuarios";
    }
  }
}