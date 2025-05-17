package com.biblioteca.controller.cuenta;

import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

import com.biblioteca.dto.comercial.SuscripcionRequestDTO;
import com.biblioteca.models.Usuario;
import com.biblioteca.service.MetodoPagoService;
import com.biblioteca.service.PlanService;
import com.biblioteca.service.SuscripcionService;
import com.biblioteca.service.UsuarioService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mi-cuenta/suscripcion")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'LECTOR')")
public class MiSuscripcionController {

  private final SuscripcionService suscripcionService;
  private final PlanService planService;
  private final MetodoPagoService metodoPagoService;
  private final UsuarioService usuarioService;

  @GetMapping
  public String verMiSuscripcion(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    // Verificar que el perfil activo es el perfil principal
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede gestionar la suscripción. Por favor, cambie al perfil principal.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    Usuario usuario = obtenerUsuarioActual();

    // Verificar si ya tiene una suscripción activa
    suscripcionService.obtenerSuscripcionActivaPorUsuario(usuario.getId())
        .ifPresentOrElse(
            suscripcion -> {
              model.addAttribute("suscripcion", suscripcion);
              model.addAttribute("tieneSuscripcion", true);
            },
            () -> {
              model.addAttribute("tieneSuscripcion", false);
              model.addAttribute("planes", planService.obtenerPlanesActivos());
            });

    return "suscripcion/mi-suscripcion";
  }

  @GetMapping("/nueva")
  public String mostrarFormularioNuevaSuscripcion(
      @RequestParam(required = false) Long planId,
      Model model,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    // Verificar que el perfil activo es el perfil principal
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede crear suscripciones. Por favor, cambie al perfil principal.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    Usuario usuario = obtenerUsuarioActual();

    // Verificar si ya tiene una suscripción activa
    if (suscripcionService.verificarSuscripcionActiva(usuario.getId())) {
      redirectAttributes.addFlashAttribute("error", "Ya tiene una suscripción activa");
      return "redirect:/mi-cuenta/suscripcion";
    }

    // Preparar formulario
    SuscripcionRequestDTO suscripcionForm = new SuscripcionRequestDTO();
    suscripcionForm.setUsuarioId(usuario.getId());

    // Si se especifica un planId, preseleccionarlo
    if (planId != null) {
      suscripcionForm.setPlanId(planId);
      model.addAttribute("planSeleccionado", planService.obtenerPlanPorId(planId).orElse(null));
    }

    model.addAttribute("suscripcionForm", suscripcionForm);
    model.addAttribute("planes", planService.obtenerPlanesActivos());
    model.addAttribute("metodosPago", metodoPagoService.obtenerMetodosPagoActivos());

    return "suscripcion/formulario";
  }

  @PostMapping("/crear")
  public String crearSuscripcion(
      @Valid @ModelAttribute("suscripcionForm") SuscripcionRequestDTO suscripcionForm,
      BindingResult result,
      Model model,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    // Verificar que el perfil activo es el perfil principal
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede crear suscripciones. Por favor, cambie al perfil principal.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    Usuario usuario = obtenerUsuarioActual();

    // Asegurar que el ID de usuario en el formulario corresponde al usuario actual
    suscripcionForm.setUsuarioId(usuario.getId());

    if (result.hasErrors()) {
      model.addAttribute("planes", planService.obtenerPlanesActivos());
      model.addAttribute("metodosPago", metodoPagoService.obtenerMetodosPagoActivos());
      return "suscripcion/formulario";
    }

    try {
      // Establecer fechas si no se proporcionan
      if (suscripcionForm.getFechaInicio() == null) {
        suscripcionForm.setFechaInicio(LocalDate.now());
      }

      suscripcionService.crearSuscripcion(suscripcionForm);
      redirectAttributes.addFlashAttribute("mensaje", "¡Suscripción creada exitosamente!");
      return "redirect:/mi-cuenta/suscripcion";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error al crear la suscripción: " + e.getMessage());
      return "redirect:/mi-cuenta/suscripcion/nueva";
    }
  }

