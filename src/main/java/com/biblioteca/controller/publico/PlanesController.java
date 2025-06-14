package com.biblioteca.controller.publico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.biblioteca.dto.comercial.PlanResponseDTO;
import com.biblioteca.dto.comercial.PlanBeneficioResponseDTO;
import com.biblioteca.dto.comercial.SuscripcionResponseDTO;
import com.biblioteca.models.acceso.Usuario;
import com.biblioteca.service.PlanBeneficioService;
import com.biblioteca.service.PlanService;
import com.biblioteca.service.SuscripcionService;
import com.biblioteca.service.UsuarioService;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/planes")
@RequiredArgsConstructor
public class PlanesController {

  private final PlanService planService;
  private final PlanBeneficioService planBeneficioService;
  private final SuscripcionService suscripcionService;
  private final UsuarioService usuarioService;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  static class CeldaPlanView {
    private boolean incluido;
    private String valor;
    private String tipoDatoBeneficio;
  }

  @Data
  @NoArgsConstructor
  static class FilaCaracteristicaView {
    private String nombreCaracteristica;
    private List<CeldaPlanView> celdasPorPlan = new ArrayList<>();
    private String icono; // Para los iconos en la tabla comparativa

    public FilaCaracteristicaView(String nombreCaracteristica) {
      this.nombreCaracteristica = nombreCaracteristica;
    }

    public FilaCaracteristicaView(String nombreCaracteristica, String icono) {
      this.nombreCaracteristica = nombreCaracteristica;
      this.icono = icono;
    }
  }

