package com.biblioteca.controller.cuenta;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca.dto.comercial.PagoRequestDTO;
import com.biblioteca.dto.comercial.PagoResponseDTO;
import com.biblioteca.models.acceso.Perfil;
import com.biblioteca.service.MetodoPagoService;
import com.biblioteca.service.OrdenService;
import com.biblioteca.service.PagoService;
import com.biblioteca.service.PerfilService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mi-cuenta/pago")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LECTOR')")
public class MiPagoController {

  private final PagoService pagoService;
  private final OrdenService ordenService;
  private final MetodoPagoService metodoPagoService;
  private final PerfilService perfilService;

  @GetMapping("/{id}")
  public String verDetallePago(@PathVariable Long id, Model model,
      HttpSession session, RedirectAttributes redirectAttributes) {
    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para ver los pagos");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    return pagoService.obtenerPagoPorId(id)
        .map(pago -> {
          // Verificar que el pago corresponde a una orden del usuario actual
          if (!ordenPertenecePerfil(pago.getOrdenId(), perfil.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para ver este pago");
            return "redirect:/mi-cuenta/orden";
          }

          model.addAttribute("pago", pago);
          return "pagos/detalle";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Pago no encontrado");
          return "redirect:/mi-cuenta/orden";
        });
  }

  @GetMapping("/orden/{ordenId}")
  public String verPagosPorOrden(
      @PathVariable Long ordenId,
      Model model,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para ver los pagos");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    // Verificar que la orden pertenece al usuario actual
    if (!ordenPertenecePerfil(ordenId, perfil.getId())) {
      redirectAttributes.addFlashAttribute("error", "No tiene permiso para ver estos pagos");
      return "redirect:/mi-cuenta/orden";
    }

    model.addAttribute("pagos", pagoService.obtenerPagosPorOrden(ordenId));
    model.addAttribute("ordenId", ordenId);

    // Agregar datos de la orden
    ordenService.obtenerOrdenPorId(ordenId)
        .ifPresent(orden -> model.addAttribute("orden", orden));

    return "pagos/lista-por-orden";
  }

  @GetMapping("/nuevo/{ordenId}")
  public String mostrarFormularioNuevoPago(
      @PathVariable Long ordenId,
      Model model,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para realizar pagos");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    // Verificar que la orden pertenece al usuario actual
    if (!ordenPertenecePerfil(ordenId, perfil.getId())) {
      redirectAttributes.addFlashAttribute("error", "No tiene permiso para realizar este pago");
      return "redirect:/mi-cuenta/orden";
    }

    // Obtener datos de la orden
    return ordenService.obtenerOrdenPorId(ordenId)
        .map(orden -> {
          model.addAttribute("orden", orden);
          model.addAttribute("metodosPago", metodoPagoService.obtenerMetodosPagoActivos());

          PagoRequestDTO pagoForm = new PagoRequestDTO();
          pagoForm.setOrdenId(ordenId);
          pagoForm.setMonto(orden.getTotalOrden());

          model.addAttribute("pagoForm", pagoForm);

          return "pagos/formulario";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Orden no encontrada");
          return "redirect:/mi-cuenta/orden";
        });
  }

  @PostMapping("/procesar")
  public String procesarPago(
      @Valid @ModelAttribute("pagoForm") PagoRequestDTO pagoDTO,
      BindingResult result,
      Model model,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    if (result.hasErrors()) {
      model.addAttribute("metodosPago", metodoPagoService.obtenerMetodosPagoActivos());

      ordenService.obtenerOrdenPorId(pagoDTO.getOrdenId())
          .ifPresent(orden -> model.addAttribute("orden", orden));

      return "pagos/formulario";
    }

    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para realizar pagos");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    // Verificar que la orden pertenece al usuario actual
    if (!ordenPertenecePerfil(pagoDTO.getOrdenId(), perfil.getId())) {
      redirectAttributes.addFlashAttribute("error", "No tiene permiso para realizar este pago");
      return "redirect:/mi-cuenta/orden";
    }

    try {
      // Registrar el pago
      PagoResponseDTO pago = pagoService.registrarPago(pagoDTO);

      // Procesar el pago
      pagoService.procesarPago(pago.getId());

      // Simular aprobación del pago
      pagoService.aprobarPago(pago.getId(), "Pago simulado " + System.currentTimeMillis());

      redirectAttributes.addFlashAttribute("mensaje", "Pago realizado exitosamente");
      return "redirect:/mi-cuenta/pago/" + pago.getId();
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error al procesar el pago: " + e.getMessage());
      return "redirect:/mi-cuenta/pago/nuevo/" + pagoDTO.getOrdenId();
    }
  }

  @PostMapping("/{id}/verificar")
  public String verificarEstadoPago(
      @PathVariable Long id,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para verificar pagos");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    return pagoService.obtenerPagoPorId(id)
        .map(pago -> {
          // Verificar que el pago corresponde a una orden del usuario actual
          if (!ordenPertenecePerfil(pago.getOrdenId(), perfil.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para verificar este pago");
            return "redirect:/mi-cuenta/orden";
          }

          // Verificar estado con pasarela de pago
          pagoService.verificarEstadoConPasarela(id);

          redirectAttributes.addFlashAttribute("mensaje", "Estado del pago verificado con la pasarela");
          return "redirect:/mi-cuenta/pago/" + id;
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Pago no encontrado");
          return "redirect:/mi-cuenta/orden";
        });
  }

  @PostMapping("/{id}/cancelar")
  public String cancelarPago(
      @PathVariable Long id,
      @RequestParam(required = false) String motivo,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para cancelar pagos");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    return pagoService.obtenerPagoPorId(id)
        .map(pago -> {
          // Verificar que el pago corresponde a una orden del usuario actual
          if (!ordenPertenecePerfil(pago.getOrdenId(), perfil.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para cancelar este pago");
            return "redirect:/mi-cuenta/orden";
          }

          // Solo se pueden cancelar pagos pendientes
          if (!"Pendiente".equals(pago.getEstado()) && !"Procesando".equals(pago.getEstado())) {
            redirectAttributes.addFlashAttribute("error",
                "No se puede cancelar un pago que no está en estado pendiente o procesando");
            return "redirect:/mi-cuenta/pago/" + id;
          }

          try {
            pagoService.rechazarPago(id, motivo);
            redirectAttributes.addFlashAttribute("mensaje", "Pago cancelado exitosamente");
          } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar el pago: " + e.getMessage());
          }

          return "redirect:/mi-cuenta/pago/" + id;
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Pago no encontrado");
          return "redirect:/mi-cuenta/orden";
        });
  }

  private boolean ordenPertenecePerfil(Long ordenId, Long perfilId) {
    return ordenService.obtenerOrdenPorId(ordenId)
        .map(orden -> orden.getPerfilId().equals(perfilId))
        .orElse(false);
  }

  private Perfil obtenerPerfilActual(HttpSession session) {
    // Obtener el ID del perfil activo desde la sesión
    Long perfilActivoId = (Long) session.getAttribute("perfilActivoId");

    if (perfilActivoId == null) {
      return null;
    }

    // Obtener el perfil desde la base de datos usando el ID
    return perfilService.obtenerEntidadPerfilPorId(perfilActivoId)
        .orElse(null);
  }
  
}