  @GetMapping("/{id}")
  public String verDetalleSuscripcion(
      @PathVariable Long id,
      Model model,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    // Verificar que el perfil activo es el perfil principal
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede ver detalles de suscripción. Por favor, cambie al perfil principal.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    Usuario usuario = obtenerUsuarioActual();

    return suscripcionService.obtenerSuscripcionPorId(id)
        .map(suscripcion -> {
          // Verificar que la suscripción pertenece al usuario actual
          if (!usuario.getId().equals(suscripcion.getUsuarioId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para ver esta suscripción");
            return "redirect:/mi-cuenta/suscripcion";
          }

          model.addAttribute("suscripcion", suscripcion);
          return "suscripcion/detalle";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Suscripción no encontrada");
          return "redirect:/mi-cuenta/suscripcion";
        });
  }

  @PostMapping("/{id}/renovar")
  public String renovarSuscripcion(
      @PathVariable Long id,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    // Verificar que el perfil activo es el perfil principal
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede renovar suscripciones. Por favor, cambie al perfil principal.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    Usuario usuario = obtenerUsuarioActual();

    return suscripcionService.obtenerSuscripcionPorId(id)
        .map(suscripcion -> {
          // Verificar que la suscripción pertenece al usuario actual
          if (!usuario.getId().equals(suscripcion.getUsuarioId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para renovar esta suscripción");
            return "redirect:/mi-cuenta/suscripcion";
          }

          try {
            suscripcionService.renovarSuscripcion(id);
            redirectAttributes.addFlashAttribute("mensaje", "Suscripción renovada exitosamente");
          } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al renovar la suscripción: " + e.getMessage());
          }

          return "redirect:/mi-cuenta/suscripcion/" + id;
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Suscripción no encontrada");
          return "redirect:/mi-cuenta/suscripcion";
        });
  }

  @GetMapping("/{id}/cambiar-plan")
  public String mostrarFormularioCambiarPlan(
      @PathVariable Long id,
      Model model,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    // Verificar que el perfil activo es el perfil principal
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede cambiar el plan de suscripción. Por favor, cambie al perfil principal.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    Usuario usuario = obtenerUsuarioActual();

    return suscripcionService.obtenerSuscripcionPorId(id)
        .map(suscripcion -> {
          // Verificar que la suscripción pertenece al usuario actual
          if (!usuario.getId().equals(suscripcion.getUsuarioId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para modificar esta suscripción");
            return "redirect:/mi-cuenta/suscripcion";
          }

          model.addAttribute("suscripcion", suscripcion);
          model.addAttribute("planes", planService.obtenerPlanesActivos());
          model.addAttribute("planActual", planService.obtenerPlanPorId(suscripcion.getPlanId()).orElse(null));

          return "suscripcion/cambiar-plan";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Suscripción no encontrada");
          return "redirect:/mi-cuenta/suscripcion";
        });
  }

  @PostMapping("/{id}/cancelar")
  public String cancelarSuscripcion(
      @PathVariable Long id,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    // Verificar que el perfil activo es el perfil principal
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede cancelar suscripciones. Por favor, cambie al perfil principal.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    Usuario usuario = obtenerUsuarioActual();

    return suscripcionService.obtenerSuscripcionPorId(id)
        .map(suscripcion -> {
          // Verificar que la suscripción pertenece al usuario actual
          if (!usuario.getId().equals(suscripcion.getUsuarioId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para cancelar esta suscripción");
            return "redirect:/mi-cuenta/suscripcion";
          }

          boolean cancelada = suscripcionService.cancelarSuscripcion(id);
          if (cancelada) {
            redirectAttributes.addFlashAttribute("mensaje", "Suscripción cancelada exitosamente");
          } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo cancelar la suscripción");
          }

          return "redirect:/mi-cuenta/suscripcion";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Suscripción no encontrada");
          return "redirect:/mi-cuenta/suscripcion";
        });
  }

  @PostMapping("/{id}/pausar")
  public String pausarSuscripcion(
      @PathVariable Long id,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    // Verificar que el perfil activo es el perfil principal
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede pausar suscripciones. Por favor, cambie al perfil principal.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    Usuario usuario = obtenerUsuarioActual();

    return suscripcionService.obtenerSuscripcionPorId(id)
        .map(suscripcion -> {
          // Verificar que la suscripción pertenece al usuario actual
          if (!usuario.getId().equals(suscripcion.getUsuarioId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para pausar esta suscripción");
            return "redirect:/mi-cuenta/suscripcion";
          }

          boolean pausada = suscripcionService.pausarSuscripcion(id);
          if (pausada) {
            redirectAttributes.addFlashAttribute("mensaje", "Suscripción pausada exitosamente");
          } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo pausar la suscripción");
          }

          return "redirect:/mi-cuenta/suscripcion/" + id;
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Suscripción no encontrada");
          return "redirect:/mi-cuenta/suscripcion";
        });
  }

  @PostMapping("/{id}/reactivar")
  public String reactivarSuscripcion(
      @PathVariable Long id,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    // Verificar que el perfil activo es el perfil principal
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede reactivar suscripciones. Por favor, cambie al perfil principal.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    Usuario usuario = obtenerUsuarioActual();

    return suscripcionService.obtenerSuscripcionPorId(id)
        .map(suscripcion -> {
          // Verificar que la suscripción pertenece al usuario actual
          if (!usuario.getId().equals(suscripcion.getUsuarioId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para reactivar esta suscripción");
            return "redirect:/mi-cuenta/suscripcion";
          }

          boolean reactivada = suscripcionService.reactivarSuscripcion(id);
          if (reactivada) {
            redirectAttributes.addFlashAttribute("mensaje", "Suscripción reactivada exitosamente");
          } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo reactivar la suscripción");
          }

          return "redirect:/mi-cuenta/suscripcion/" + id;
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Suscripción no encontrada");
          return "redirect:/mi-cuenta/suscripcion";
        });
  }

  @GetMapping("/{id}/cambiar-metodo-pago")
  public String mostrarFormularioCambiarMetodoPago(
      @PathVariable Long id,
      Model model,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    // Verificar que el perfil activo es el perfil principal
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede cambiar el método de pago. Por favor, cambie al perfil principal.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    Usuario usuario = obtenerUsuarioActual();

    return suscripcionService.obtenerSuscripcionPorId(id)
        .map(suscripcion -> {
          // Verificar que la suscripción pertenece al usuario actual
          if (!usuario.getId().equals(suscripcion.getUsuarioId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para modificar esta suscripción");
            return "redirect:/mi-cuenta/suscripcion";
          }

          model.addAttribute("suscripcion", suscripcion);
          model.addAttribute("metodosPago", metodoPagoService.obtenerMetodosPagoActivos());
          model.addAttribute("metodoPagoActual",
              metodoPagoService.obtenerMetodoPagoPorId(suscripcion.getMetodoPagoId()).orElse(null));

          return "suscripcion/cambiar-metodo-pago";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Suscripción no encontrada");
          return "redirect:/mi-cuenta/suscripcion";
        });
  }

  @PostMapping("/{id}/cambiar-metodo-pago")
  public String cambiarMetodoPago(
      @PathVariable Long id,
      @RequestParam Long nuevoMetodoPagoId,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    // Verificar que el perfil activo es el perfil principal
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede cambiar el método de pago. Por favor, cambie al perfil principal.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    Usuario usuario = obtenerUsuarioActual();

    return suscripcionService.obtenerSuscripcionPorId(id)
        .map(suscripcion -> {
          // Verificar que la suscripción pertenece al usuario actual
          if (!usuario.getId().equals(suscripcion.getUsuarioId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para modificar esta suscripción");
            return "redirect:/mi-cuenta/suscripcion";
          }

          try {
            suscripcionService.cambiarMetodoPago(id, nuevoMetodoPagoId);
            redirectAttributes.addFlashAttribute("mensaje", "Método de pago cambiado exitosamente");
          } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar el método de pago: " + e.getMessage());
          }

          return "redirect:/mi-cuenta/suscripcion/" + id;
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Suscripción no encontrada");
          return "redirect:/mi-cuenta/suscripcion";
        });
  }

  /**
   * Método para verificar si el perfil activo es el perfil principal
   */
  private boolean esPerfilPrincipal(HttpSession session) {
    Boolean esPerfilPrincipal = (Boolean) session.getAttribute("esPerfilPrincipal");
    return esPerfilPrincipal != null && esPerfilPrincipal;
  }
  
  /**
   * Método para obtener el usuario actual
   */
  private Usuario obtenerUsuarioActual() {
    String username = obtenerUsernameActual();
    return usuarioService.buscarPorUsername(username).orElse(null);
  }

  /**
   * Método para obtener el nombre de usuario actual
   */
  private String obtenerUsernameActual() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication.getName();
  }
}