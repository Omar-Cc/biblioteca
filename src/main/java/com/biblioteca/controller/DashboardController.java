package com.biblioteca.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.biblioteca.dto.ActividadRecienteDTO;
import com.biblioteca.dto.EstadisticasDTO;
import com.biblioteca.dto.ObraPopularDTO;
import com.biblioteca.service.AutorService;
import com.biblioteca.service.EditorialService;
import com.biblioteca.service.GeneroService;
import com.biblioteca.service.ObraService;
import com.biblioteca.service.UsuarioService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class DashboardController {

  @Autowired
  private UsuarioService usuarioService;

  @Autowired
  private ObraService obraService;

  @Autowired
  private GeneroService generoService;

  @Autowired
  private AutorService autorService;

  @Autowired
  private EditorialService editorialService;

  @GetMapping({ "/dashboard" })
  public String dashboard(Model model) {
    // Obtener estadísticas generales
    EstadisticasDTO estadisticas = new EstadisticasDTO();
    estadisticas.setTotalUsuarios(usuarioService.contarUsuarios());
    estadisticas.setNuevosUsuariosMes(usuarioService.contarUsuariosNuevosMes());
    estadisticas.setTotalObras(obraService.contarObras());
    estadisticas.setNuevasObrasMes(obraService.contarObrasNuevasMes());
    estadisticas.setTotalPrestamos(obraService.contarPrestamos());
    estadisticas.setNuevosPrestamos(obraService.contarPrestamosMes());
    estadisticas.setTotalGeneros(generoService.contarGeneros());

    // Datos para gráficos
    estadisticas.setPrestamosPorMes(obraService.obtenerPrestamosPorMes());
    estadisticas.setObrasPorGenero(obraService.obtenerObrasPorGenero());

    // Obtener actividades recientes (límite 5)
    List<ActividadRecienteDTO> actividades = usuarioService.obtenerActividadesRecientes(5);

    // Obtener obras más populares (límite 5)
    List<ObraPopularDTO> obrasPopulares = obraService.obtenerObrasPopulares(5);

    // Añadir datos al modelo
    model.addAttribute("estadisticas", estadisticas);
    model.addAttribute("actividades", actividades);
    model.addAttribute("obrasPopulares", obrasPopulares);

    return "admin/dashboard";
  }

  @GetMapping("/dashboard/refresh")
  @ResponseBody
  public Map<String, Object> refreshDashboard() {
    Map<String, Object> response = new HashMap<>();

    // Obtener estadísticas actualizadas
    EstadisticasDTO estadisticas = new EstadisticasDTO();
    estadisticas.setTotalUsuarios(usuarioService.contarUsuarios());
    estadisticas.setNuevosUsuariosMes(usuarioService.contarUsuariosNuevosMes());
    estadisticas.setTotalObras(obraService.contarObras());
    estadisticas.setNuevasObrasMes(obraService.contarObrasNuevasMes());
    estadisticas.setTotalPrestamos(obraService.contarPrestamos());
    estadisticas.setNuevosPrestamos(obraService.contarPrestamosMes());
    estadisticas.setTotalGeneros(generoService.contarGeneros());

    // Datos para gráficos
    estadisticas.setPrestamosPorMes(obraService.obtenerPrestamosPorMes());
    estadisticas.setObrasPorGenero(obraService.obtenerObrasPorGenero());

    // Obtener actividades recientes (límite 5)
    List<ActividadRecienteDTO> actividades = usuarioService.obtenerActividadesRecientes(5);

    // Obtener obras más populares (límite 5)
    List<ObraPopularDTO> obrasPopulares = obraService.obtenerObrasPopulares(5);

    // Añadir datos a la respuesta
    response.put("estadisticas", estadisticas);
    response.put("actividades", actividades);
    response.put("obrasPopulares", obrasPopulares);

    return response;
  }
}