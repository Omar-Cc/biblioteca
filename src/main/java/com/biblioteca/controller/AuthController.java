package com.biblioteca.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca.dto.UsuarioRegistroDTO;
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
      @ModelAttribute("exito") String exito) { // Para flash attribute de registro
    if (error != null) {
      // Usar un atributo específico para no colisionar
      model.addAttribute("loginError", "Usuario o contraseña incorrectos.");
    }
    if (logout != null) {
      model.addAttribute("logoutMessage", "Has cerrado sesión correctamente.");
    }
    if (exito != null && !exito.isEmpty()) { // Mostrar mensaje de éxito de registro
      model.addAttribute("registroExitoso", exito);
    }
    return "auth/login";
  }

  @GetMapping("/login-success")
  public String loginSuccess() {
    return "redirect:/mi-cuenta/perfiles/seleccionar";
  }

  @GetMapping("/registro")
  public String mostrarPaginaRegistro(Model model) {
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
      model.addAttribute("usuarioRegistroDTO", registroDTO);
      return "registro";
    }

    try {
      usuarioService.registrarUsuario(registroDTO);
      redirectAttributes.addFlashAttribute("exito", "¡Registro exitoso! Ahora puedes iniciar sesión.");
      return "redirect:/login";
    } catch (IllegalArgumentException e) {
      model.addAttribute("usuarioRegistroDTO", registroDTO);
      model.addAttribute("registroError", e.getMessage());
      return "registro";
    } catch (Exception e) {
      model.addAttribute("usuarioRegistroDTO", registroDTO);
      model.addAttribute("registroError", "Ocurrió un error inesperado durante el registro.");
      e.printStackTrace();
      return "registro";
    }
  }

  @GetMapping("/403")
  public String mostrarPagina403() {
    return "403";
  }

  @GetMapping("/404")
  public String mostrarPagina404() {
    return "404";
  }

  @GetMapping("/500")
  public String mostrarPagina500() {
    return "500";
  }

  @GetMapping("/error")
  public String mostrarPaginaError() {
    return "error";
  }

}
