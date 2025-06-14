package com.biblioteca.controller.cuenta;

import java.time.LocalDate;
import java.util.Optional;

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

import com.biblioteca.dto.comercial.PlanResponseDTO;
import com.biblioteca.dto.comercial.SuscripcionRequestDTO;
import com.biblioteca.dto.comercial.SuscripcionResponseDTO;
import com.biblioteca.models.acceso.Usuario;
import com.biblioteca.service.MetodoPagoService;
import com.biblioteca.service.PlanBeneficioService;
import com.biblioteca.service.PlanService;
import com.biblioteca.service.SuscripcionService;
import com.biblioteca.service.UsuarioService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/mi-cuenta/suscripcion")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LECTOR')")
public class MiSuscripcionController {
  private final SuscripcionService suscripcionService;
  private final PlanService planService;
  private final PlanBeneficioService planBeneficioService;
  private final MetodoPagoService metodoPagoService;
  private final UsuarioService usuarioService;
  @GetMapping
  public String verMiSuscripcion(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    Usuario usuario = obtenerUsuarioActual();
    
    // Verificar si es perfil principal para habilitar gestión
    boolean esPerfilPrincipal = esPerfilPrincipal(session);
    model.addAttribute("esPerfilPrincipal", esPerfilPrincipal);
    
    // Si no es perfil principal, mostrar información pero deshabilitar gestión
    if (!esPerfilPrincipal) {
      model.addAttribute("soloLectura", true);
      model.addAttribute("mensaje", "Solo el perfil principal puede gestionar la suscripción. Estás viendo la información en modo de solo lectura.");
    }

    // Verificar si ya tiene una suscripción activa
    suscripcionService.obtenerSuscripcionActivaPorUsuario(usuario.getId())
        .ifPresentOrElse(
            suscripcion -> {
              model.addAttribute("suscripcion", suscripcion);
              model.addAttribute("tieneSuscripcion", true);

              try {
                // Obtener beneficios del plan usando planId de la suscripción
                var beneficiosPlan = planBeneficioService.obtenerBeneficiosPorPlanId(suscripcion.getPlanId());
                model.addAttribute("beneficiosPlan", beneficiosPlan);

                // Obtener información del plan
                planService.obtenerPlanPorId(suscripcion.getPlanId())
                    .ifPresent(plan -> model.addAttribute("plan", plan));

                // Obtener información del método de pago
                metodoPagoService.obtenerMetodoPagoPorId(suscripcion.getMetodoPagoId())
                    .ifPresent(metodoPago -> model.addAttribute("metodoPago", metodoPago));

                // Calcular días hasta renovación
                if (suscripcion.getFechaRenovacion() != null) {
                  long diasHastaRenovacion = java.time.temporal.ChronoUnit.DAYS.between(
                      LocalDate.now(), suscripcion.getFechaRenovacion());
                  model.addAttribute("diasHastaRenovacion", Math.max(0, diasHastaRenovacion));
                } else {
                  model.addAttribute("diasHastaRenovacion", 0);
                }

              } catch (Exception e) {
                // En caso de error, continuar sin la información adicional
                model.addAttribute("beneficiosPlan", java.util.Collections.emptyList());
                model.addAttribute("diasHastaRenovacion", 0);
                model.addAttribute("plan", null);
                model.addAttribute("metodoPago", null);
              }
            },
            () -> {
              model.addAttribute("tieneSuscripcion", false);
              // Obtener planes disponibles para mostrar al usuario
              try {
                var planesDisponibles = planService.obtenerPlanesActivos();
                model.addAttribute("planes", planesDisponibles);
              } catch (Exception e) {
                model.addAttribute("planes", java.util.Collections.emptyList());
              }
            });

    return "mi-cuenta/suscripcion/mi-suscripcion";
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

    return "mi-cuenta/suscripcion/formulario";
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
      return "mi-cuenta/suscripcion/formulario";
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
          return "mi-cuenta/suscripcion/detalle";
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

          return "mi-cuenta/suscripcion/cambiar-plan";
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

          return "mi-cuenta/suscripcion/cambiar-metodo-pago";
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

  @GetMapping("/detalle")
  public String verDetalleSuscripcion(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede ver los detalles de la suscripción.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    Usuario usuario = obtenerUsuarioActual();
    return suscripcionService.obtenerSuscripcionActivaPorUsuario(usuario.getId())
        .map(suscripcion -> {
          model.addAttribute("suscripcion", suscripcion);
          // Crear estadísticas básicas
          var estadisticas = new java.util.HashMap<String, Object>();
          estadisticas.put("prestamosRealizados", 0);
          estadisticas.put("contenidosAccedidos", 0);
          estadisticas.put("ultimoAcceso", null);
          model.addAttribute("estadisticas", estadisticas); // Historial de pagos vacío por ahora
          model.addAttribute("historialPagos", java.util.Collections.emptyList());
          return "mi-cuenta/suscripcion/detalle";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "No tiene una suscripción activa");
          return "redirect:/mi-cuenta/suscripcion";
        });
  }

