package com.biblioteca.controller;

import com.biblioteca.models.Usuario;
import com.biblioteca.service.PerfilService;
import com.biblioteca.service.UsuarioService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

  private final PerfilService perfilService;
  private final UsuarioService usuarioService;

  /**
   * Método para agregar atributos globales al modelo.
   * 
   * @param session La sesión HTTP actual.
   * @return El número total de perfiles del usuario autenticado.
   */
  @ModelAttribute("totalPerfiles")
  public Long getTotalPerfiles(HttpSession session) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    // Verificar si el usuario está autenticado y no es anónimo
    if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
      String username = auth.getName();
      return perfilService.contarPerfilesPorUsuario(username);
    }

    return 0L; // Valor predeterminado cuando no hay usuario autenticado
  }

  /**
   * Método para obtener la foto de perfil del usuario autenticado.
   * 
   * @param session La sesión HTTP actual.
   * @return La URL de la foto de perfil del usuario autenticado.
   */
  @ModelAttribute("fotoPerfil")
  public String getFotoPerfil(HttpSession session) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    // Verificar si el usuario está autenticado y no es anónimo
    if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
      String username = auth.getName();

      // Obtener el ID del perfil activo desde la sesión
      Long perfilActivoId = (Long) session.getAttribute("perfilActivoId");

      // Si hay un perfil activo, obtener su foto
      if (perfilActivoId != null) {
        String fotoPerfil = perfilService.obtenerPerfilPorIdYUsuario(perfilActivoId, username)
            .map(perfil -> perfil.getFotoPerfil())
            .orElse(null);

        // Asegurarnos de que la foto no sea una cadena vacía
        return (fotoPerfil != null && !fotoPerfil.isEmpty()) ? fotoPerfil : null;
      } else {
        // Si no hay perfil activo, intentar obtener el perfil principal
        String fotoPerfil = perfilService.obtenerPerfilPrincipal(username)
            .map(perfil -> perfil.getFotoPerfil())
            .orElse(null);

        // Asegurarnos de que la foto no sea una cadena vacía
        return (fotoPerfil != null && !fotoPerfil.isEmpty()) ? fotoPerfil : null;
      }
    }

    return null; // Valor predeterminado cuando no hay usuario autenticado
  }

  @ModelAttribute("fechaAntiguedad")
  public String getFechaAntiguedad(HttpSession session) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    // Verificar si el usuario está autenticado y no es anónimo
    if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
      String username = auth.getName();
      Usuario usuario = usuarioService.buscarPorUsername(username)
          .orElse(null);

      if (usuario != null && usuario.getFechaRegistro() != null) {
        // Usar DateTimeFormatter para formatear la fecha
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        return usuario.getFechaRegistro().format(formatter);
      }
    }

    return null; // Valor predeterminado cuando no hay usuario autenticado
  }

}