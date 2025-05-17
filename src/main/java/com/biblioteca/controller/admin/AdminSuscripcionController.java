package com.biblioteca.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.biblioteca.service.SuscripcionService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/suscripciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSuscripcionController {

  private final SuscripcionService suscripcionService;

  @GetMapping
  public String listarTodasLasSuscripciones(Model model) {
    model.addAttribute("suscripciones", suscripcionService.obtenerTodasLasSuscripciones());
    model.addAttribute("activeTab", "suscripciones");
    return "admin/suscripciones/lista";
  }

  @GetMapping("/por-vencer")
  public String listarSuscripcionesPorVencer(
      @RequestParam(defaultValue = "7") int dias,
      Model model) {

    model.addAttribute("suscripciones", suscripcionService.obtenerSuscripcionesPorVencer(dias));
    model.addAttribute("diasRestantes", dias);
    model.addAttribute("activeTab", "suscripciones");
    return "admin/suscripciones/por-vencer";
  }

  @GetMapping("/vencidas")
  public String listarSuscripcionesVencidas(Model model) {
    model.addAttribute("suscripciones", suscripcionService.obtenerSuscripcionesVencidas());
    model.addAttribute("activeTab", "suscripciones");
    return "admin/suscripciones/vencidas";
  }
}