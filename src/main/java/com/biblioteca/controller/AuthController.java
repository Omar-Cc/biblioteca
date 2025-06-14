package com.biblioteca.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca.dto.UsuarioRegistroDTO;
import com.biblioteca.models.acceso.Usuario;
import com.biblioteca.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
public class AuthController {

  private final UsuarioService usuarioService;

  public AuthController(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @GetMapping("/login")
  public String mostrarPaginaLogin(Model model,
      @RequestParam(value = "error", required = false) String error,
      @RequestParam(value = "logout", required = false) String logout,
      @ModelAttribute("exito") String exito) {

    // Verificar si el usuario ya está autenticado
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    if (error != null) {
      model.addAttribute("loginError", "Usuario o contraseña incorrectos.");
    }
    if (logout != null) {
      model.addAttribute("logoutMessage", "Has cerrado sesión correctamente.");
    }
    if (exito != null && !exito.isEmpty()) {
      model.addAttribute("registroExitoso", exito);
    }
    return "auth/login";
  }

  @GetMapping("/registro")
  public String mostrarPaginaRegistro(Model model) {
    // Verificar si el usuario ya está autenticado
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    if (!model.containsAttribute("usuarioRegistroDTO")) {
      model.addAttribute("usuarioRegistroDTO", new UsuarioRegistroDTO());
    }
    return "auth/registro";
  }

  @PostMapping("/registro")
  public String procesarRegistro(
      @Valid @ModelAttribute("usuarioRegistroDTO") UsuarioRegistroDTO registroDTO,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      Model model) {

    System.out.println("=== PROCESANDO REGISTRO ===");
    System.out.println("Username: " + registroDTO.getUsername());
    System.out.println("Email: " + registroDTO.getEmail());
    System.out.println("Errores de binding: " + bindingResult.hasErrors());

    if (!registroDTO.getPassword().equals(registroDTO.getConfirmPassword())) {
      bindingResult.rejectValue("confirmPassword", "password.mismatch", "Las contraseñas no coinciden.");
    }

    if (usuarioService.buscarPorUsername(registroDTO.getUsername()).isPresent()) {
      bindingResult.rejectValue("username", "username.exists", "El nombre de usuario ya está en uso.");
    }

    if (usuarioService.buscarPorEmail(registroDTO.getEmail()).isPresent()) {
      bindingResult.rejectValue("email", "email.exists", "El correo electrónico ya está registrado.");
    }

    if (bindingResult.hasErrors()) {
      System.out.println("❌ Errores de validación:");
      bindingResult.getAllErrors().forEach(error -> System.out.println("  - " + error.getDefaultMessage()));
      model.addAttribute("usuarioRegistroDTO", registroDTO);
      return "auth/registro";
    }

    try {
      Usuario usuario = usuarioService.registrarUsuario(registroDTO);
      System.out.println("✅ Usuario registrado exitosamente: " + usuario.getUsername());
      redirectAttributes.addFlashAttribute("exito", "¡Registro exitoso! Ahora puedes iniciar sesión.");
      return "redirect:/login";
    } catch (Exception e) {
      System.out.println("❌ Error en registro: " + e.getMessage());
      e.printStackTrace();
      model.addAttribute("usuarioRegistroDTO", registroDTO);
      model.addAttribute("registroError", "Error al registrar usuario: " + e.getMessage());
      return "auth/registro";
    }
  }

  @GetMapping("/403")
  public String mostrarPagina403() {
    return "error/403";
  }

  @GetMapping("/404")
  public String mostrarPagina404() {
    return "error/404";
  }

  @GetMapping("/500")
  public String mostrarPagina500() {
    return "error/500";
  }

  @GetMapping("/error")
  public String mostrarPaginaError() {
    return "error/error";
  }

}