  @GetMapping
  public String listarPlanes(
      @RequestParam(value = "modalidad", required = false, defaultValue = "mensual") String modalidad,
      Model model, HttpSession session) {

    List<PlanResponseDTO> planes = planService.obtenerPlanesActivos();

    Map<Long, List<PlanBeneficioResponseDTO>> beneficiosPorPlanMap = new HashMap<>();
    Set<String> nombresBeneficiosUnicos = new LinkedHashSet<>();

    for (PlanResponseDTO plan : planes) {
      List<PlanBeneficioResponseDTO> beneficiosDelPlan = planBeneficioService.obtenerBeneficiosPorPlanId(plan.getId());
      beneficiosPorPlanMap.put(plan.getId(), beneficiosDelPlan);
      beneficiosDelPlan.stream()
          .filter(pb -> pb.isActivo() && pb.getBeneficio() != null && pb.getBeneficio().isActivo())
          .map(pb -> pb.getBeneficio().getNombre())
          .forEach(nombresBeneficiosUnicos::add);
    }

    List<String> nombresBeneficiosOrdenados = new ArrayList<>(nombresBeneficiosUnicos);
    nombresBeneficiosOrdenados.sort(String.CASE_INSENSITIVE_ORDER);

    // Construir la estructura para la tabla comparativa con iconos
    List<FilaCaracteristicaView> filasTablaComparativa = new ArrayList<>();
    for (String nombreBeneficio : nombresBeneficiosOrdenados) {
      // Asignar iconos basados en el tipo de beneficio
      String icono = determinarIconoBeneficio(nombreBeneficio);
      FilaCaracteristicaView filaView = new FilaCaracteristicaView(nombreBeneficio, icono);

      for (PlanResponseDTO plan : planes) {
        List<PlanBeneficioResponseDTO> beneficiosDelPlanActual = beneficiosPorPlanMap.get(plan.getId());
        PlanBeneficioResponseDTO beneficioEncontrado = null;

        if (beneficiosDelPlanActual != null) {
          beneficioEncontrado = beneficiosDelPlanActual.stream()
              .filter(pb -> pb.isActivo() &&
                  pb.getBeneficio() != null &&
                  pb.getBeneficio().isActivo() &&
                  nombreBeneficio.equals(pb.getBeneficio().getNombre()))
              .findFirst().orElse(null);
        }

        CeldaPlanView celdaView;
        if (beneficioEncontrado != null) {
          celdaView = new CeldaPlanView(
              true,
              beneficioEncontrado.getValor(),
              beneficioEncontrado.getBeneficio().getTipoDato());
        } else {
          celdaView = new CeldaPlanView(false, null, null);
        }
        filaView.getCeldasPorPlan().add(celdaView);
      }
      filasTablaComparativa.add(filaView);
    }

    // Determinar si mostrar precios anuales o mensuales
    boolean esAnual = "anual".equalsIgnoreCase(modalidad);
    
    // Crear mapa de precios por planId para facilitar comparaciones
    Map<Long, Integer> preciosPorPlanId = new HashMap<>();
    for (PlanResponseDTO plan : planes) {
      preciosPorPlanId.put(plan.getId(), plan.getPrecioMensual());
    }
    
    // Obtener información de suscripción del usuario si está autenticado
    SuscripcionResponseDTO suscripcionActual = null;
    Long planIdActual = null;
    boolean usuarioAutenticado = false;
    boolean esPerfilPrincipal = false;
    
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null && authentication.isAuthenticated() && 
          !"anonymousUser".equals(authentication.getName())) {
        usuarioAutenticado = true;
        
        // Verificar si es perfil principal
        esPerfilPrincipal = esPerfilPrincipal(session);
        
        String username = authentication.getName();
        Usuario usuario = usuarioService.buscarPorUsername(username).orElse(null);
        if (usuario != null) {
          suscripcionActual = suscripcionService.obtenerSuscripcionActivaPorUsuario(usuario.getId()).orElse(null);
          if (suscripcionActual != null) {
            planIdActual = suscripcionActual.getPlanId();
          }
        }
      }
    } catch (Exception e) {
      // Si hay error, continuar sin información de suscripción
    }

    model.addAttribute("planes", planes);
    model.addAttribute("beneficiosPorPlan", beneficiosPorPlanMap);
    model.addAttribute("filasTablaComparativa", filasTablaComparativa);
    model.addAttribute("modalidadPago", modalidad);
    model.addAttribute("esModalidadAnual", esAnual);
    model.addAttribute("activeTab", "planes");
    model.addAttribute("usuarioAutenticado", usuarioAutenticado);
    model.addAttribute("suscripcionActual", suscripcionActual);
    model.addAttribute("planIdActual", planIdActual);
    model.addAttribute("preciosPorPlanId", preciosPorPlanId);
    model.addAttribute("esPerfilPrincipal", esPerfilPrincipal);

    return "public/planes/lista";
  }

  @GetMapping("/{id}")
  public String verDetallePlan(
      @PathVariable Long id,
      @RequestParam(value = "modalidad", required = false, defaultValue = "mensual") String modalidad,
      Model model, HttpSession session) {

    if (planService.obtenerPlanPorId(id).isEmpty()) {
      return "redirect:/planes?error=Plan+no+encontrado";
    }

    // Determinar si mostrar precios anuales o mensuales
    boolean esAnual = "anual".equalsIgnoreCase(modalidad);
    
    // Crear mapa de precios para comparaciones (necesario para el template)
    List<PlanResponseDTO> todosLosPlanes = planService.obtenerPlanesActivos();
    Map<Long, Integer> preciosPorPlanId = new HashMap<>();
    for (PlanResponseDTO plan : todosLosPlanes) {
      preciosPorPlanId.put(plan.getId(), plan.getPrecioMensual());
    }
    
    // Obtener información de suscripción del usuario si está autenticado
    SuscripcionResponseDTO suscripcionActual = null;
    boolean usuarioAutenticado = false;
    boolean tieneEstePlan = false;
    boolean esPerfilPrincipal = false;
    
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null && authentication.isAuthenticated() && 
          !"anonymousUser".equals(authentication.getName())) {
        usuarioAutenticado = true;
        
        // Verificar si es perfil principal
        esPerfilPrincipal = esPerfilPrincipal(session);
        
        String username = authentication.getName();
        Usuario usuario = usuarioService.buscarPorUsername(username).orElse(null);
        if (usuario != null) {
          suscripcionActual = suscripcionService.obtenerSuscripcionActivaPorUsuario(usuario.getId()).orElse(null);
          if (suscripcionActual != null && suscripcionActual.getPlanId().equals(id)) {
            tieneEstePlan = true;
          }
        }
      }
    } catch (Exception e) {
      // Si hay error, continuar sin información de suscripción
    }

    model.addAttribute("plan", planService.obtenerPlanPorId(id).get());
    model.addAttribute("beneficiosAsociados", planBeneficioService.obtenerBeneficiosPorPlanId(id));
    model.addAttribute("modalidadPago", modalidad);
    model.addAttribute("esModalidadAnual", esAnual);
    model.addAttribute("activeTab", "planes");
    model.addAttribute("usuarioAutenticado", usuarioAutenticado);
    model.addAttribute("suscripcionActual", suscripcionActual);
    model.addAttribute("tieneEstePlan", tieneEstePlan);
    model.addAttribute("preciosPorPlanId", preciosPorPlanId);
    model.addAttribute("esPerfilPrincipal", esPerfilPrincipal);

    return "public/planes/detalle";
  }

  /**
   * Determina el icono apropiado basado en el nombre del beneficio
   */
  private String determinarIconoBeneficio(String nombreBeneficio) {
    String nombre = nombreBeneficio.toLowerCase();

    if (nombre.contains("libro") || nombre.contains("catálogo") || nombre.contains("título")) {
      return "book";
    } else if (nombre.contains("préstamo") || nombre.contains("simultáneo")) {
      return "collection";
    } else if (nombre.contains("descarga") || nombre.contains("offline")) {
      return "download";
    } else if (nombre.contains("dispositivo") || nombre.contains("acceso")) {
      return "devices";
    } else if (nombre.contains("soporte") || nombre.contains("ayuda")) {
      return "headset";
    } else if (nombre.contains("perfil") || nombre.contains("usuario") || nombre.contains("familia")) {
      return "people";
    } else if (nombre.contains("almacenamiento") || nombre.contains("espacio")) {
      return "hdd";
    } else if (nombre.contains("velocidad") || nombre.contains("rapidez")) {
      return "speedometer2";
    } else if (nombre.contains("calidad") || nombre.contains("resolución")) {
      return "gem";
    } else if (nombre.contains("anuncio") || nombre.contains("publicidad")) {
      return "shield-check";
    } else {
      return "check-circle";
    }
  }

  /**
   * Método para verificar si el perfil activo es el perfil principal
   */
  private boolean esPerfilPrincipal(HttpSession session) {
    Boolean esPerfilPrincipal = (Boolean) session.getAttribute("esPerfilPrincipal");
    return esPerfilPrincipal != null && esPerfilPrincipal;
  }
}