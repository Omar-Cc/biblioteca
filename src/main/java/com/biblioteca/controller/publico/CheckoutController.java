package com.biblioteca.controller.publico;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca.service.MetodoPagoService;
import com.biblioteca.service.PagoService;
import com.biblioteca.service.PlanBeneficioService;
import com.biblioteca.service.PlanService;
import com.biblioteca.service.SuscripcionService;
import com.biblioteca.service.UsuarioService;
import com.biblioteca.dto.comercial.PagoRequestDTO;
import com.biblioteca.dto.comercial.PagoResponseDTO;
import com.biblioteca.dto.comercial.PlanResponseDTO;
import com.biblioteca.dto.comercial.SuscripcionResponseDTO;
import com.biblioteca.models.acceso.Usuario;
import com.biblioteca.dto.comercial.SuscripcionRequestDTO;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {  @Autowired
  private PlanService planService;

  @Autowired
  private SuscripcionService suscripcionService;

  @Autowired
  private UsuarioService usuarioService;

  @Autowired
  private PlanBeneficioService planBeneficioService;
  @Autowired
  private MetodoPagoService metodoPagoService;

  @Autowired
  private PagoService pagoService;

  // Constantes para estados
  private static final String ESTADO_ACTIVA = "ACTIVA";
  private static final String ESTADO_TRIAL = "TRIAL";

  // Constantes para modalidades
  private static final String MODALIDAD_MENSUAL = "MENSUAL";
  private static final String MODALIDAD_ANUAL = "ANUAL";

  /**
   * Muestra la página de checkout para un plan específico
   */  @GetMapping("/plan/{planId}")
  public String mostrarCheckout(@PathVariable Long planId,
      @RequestParam(defaultValue = "MENSUAL") String modalidad,
      Model model,
      RedirectAttributes redirectAttributes,
      HttpServletRequest request) {

    System.out.println("🔍 DEBUG - CheckoutController.mostrarCheckout iniciado");
    System.out.println("🔍 DEBUG - planId: " + planId);
    System.out.println("🔍 DEBUG - modalidad: " + modalidad);

    // Verificar autenticación
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    System.out.println("🔍 DEBUG - auth: " + (auth != null ? auth.getName() : "null"));    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
      System.out.println("🔍 DEBUG - Usuario no autenticado en CheckoutController");
      // Spring Security se encargará automáticamente de guardar la URL cuando redirija
      redirectAttributes.addFlashAttribute("mensaje", "Debes iniciar sesión para suscribirte a un plan");
      redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
      return "redirect:/login";
    }// Obtener usuario actual
    Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(auth.getName());
    System.out.println("🔍 DEBUG - usuarioOpt.isPresent(): " + usuarioOpt.isPresent());

    if (usuarioOpt.isEmpty()) {
      System.out.println("🔍 DEBUG - Usuario no encontrado, redirigiendo a planes");
      redirectAttributes.addFlashAttribute("mensaje", "Usuario no encontrado, redirigiendo a planes");
      redirectAttributes.addFlashAttribute("tipoMensaje", "error");
      return "redirect:/planes";
    }    Usuario usuario = usuarioOpt.get();
    System.out.println("🔍 DEBUG - Usuario encontrado: " + usuario.getEmail());

    // Verificar si es perfil principal
    Boolean esPerfilPrincipal = (Boolean) request.getSession().getAttribute("esPerfilPrincipal");
    if (esPerfilPrincipal == null || !esPerfilPrincipal) {
      System.out.println("🔍 DEBUG - Acceso denegado: No es perfil principal");
      redirectAttributes.addFlashAttribute("mensaje", "Solo puedes gestionar suscripciones desde el perfil principal");
      redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
      return "redirect:/planes";
    }

    // Obtener plan
    Optional<PlanResponseDTO> planOpt = planService.obtenerPlanPorId(planId);
    System.out.println("🔍 DEBUG - planOpt.isPresent(): " + planOpt.isPresent());

    if (planOpt.isEmpty()) {
      System.out.println("🔍 DEBUG - Plan no encontrado, redirigiendo a planes");
      redirectAttributes.addFlashAttribute("mensaje", "Plan no encontrado");
      redirectAttributes.addFlashAttribute("tipoMensaje", "error");
      return "redirect:/planes";
    }
    PlanResponseDTO plan = planOpt.get();
    System.out.println("🔍 DEBUG - Plan encontrado: " + plan.getNombre());

    // Verificar si el usuario ya tiene una suscripción activa
    Optional<SuscripcionResponseDTO> suscripcionOpt = suscripcionService
        .obtenerSuscripcionActivaPorUsuario(usuario.getId());
    if (suscripcionOpt.isPresent()) {
      SuscripcionResponseDTO suscripcionActual = suscripcionOpt.get();
      if (suscripcionActual.getPlanId().equals(planId)) {
        redirectAttributes.addFlashAttribute("mensaje", "Ya tienes una suscripción activa a este plan");
        redirectAttributes.addFlashAttribute("tipoMensaje", "info");
        return "redirect:/mi-cuenta/suscripcion";
      }
    }

    // Validar modalidad
    if (!MODALIDAD_MENSUAL.equals(modalidad.toUpperCase()) && !MODALIDAD_ANUAL.equals(modalidad.toUpperCase())) {
      modalidad = MODALIDAD_MENSUAL;
    }
    modalidad = modalidad.toUpperCase();

    // Calcular precio según modalidad
    BigDecimal precio = MODALIDAD_ANUAL.equals(modalidad)
        ? BigDecimal.valueOf(plan.getPrecioAnual()).divide(BigDecimal.valueOf(100))
        : BigDecimal.valueOf(plan.getPrecioMensual()).divide(BigDecimal.valueOf(100));

    // Calcular descuento para modalidad anual
    BigDecimal descuento = BigDecimal.ZERO;
    if (MODALIDAD_ANUAL.equals(modalidad) && plan.getPrecioMensual() > 0) {
      BigDecimal precioMensualAnualizado = BigDecimal.valueOf(plan.getPrecioMensual() * 12)
          .divide(BigDecimal.valueOf(100));
      descuento = precioMensualAnualizado.subtract(precio);
    }    // Obtener beneficios del plan
    var beneficios = planBeneficioService.obtenerBeneficiosPorPlanId(planId);

    // Obtener métodos de pago activos
    var metodosPago = metodoPagoService.obtenerMetodosPagoActivos();

    // Agregar atributos al modelo
    model.addAttribute("plan", plan);
    model.addAttribute("usuario", usuario);
    model.addAttribute("modalidad", modalidad);
    model.addAttribute("precio", precio);
    model.addAttribute("descuento", descuento);
    model.addAttribute("beneficios", beneficios);
    model.addAttribute("metodosPago", metodosPago);
    model.addAttribute("suscripcionActual", suscripcionOpt.orElse(null));
    model.addAttribute("esUpgrade", suscripcionOpt.isPresent());

    return "public/checkout/checkout";
  }

  /**
   * Procesa el checkout y crea la suscripción
   */  @PostMapping("/procesar")
  public String procesarCheckout(@RequestParam Long planId,
      @RequestParam String modalidad,
      @RequestParam Long metodoPagoId,
      RedirectAttributes redirectAttributes,
      HttpServletRequest request) {

    // Verificar autenticación
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
      redirectAttributes.addFlashAttribute("mensaje", "Debes iniciar sesión para completar la suscripción");
      redirectAttributes.addFlashAttribute("tipoMensaje", "error");
      return "redirect:/auth/login";
    }

    try {      // Obtener usuario actual
      Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(auth.getName());
      if (usuarioOpt.isEmpty()) {
        redirectAttributes.addFlashAttribute("mensaje", "Usuario no encontrado");
        redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        return "redirect:/planes";
      }
      Usuario usuario = usuarioOpt.get();

      // Verificar si es perfil principal
      Boolean esPerfilPrincipal = (Boolean) request.getSession().getAttribute("esPerfilPrincipal");
      if (esPerfilPrincipal == null || !esPerfilPrincipal) {
        redirectAttributes.addFlashAttribute("mensaje", "Solo puedes gestionar suscripciones desde el perfil principal");
        redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
        return "redirect:/planes";
      }

      // Obtener plan
      Optional<PlanResponseDTO> planOpt = planService.obtenerPlanPorId(planId);
      if (planOpt.isEmpty()) {
        redirectAttributes.addFlashAttribute("mensaje", "Plan no encontrado");
        redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        return "redirect:/planes";
      }
      PlanResponseDTO plan = planOpt.get();

      // Validar modalidad
      if (!MODALIDAD_MENSUAL.equals(modalidad.toUpperCase()) && !MODALIDAD_ANUAL.equals(modalidad.toUpperCase())) {
        modalidad = MODALIDAD_MENSUAL;
      }
      modalidad = modalidad.toUpperCase();

      // Verificar si ya tiene suscripción activa al mismo plan
      Optional<SuscripcionResponseDTO> suscripcionOpt = suscripcionService
          .obtenerSuscripcionActivaPorUsuario(usuario.getId());
      if (suscripcionOpt.isPresent()) {
        SuscripcionResponseDTO suscripcionExistente = suscripcionOpt.get();
        if (suscripcionExistente.getPlanId().equals(planId)) {
          redirectAttributes.addFlashAttribute("mensaje", "Ya tienes una suscripción activa a este plan");
          redirectAttributes.addFlashAttribute("tipoMensaje", "info");
          return "redirect:/mi-cuenta/suscripcion";
        }      }

      SuscripcionResponseDTO nuevaSuscripcion;
      
      // Verificar si el usuario ya tiene una suscripción activa
      if (suscripcionOpt.isPresent()) {
        // Usuario ya tiene suscripción, cambiar plan
        SuscripcionResponseDTO suscripcionExistente = suscripcionOpt.get();        System.out.println("🔍 DEBUG - Cambiando plan de suscripción existente ID: " + suscripcionExistente.getId() + " al plan: " + planId);
        
        // Usar el método cambiarPlan del servicio con modalidad
        nuevaSuscripcion = suscripcionService.cambiarPlan(suscripcionExistente.getId(), planId, modalidad);
        
        // Actualizar método de pago si es diferente
        if (!metodoPagoId.equals(suscripcionExistente.getMetodoPagoId())) {
          nuevaSuscripcion = suscripcionService.cambiarMetodoPago(nuevaSuscripcion.getId(), metodoPagoId);
        }
        
      } else {
        // Usuario no tiene suscripción, crear nueva
        System.out.println("🔍 DEBUG - Creando nueva suscripción para usuario");
          SuscripcionRequestDTO nuevaSuscripcionRequest = new SuscripcionRequestDTO();
        nuevaSuscripcionRequest.setUsuarioId(usuario.getId());
        nuevaSuscripcionRequest.setPlanId(planId);
        nuevaSuscripcionRequest.setEstado(ESTADO_ACTIVA);
        nuevaSuscripcionRequest.setFechaInicio(LocalDate.now());
        nuevaSuscripcionRequest.setModalidadPago(modalidad); // Guardar la modalidad elegida

        // Calcular fecha de renovación según modalidad
        if (MODALIDAD_ANUAL.equals(modalidad)) {
          nuevaSuscripcionRequest.setFechaRenovacion(LocalDate.now().plusYears(1));
        } else {
          nuevaSuscripcionRequest.setFechaRenovacion(LocalDate.now().plusMonths(1));
        }

        // Si tiene días de prueba y es una nueva suscripción
        if (plan.getDiasPrueba() > 0) {
          nuevaSuscripcionRequest.setEstado(ESTADO_TRIAL);
          nuevaSuscripcionRequest.setFechaRenovacion(LocalDate.now().plusDays(plan.getDiasPrueba()));
        }

        nuevaSuscripcionRequest.setMetodoPagoId(metodoPagoId);
        
        // Crear nueva suscripción usando el servicio
        nuevaSuscripcion = suscripcionService.crearSuscripcion(nuevaSuscripcionRequest);
      }      // Crear pago para la suscripción
      try {
        // Calcular precio según modalidad
        BigDecimal precio = MODALIDAD_ANUAL.equals(modalidad)
            ? BigDecimal.valueOf(plan.getPrecioAnual()).divide(BigDecimal.valueOf(100))
            : BigDecimal.valueOf(plan.getPrecioMensual()).divide(BigDecimal.valueOf(100));

        PagoRequestDTO pagoRequest = PagoRequestDTO.builder()
            .suscripcionId(nuevaSuscripcion.getId())
            .metodoPagoId(metodoPagoId)
            .monto((int) (precio.doubleValue() * 100)) // Convertir a centavos
            .esSimulado(true) // Marcar como simulado para entorno de desarrollo
            .simularFallo(false)
            .build();

        PagoResponseDTO pagoResponse = pagoService.registrarPago(pagoRequest);
        
        // Procesar el pago automáticamente para suscripciones
        pagoService.procesarPago(pagoResponse.getId());
        
        // Log del resultado del pago
        System.out.println("🔍 DEBUG - Pago procesado: " + pagoResponse.getEstado());
        
      } catch (Exception e) {
        System.out.println("⚠️ WARNING - Error al procesar pago: " + e.getMessage());
        // No fallar la suscripción por un error en el pago
      }// Mensaje de éxito
      String mensaje;
      if (suscripcionOpt.isPresent()) {
        // Cambio de plan
        mensaje = "¡Plan cambiado exitosamente! Ya puedes disfrutar de tu nuevo plan " + plan.getNombre() + ".";
      } else if (plan.getDiasPrueba() > 0) {
        // Nueva suscripción con período de prueba
        mensaje = "¡Suscripción creada exitosamente! Tienes " + plan.getDiasPrueba() + " días de prueba gratis.";
      } else {
        // Nueva suscripción sin período de prueba
        mensaje = "¡Suscripción creada exitosamente! Ya puedes disfrutar de tu nuevo plan.";
      }

      redirectAttributes.addFlashAttribute("mensaje", mensaje);
      redirectAttributes.addFlashAttribute("tipoMensaje", "success");

      return "redirect:/mi-cuenta/suscripcion";

    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("mensaje", "Error al procesar la suscripción: " + e.getMessage());
      redirectAttributes.addFlashAttribute("tipoMensaje", "error");
      return "redirect:/checkout/plan/" + planId + "?modalidad=" + modalidad;
    }
  }

  /**
   * Página de confirmación de suscripción
   */
  @GetMapping("/confirmacion")
  public String paginaConfirmacion(Model model) {
    // Verificar autenticación
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
      return "redirect:/auth/login";
    }

    Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(auth.getName());
    if (usuarioOpt.isEmpty()) {
      return "redirect:/auth/login";
    }
    Usuario usuario = usuarioOpt.get();

    Optional<SuscripcionResponseDTO> suscripcionOpt = suscripcionService
        .obtenerSuscripcionActivaPorUsuario(usuario.getId());

    if (suscripcionOpt.isPresent()) {
      SuscripcionResponseDTO suscripcion = suscripcionOpt.get();
      Optional<PlanResponseDTO> planOpt = planService.obtenerPlanPorId(suscripcion.getPlanId());

      model.addAttribute("suscripcion", suscripcion);
      if (planOpt.isPresent()) {
        model.addAttribute("plan", planOpt.get());
      }
    }

    return "public/checkout/confirmacion";
  }
}