  @GetMapping("/cambiar-plan")
  public String mostrarFormularioCambioPlan(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede cambiar el plan de suscripción.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    Usuario usuario = obtenerUsuarioActual();
    return suscripcionService.obtenerSuscripcionActivaPorUsuario(usuario.getId())
        .map(suscripcion -> {
          model.addAttribute("suscripcionActual", suscripcion);
          model.addAttribute("planesDisponibles", planService.obtenerPlanesActivos());
          return "mi-cuenta/suscripcion/cambiar-plan";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "No tiene una suscripción activa");
          return "redirect:/mi-cuenta/suscripcion";
        });
  }
  @PostMapping("/cambiar-plan")
  public String procesarCambioPlan(
      @RequestParam Long planId,
      @RequestParam(required = false) String modalidadPago,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede cambiar el plan de suscripción.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      Usuario usuario = obtenerUsuarioActual();
      
      // Obtener suscripción activa del usuario
      Optional<SuscripcionResponseDTO> suscripcionOpt = suscripcionService.obtenerSuscripcionActivaPorUsuario(usuario.getId());
      
      if (!suscripcionOpt.isPresent()) {
        redirectAttributes.addFlashAttribute("error", "No tiene una suscripción activa para cambiar el plan");
        return "redirect:/mi-cuenta/suscripcion";
      }
      
      SuscripcionResponseDTO suscripcionActual = suscripcionOpt.get();
      
      // Verificar que el plan seleccionado existe y está activo
      Optional<PlanResponseDTO> nuevoPlanOpt = planService.obtenerPlanPorId(planId);
      if (!nuevoPlanOpt.isPresent()) {
        redirectAttributes.addFlashAttribute("error", "El plan seleccionado no existe");
        return "redirect:/mi-cuenta/suscripcion/cambiar-plan";
      }
      
      PlanResponseDTO nuevoPlan = nuevoPlanOpt.get();
      
      // Verificar que no sea el mismo plan actual
      if (suscripcionActual.getPlanId().equals(planId) && 
          (modalidadPago == null || modalidadPago.equals(suscripcionActual.getModalidadPago()))) {
        redirectAttributes.addFlashAttribute("mensaje", "Ya tiene ese plan y modalidad activos");
        redirectAttributes.addFlashAttribute("tipoMensaje", "info");
        return "redirect:/mi-cuenta/suscripcion";
      }
      
      // Determinar si es modalidad de pago a usar
      String modalidadFinal = modalidadPago != null ? modalidadPago : suscripcionActual.getModalidadPago();
        // Procesar cambio de plan usando el servicio
      suscripcionService.cambiarPlan(
          suscripcionActual.getId(), planId, modalidadFinal);
      
      // Determinar tipo de cambio y mensaje apropiado
      boolean esUpgrade = determinarSiEsUpgrade(suscripcionActual, nuevoPlan);
      boolean esDowngrade = determinarSiEsDowngrade(suscripcionActual, nuevoPlan);
      
      String mensaje;
      String tipoMensaje = "success";
      
      if (esUpgrade) {
        mensaje = String.format("¡Plan actualizado a %s exitosamente! El cambio es efectivo inmediatamente. " +
            "Se ha calculado el prorrateado correspondiente.", nuevoPlan.getNombre());
      } else if (esDowngrade) {
        mensaje = String.format("Su plan cambiará a %s al final del período actual (%s). " +
            "Hasta entonces, mantendrá todos los beneficios del plan actual.", 
            nuevoPlan.getNombre(), 
            suscripcionActual.getFechaRenovacion());
        tipoMensaje = "info";
      } else {
        mensaje = String.format("Plan cambiado exitosamente a %s.", nuevoPlan.getNombre());
      }
      
      // Agregar información sobre cambio de modalidad si aplica
      if (modalidadPago != null && !modalidadPago.equals(suscripcionActual.getModalidadPago())) {
        mensaje += String.format(" La modalidad de pago se cambió de %s a %s.", 
            suscripcionActual.getModalidadPago(), modalidadPago);
      }
      
      redirectAttributes.addFlashAttribute("mensaje", mensaje);
      redirectAttributes.addFlashAttribute("tipoMensaje", tipoMensaje);
      
    } catch (Exception e) {
      log.error("Error al cambiar el plan: {}", e.getMessage(), e);
      redirectAttributes.addFlashAttribute("error", "Error al cambiar el plan: " + e.getMessage());
      return "redirect:/mi-cuenta/suscripcion/cambiar-plan";
    }

    return "redirect:/mi-cuenta/suscripcion";
  }

  @GetMapping("/metodo-pago")
  public String mostrarFormularioMetodoPago(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede cambiar el método de pago.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    Usuario usuario = obtenerUsuarioActual();
    return suscripcionService.obtenerSuscripcionActivaPorUsuario(usuario.getId())
        .map(suscripcion -> {
          model.addAttribute("suscripcion", suscripcion);
          // Método de pago ficticio por ahora
          var metodoPago = new java.util.HashMap<String, String>();
          metodoPago.put("tipo", "Tarjeta de Crédito");
          metodoPago.put("ultimosDigitos", "1234");
          metodoPago.put("fechaVencimiento", "12/25");
          model.addAttribute("metodoPagoActual", metodoPago);
          return "mi-cuenta/suscripcion/cambiar-metodo-pago";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "No tiene una suscripción activa");
          return "redirect:/mi-cuenta/suscripcion";
        });
  }

  @PostMapping("/metodo-pago")
  public String procesarCambioMetodoPago(
      @RequestParam String tipoPago,
      @RequestParam(required = false) String numeroTarjeta,
      @RequestParam(required = false) String fechaVencimiento,
      @RequestParam(required = false) String codigoSeguridad,
      @RequestParam(required = false) String nombreTitular,
      @RequestParam(required = false) String documentoTitular,
      @RequestParam(required = false) String emailPaypal,
      @RequestParam(required = false) String direccion,
      @RequestParam(required = false) String ciudad,
      @RequestParam(required = false) String codigoPostal,
      @RequestParam(required = false) String pais,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede cambiar el método de pago.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      obtenerUsuarioActual();
      // Por ahora solo mostrar mensaje de éxito - implementar lógica real más tarde
      redirectAttributes.addFlashAttribute("mensaje", "Funcionalidad de cambio de método de pago en desarrollo");
      redirectAttributes.addFlashAttribute("tipoMensaje", "info");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error al actualizar método de pago: " + e.getMessage());
    }

    return "redirect:/mi-cuenta/suscripcion";
  }

  @PostMapping("/pausar")
  public String pausarSuscripcion(HttpSession session, RedirectAttributes redirectAttributes) {
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede pausar la suscripción.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      // Por ahora solo mostrar mensaje de éxito - implementar lógica real más tarde
      redirectAttributes.addFlashAttribute("mensaje", "Funcionalidad de pausar suscripción en desarrollo");
      redirectAttributes.addFlashAttribute("tipoMensaje", "info");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error al pausar la suscripción: " + e.getMessage());
    }

    return "redirect:/mi-cuenta/suscripcion";
  }

  @PostMapping("/reanudar")
  public String reanudarSuscripcion(HttpSession session, RedirectAttributes redirectAttributes) {
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede reanudar la suscripción.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      // Por ahora solo mostrar mensaje de éxito - implementar lógica real más tarde
      redirectAttributes.addFlashAttribute("mensaje", "Funcionalidad de reanudar suscripción en desarrollo");
      redirectAttributes.addFlashAttribute("tipoMensaje", "info");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error al reanudar la suscripción: " + e.getMessage());
    }

    return "redirect:/mi-cuenta/suscripcion";
  }

  @PostMapping("/renovar")
  public String renovarSuscripcion(HttpSession session, RedirectAttributes redirectAttributes) {
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede renovar la suscripción.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      // Por ahora solo mostrar mensaje de éxito - implementar lógica real más tarde
      redirectAttributes.addFlashAttribute("mensaje", "Funcionalidad de renovar suscripción en desarrollo");
      redirectAttributes.addFlashAttribute("tipoMensaje", "info");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error al renovar la suscripción: " + e.getMessage());
    }

    return "redirect:/mi-cuenta/suscripcion";
  }

  @PostMapping("/cancelar")
  public String cancelarSuscripcion(
      @RequestParam String motivo,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede cancelar la suscripción.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      // Por ahora solo mostrar mensaje de éxito - implementar lógica real más tarde
      redirectAttributes.addFlashAttribute("mensaje",
          "Funcionalidad de cancelar suscripción en desarrollo. Motivo: " + motivo);
      redirectAttributes.addFlashAttribute("tipoMensaje", "info");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error al cancelar la suscripción: " + e.getMessage());
    }

    return "redirect:/mi-cuenta/suscripcion";
  }

  @GetMapping("/factura/{pagoId}")
  public String descargarFactura(@PathVariable Long pagoId, HttpSession session,
      RedirectAttributes redirectAttributes) {
    if (!esPerfilPrincipal(session)) {
      redirectAttributes.addFlashAttribute("error",
          "Solo el perfil principal puede descargar facturas.");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      // Por ahora solo mostrar mensaje - implementar lógica real más tarde
      redirectAttributes.addFlashAttribute("mensaje",
          "Funcionalidad de descargar facturas en desarrollo. ID: " + pagoId);
      redirectAttributes.addFlashAttribute("tipoMensaje", "info");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error al generar la factura: " + e.getMessage());
    }

    return "redirect:/mi-cuenta/suscripcion";
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

  /**
   * Determina si el cambio de plan es un upgrade (plan más caro)
   */
  private boolean determinarSiEsUpgrade(SuscripcionResponseDTO suscripcionActual, PlanResponseDTO nuevoPlan) {
    try {
      Optional<PlanResponseDTO> planActualOpt = planService.obtenerPlanPorId(suscripcionActual.getPlanId());
      if (!planActualOpt.isPresent()) {
        return false;
      }
      
      PlanResponseDTO planActual = planActualOpt.get();
      
      // Comparar precios según modalidad actual
      if ("anual".equalsIgnoreCase(suscripcionActual.getModalidadPago())) {
        return nuevoPlan.getPrecioAnual() > planActual.getPrecioAnual();
      } else {
        return nuevoPlan.getPrecioMensual() > planActual.getPrecioMensual();
      }
    } catch (Exception e) {
      log.warn("Error determinando upgrade: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Determina si el cambio de plan es un downgrade (plan más barato)
   */
  private boolean determinarSiEsDowngrade(SuscripcionResponseDTO suscripcionActual, PlanResponseDTO nuevoPlan) {
    try {
      Optional<PlanResponseDTO> planActualOpt = planService.obtenerPlanPorId(suscripcionActual.getPlanId());
      if (!planActualOpt.isPresent()) {
        return false;
      }
      
      PlanResponseDTO planActual = planActualOpt.get();
      
      // Comparar precios según modalidad actual
      if ("anual".equalsIgnoreCase(suscripcionActual.getModalidadPago())) {
        return nuevoPlan.getPrecioAnual() < planActual.getPrecioAnual();
      } else {
        return nuevoPlan.getPrecioMensual() < planActual.getPrecioMensual();
      }
    } catch (Exception e) {
      log.warn("Error determinando downgrade: {}", e.getMessage());
      return false;
    }
  }
}
