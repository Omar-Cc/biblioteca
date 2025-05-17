package com.biblioteca.controller;

import com.biblioteca.dto.ContenidoResponseDTO;
import com.biblioteca.service.ContenidoService;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
public class HomeController {

  private final ContenidoService contenidoService;

  public HomeController(ContenidoService contenidoService) {
    this.contenidoService = contenidoService;
  }

  @GetMapping("/")
  public String index(Model model, HttpSession session,
      @RequestParam(required = false) String perfilActivado) {
    // Verificar autenticación
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    // Verificar si venimos de activar un perfil
    if ("true".equals(perfilActivado)) {
      // Verificar que haya un perfil activo
      Object perfilActivoId = session.getAttribute("perfilActivoId");
      if (perfilActivoId != null) {
        // Reforzar la persistencia de la sesión
        session.setAttribute("timestampHome", System.currentTimeMillis());
      } else {
        System.out.println("ADVERTENCIA: Perfil no encontrado en sesión después de activación");
      }
    }

    // Cargar contenidos destacados y configurar navbar
    cargarContenidosDestacados(model);
    configurarNavbar(model, auth);

    return "public/index";
  }

  // Métodos auxiliares para separar responsabilidades
  private void cargarContenidosDestacados(Model model) {
    try {
      List<ContenidoResponseDTO> contenidosDestacados = contenidoService.obtenerContenidosDestacados();
      model.addAttribute("contenidosDestacados", contenidosDestacados);
    } catch (Exception e) {
      model.addAttribute("contenidosDestacados", Collections.emptyList());
      model.addAttribute("errorMessage", "No se pudieron cargar los contenidos destacados");
    }
  }

  private void configurarNavbar(Model model, Authentication auth) {
    // Obtener información del carrito si el usuario está autenticado
    if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
      try {
        model.addAttribute("carritoItems", 0);
      } catch (Exception e) {
        model.addAttribute("carritoItems", 0);
        model.addAttribute("errorMessage", "No se pudo cargar el carrito");
      }
    }

    model.addAttribute("activeTab", "home");
  }
}