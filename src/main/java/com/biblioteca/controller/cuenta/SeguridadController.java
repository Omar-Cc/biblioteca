package com.biblioteca.controller.cuenta;

import com.biblioteca.dto.SessionInfo;
import com.biblioteca.models.Usuario;
import com.biblioteca.service.SessionManagementService;
import com.biblioteca.service.UsuarioService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/mi-cuenta/seguridad")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'LECTOR')")
public class SeguridadController {

  private final UsuarioService usuarioService;
  private final SessionManagementService sessionManagementService;

  @GetMapping
  public String mostrarSeguridad(Model model, Principal principal, HttpSession currentSession) {
    try {
      String username = principal.getName();

      // Cargar datos del usuario para mostrar información de seguridad
      Usuario usuario = usuarioService.buscarPorUsername(username)
          .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

      // Obtener la información de las sesiones activas
      List<SessionInfo> sesiones = sessionManagementService.getUserSessions(username);

      // Añadir ayudantes para formatear el tiempo transcurrido
      model.addAttribute("helper", new SessionDisplayHelper());

      model.addAttribute("usuario", usuario);
      model.addAttribute("sesiones", sesiones);
      model.addAttribute("sesionActualId", currentSession.getId());
      model.addAttribute("ultimoPasswordChange",
          usuario.getUltimaActualizacionPassword() != null ? usuario.getUltimaActualizacionPassword()
              : usuario.getFechaRegistro());

      return "usuarios/seguridad";
    } catch (Exception e) {
      model.addAttribute("errorMessage", "No se pudo cargar la información de seguridad: " + e.getMessage());
      return "error";
    }
  }

  @PostMapping("/cerrar-sesiones")
  public String cerrarTodasLasSesiones(HttpSession currentSession, Principal principal,
      RedirectAttributes redirectAttributes) {
    try {
      String username = principal.getName();
      int sesioncesCerradas = sessionManagementService.invalidateAllSessionsExcept(username, currentSession.getId());

      String mensaje = sesioncesCerradas > 0
          ? "Se han cerrado " + sesioncesCerradas + " sesiones en otros dispositivos."
          : "No había otras sesiones activas para cerrar.";

      redirectAttributes.addFlashAttribute("successMessage", mensaje);
      return "redirect:/mi-cuenta/seguridad";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error al cerrar sesiones: " + e.getMessage());
      return "redirect:/mi-cuenta/seguridad";
    }
  }

  @PostMapping("/cerrar-sesion")
  public String cerrarSesionEspecifica(@RequestParam String sessionId, Principal principal,
      HttpSession currentSession, RedirectAttributes redirectAttributes) {
    try {
      String username = principal.getName();

      // No permitir cerrar la sesión actual desde esta ruta
      if (sessionId.equals(currentSession.getId())) {
        redirectAttributes.addFlashAttribute("errorMessage",
            "No puedes cerrar tu sesión actual desde aquí. Utiliza la opción 'Cerrar sesión'.");
        return "redirect:/mi-cuenta/seguridad";
      }

      boolean exito = sessionManagementService.invalidateSession(username, sessionId);

      if (exito) {
        redirectAttributes.addFlashAttribute("successMessage", "La sesión ha sido cerrada correctamente");
      } else {
        redirectAttributes.addFlashAttribute("errorMessage", "No se encontró la sesión especificada");
      }

      return "redirect:/mi-cuenta/seguridad";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error al cerrar la sesión: " + e.getMessage());
      return "redirect:/mi-cuenta/seguridad";
    }
  }

  @PostMapping("/eliminar-cuenta")
  public String eliminarCuenta(
      @RequestParam String confirmText,
      @RequestParam String password,
      Principal principal,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    if (!"ELIMINAR".equals(confirmText)) {
      redirectAttributes.addFlashAttribute("errorMessage", "Texto de confirmación incorrecto");
      return "redirect:/mi-cuenta/seguridad";
    }

    try {
      String username = principal.getName();
      boolean eliminacionExitosa = usuarioService.eliminarCuenta(username, password);

      if (eliminacionExitosa) {
        // Invalidar la sesión
        session.invalidate();
        return "redirect:/login?accountDeleted=true";
      } else {
        redirectAttributes.addFlashAttribute("errorMessage",
            "La contraseña es incorrecta o no se pudo eliminar la cuenta");
        return "redirect:/mi-cuenta/seguridad";
      }
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la cuenta: " + e.getMessage());
      return "redirect:/mi-cuenta/seguridad";
    }
  }

  // Clase auxiliar para formatear tiempo de actividad
  public static class SessionDisplayHelper {
    public String formatLastActivity(LocalDateTime lastActivity, String sessionId, String currentSessionId) {
      if (sessionId.equals(currentSessionId)) {
        return "Actual";
      }

      LocalDateTime now = LocalDateTime.now();
      long days = ChronoUnit.DAYS.between(lastActivity, now);
      long hours = ChronoUnit.HOURS.between(lastActivity, now);
      long minutes = ChronoUnit.MINUTES.between(lastActivity, now);

      if (days > 0) {
        return "Hace " + days + (days == 1 ? " día" : " días");
      } else if (hours > 0) {
        return "Hace " + hours + (hours == 1 ? " hora" : " horas");
      } else if (minutes > 0) {
        return "Hace " + minutes + (minutes == 1 ? " minuto" : " minutos");
      } else {
        return "Hace unos segundos";
      }
    }
  }
}