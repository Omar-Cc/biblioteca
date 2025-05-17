package com.biblioteca.controller.cuenta;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca.dto.comercial.FacturaResponseDTO;
import com.biblioteca.models.Perfil;
import com.biblioteca.models.comercial.Orden;
import com.biblioteca.service.FacturaService;
import com.biblioteca.service.OrdenService;
import com.biblioteca.service.PerfilService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mi-cuenta/facturas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'LECTOR')")
public class MiFacturaController {

  private final FacturaService facturaService;
  private final PerfilService perfilService;
  private final OrdenService ordenService;

  @GetMapping
  public String listarFacturasUsuario(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    // Obtener el perfil activo desde la sesión
    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para ver las facturas");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    // Mostrar facturas del perfil activo, no del usuario completo
    model.addAttribute("facturas", facturaService.obtenerFacturasPorPerfil(perfil.getId()));
    return "facturas/lista-usuario";
  }

  @GetMapping("/{id}")
  public String verFactura(@PathVariable Long id, Model model,
      HttpSession session, RedirectAttributes redirectAttributes) {
    Perfil perfilActual = obtenerPerfilActual(session);
    
    if (perfilActual == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para ver las facturas");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    return facturaService.obtenerFacturaPorId(id)
        .map(factura -> {
          // Verificar que la factura pertenece al perfil actual
          if (!facturaPertenecePerfil(factura, perfilActual.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para ver esta factura");
            return "redirect:/mi-cuenta/facturas";
          }

          model.addAttribute("factura", factura);
          return "facturas/detalle";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Factura no encontrada");
          return "redirect:/mi-cuenta/facturas";
        });
  }

  @GetMapping("/{id}/imprimir")
  public String prepararImpresionFactura(@PathVariable Long id, Model model,
      HttpSession session, RedirectAttributes redirectAttributes) {
    Perfil perfilActual = obtenerPerfilActual(session);
    
    if (perfilActual == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para imprimir facturas");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    return facturaService.obtenerFacturaPorId(id)
        .map(factura -> {
          // Verificar que la factura pertenece al perfil actual
          if (!facturaPertenecePerfil(factura, perfilActual.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para imprimir esta factura");
            return "redirect:/mi-cuenta/facturas";
          }

          model.addAttribute("factura", factura);
          return "facturas/imprimir";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Factura no encontrada");
          return "redirect:/mi-cuenta/facturas";
        });
  }

  private Perfil obtenerPerfilActual(HttpSession session) {
    // Obtener el ID del perfil activo desde la sesión
    Long perfilActivoId = (Long) session.getAttribute("perfilActivoId");

    if (perfilActivoId == null) {
      return null;
    }

    // Obtener el perfil desde la base de datos usando el ID
    return perfilService.obtenerPerfilPorId(perfilActivoId)
        .orElse(null);
  }

  private boolean facturaPertenecePerfil(FacturaResponseDTO factura, Long perfilId) {
    try {
      // Verificar si la factura pertenece al perfil actual
      Orden orden = ordenService.obtenerEntidadOrdenPorId(factura.getOrdenId())
          .orElseThrow(() -> new NullPointerException("Orden no encontrada"));

      return orden.getPerfil().getId().equals(perfilId);
    } catch (NullPointerException e) {
      return false;
    }
  }
}