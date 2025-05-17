package com.biblioteca.controller.cuenta;

import com.biblioteca.dto.LectorRequestDTO;
import com.biblioteca.dto.UsuarioCombinedDTO;
import com.biblioteca.dto.UsuarioDataDTO;
import com.biblioteca.dto.UsuarioPasswordDTO;
import com.biblioteca.models.Usuario;
import com.biblioteca.service.LectorService;
import com.biblioteca.service.UsuarioService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/mi-cuenta")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'LECTOR')")
public class MiCuentaController {

  private final UsuarioService usuarioService;
  private final LectorService lectorService;

  @GetMapping
  public String mostrarCuenta(Model model, Principal principal, HttpSession session) {
    try {
      String username = principal.getName();
      // Cargar datos del usuario
      Usuario usuario = usuarioService.buscarPorUsername(username)
          .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
      model.addAttribute("usuario", usuario);

      // Cargar datos del lector si existen
      lectorService.obtenerPerfilLectorPorUsername(username).ifPresent(lector -> {
        model.addAttribute("lector", lector);
      });

      return "usuarios/mi-cuenta";
    } catch (Exception e) {
      model.addAttribute("error", "No se pudo cargar la información de tu cuenta: " + e.getMessage());
      return "error";
    }
  }

  @GetMapping("/editar")
  public String mostrarFormularioEditarCombinado(Model model, Principal principal, HttpSession session) {
    try {
      // Verificar si es perfil principal
      Boolean esPerfilPrincipal = (Boolean) session.getAttribute("esPerfilPrincipal");
      if (esPerfilPrincipal != null && !esPerfilPrincipal) {
        return "redirect:/mi-cuenta";
      }

      String username = principal.getName();
      Usuario usuario = usuarioService.buscarPorUsername(username)
          .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

      // Crear DTO combinado
      UsuarioCombinedDTO combinedDTO = new UsuarioCombinedDTO();
      combinedDTO.setEmail(usuario.getEmail());

      // Agregar datos del lector si existen
      lectorService.obtenerPerfilLectorPorUsername(username).ifPresent(lector -> {
        LectorRequestDTO lectorDTO = new LectorRequestDTO();
        lectorDTO.setNombre(lector.getNombre());
        lectorDTO.setApellido(lector.getApellido());
        lectorDTO.setDireccion(lector.getDireccion());
        lectorDTO.setTelefono(lector.getTelefono());
        lectorDTO.setFechaNacimiento(lector.getFechaNacimiento());

        combinedDTO.setLectorDTO(lectorDTO);
      });

      model.addAttribute("usuarioDataDTO", combinedDTO);
      return "usuarios/editar-cuenta";
    } catch (Exception e) {
      model.addAttribute("error", "No se pudo cargar el formulario: " + e.getMessage());
      return "error";
    }
  }

  @PostMapping("/editar")
  public String procesarEdicionCombinada(
      @Valid @ModelAttribute("usuarioDataDTO") UsuarioCombinedDTO combinedDTO,
      BindingResult result,
      Principal principal,
      RedirectAttributes redirectAttributes,
      Model model) {

    String username = principal.getName();

    // Validar email único si ha cambiado
    usuarioService.buscarPorEmail(combinedDTO.getEmail())
        .ifPresent(existingUser -> {
          if (!existingUser.getUsername().equals(username)) {
            result.rejectValue("email", "email.exists", "El correo electrónico ya está registrado");
          }
        });

    if (result.hasErrors()) {
      return "usuarios/editar-cuenta";
    }

    try {
      // Actualizar datos del usuario
      UsuarioDataDTO usuarioDTO = new UsuarioDataDTO();
      usuarioDTO.setEmail(combinedDTO.getEmail());
      usuarioService.actualizarUsuario(username, usuarioDTO);

      // Actualizar datos del lector si existen
      if (combinedDTO.getLectorDTO() != null) {
        if (lectorService.tienePerfilLector(username)) {
          lectorService.actualizarPerfilLector(username, combinedDTO.getLectorDTO());
        } else {
          lectorService.crearPerfilLector(username, combinedDTO.getLectorDTO());
        }
      }

      redirectAttributes.addFlashAttribute("successMessage", "Información actualizada correctamente");
      return "redirect:/mi-cuenta";
    } catch (Exception e) {
      model.addAttribute("errorMessage", "Error al actualizar: " + e.getMessage());
      return "usuarios/editar-cuenta";
    }
  }

  @GetMapping("/cambiar-password")
  public String mostrarFormularioCambioPassword(Model model) {
    model.addAttribute("usuarioPasswordDTO", new UsuarioPasswordDTO());
    return "usuarios/cambiar-password";
  }

  @PostMapping("/cambiar-password")
  public String procesarCambioPassword(
      @Valid @ModelAttribute("usuarioPasswordDTO") UsuarioPasswordDTO passwordDTO,
      BindingResult result,
      Principal principal,
      RedirectAttributes redirectAttributes,
      Model model) {

    if (!passwordDTO.getNuevaPassword().equals(passwordDTO.getConfirmPassword())) {
      result.rejectValue("confirmPassword", "password.mismatch", "Las contraseñas no coinciden");
    }

    if (result.hasErrors()) {
      return "usuarios/cambiar-password";
    }

    try {
      boolean cambioExitoso = usuarioService.cambiarPassword(
          principal.getName(),
          passwordDTO.getPasswordActual(),
          passwordDTO.getNuevaPassword());

      if (cambioExitoso) {
        redirectAttributes.addFlashAttribute("successMessage", "Contraseña actualizada correctamente");
        return "redirect:/mi-cuenta";
      } else {
        model.addAttribute("errorMessage", "La contraseña actual es incorrecta");
        return "usuarios/cambiar-password";
      }
    } catch (Exception e) {
      model.addAttribute("errorMessage", "Error al cambiar la contraseña: " + e.getMessage());
      return "usuarios/cambiar-password";
    }
  }
}