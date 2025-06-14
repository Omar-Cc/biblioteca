package com.biblioteca.controller;

import com.biblioteca.dto.ContenidoResponseDTO;
import com.biblioteca.service.ContenidoService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

  private final ContenidoService contenidoService;
  // private final PlanService planService;

  @GetMapping("/")
  public String index(Model model, HttpSession session,
      @RequestParam(required = false) String perfilActivado) {
    // Verificar si venimos de activar un perfil
    if ("true".equals(perfilActivado)) {
      // Verificar que haya un perfil activo
      Object perfilActivoId = session.getAttribute("perfilActivoId");
      if (perfilActivoId != null) {
        // Reforzar la persistencia de la sesión
        session.setAttribute("timestampHome", System.currentTimeMillis());
      } else {
        System.out.println("ADVERTENCIA: Perfil no encontrado en sesión después de activación");
      }
    }

    /*
     * // Obtener planes destacados (por ejemplo, los 3 más populares)
     * List<Plan> planesDestacados = planService.obtenerPlanesDestacados(3);
     * model.addAttribute("planesDestacados", planesDestacados);
     * 
     * // Plan más popular para CTA
     * Plan planMasPopular = planService.obtenerPlanMasPopular();
     * model.addAttribute("planMasPopular", planMasPopular);
     * 
     * // Beneficios por plan
     * Map<Long, List<PlanBeneficio>> beneficiosPorPlan =
     * planBeneficioService.obtenerBeneficiosPorPlan();
     * model.addAttribute("beneficiosPorPlan", beneficiosPorPlan);
     */

    // Cargar contenidos destacados y configurar navbar
    cargarContenidosDestacados(model);
    model.addAttribute("activeTab", "home");

    return "public/index";
  }

  /**
   * Página "Acerca de Nosotros"
   * Información sobre la empresa, misión, visión, equipo
   */
  @GetMapping("/nosotros")
  public String nosotros(Model model) {
    model.addAttribute("activeTab", "nosotros");

    // Agregar estadísticas de la plataforma (puedes obtenerlas de servicios reales)
    model.addAttribute("totalLibros", "50,000+");
    model.addAttribute("usuariosActivos", "25,000+");
    model.addAttribute("autoresColaboradores", "500+");
    model.addAttribute("paisesAlcance", "15");

    return "public/nosotros";
  }

  /**
   * Página de Contacto
   * Formulario de contacto e información de la empresa
   */
  @GetMapping("/contacto")
  public String contacto(Model model) {
    model.addAttribute("activeTab", "contacto");

    // Información de contacto de la empresa
    model.addAttribute("direccion", "Av. Tecnológica 123, Lima, Perú 15001");
    model.addAttribute("telefono1", "+51 987 654 321");
    model.addAttribute("telefono2", "+51 1 456 7890");
    model.addAttribute("emailContacto", "contacto@bibliovirtual.com");
    model.addAttribute("emailSoporte", "soporte@bibliovirtual.com");

    // Horarios de atención
    model.addAttribute("horarioSemana", "9:00 AM - 6:00 PM");
    model.addAttribute("horarioSabado", "10:00 AM - 4:00 PM");
    model.addAttribute("horarioDomingo", "Cerrado");

    return "public/contacto";
  }

  /**
   * Página de Términos y Condiciones
   * Documentos legales sobre el uso de la plataforma
   */
  @GetMapping("/terminos-y-condiciones")
  public String terminos(Model model) {
    model.addAttribute("activeTab", "legal");

    // Información de versión y fechas
    model.addAttribute("fechaActualizacion", "15 de Noviembre, 2023");
    model.addAttribute("versionTerminos", "v2.1");
    model.addAttribute("emailLegal", "legal@bibliovirtual.com");

    return "public/terminos";
  }

  /**
   * Página de Política de Privacidad
   * Información sobre tratamiento de datos personales
   */
  @GetMapping("/politica-de-privacidad")
  public String privacidad(Model model) {
    model.addAttribute("activeTab", "legal");

    // Información sobre privacidad y protección de datos
    model.addAttribute("fechaActualizacionPrivacidad", "15 de Noviembre, 2023");
    model.addAttribute("emailPrivacidad", "privacidad@bibliovirtual.com");
    model.addAttribute("emailDPO", "dpo@bibliovirtual.com"); // Data Protection Officer
    model.addAttribute("responsablePrivacidad", "Ana García");

    // Certificaciones y cumplimiento
    model.addAttribute("cumpleGDPR", true);
    model.addAttribute("cumpleLGPD", true);
    model.addAttribute("cumpleLOPD", true); // Ley Peruana de Protección de Datos

    return "public/privacidad";
  }

  /**
   * Página de Licencias de Contenido
   * Información sobre tipos de licencias y uso del contenido
   */
  @GetMapping("/licencias-de-contenido")
  public String licencias(Model model) {
    model.addAttribute("activeTab", "legal");

    // Información sobre licencias
    model.addAttribute("fechaActualizacionLicencias", "15 de Noviembre, 2023");
    model.addAttribute("emailLegal", "legal@bibliovirtual.com");
    model.addAttribute("emailInstitucional", "institucional@bibliovirtual.com");
    model.addAttribute("emailAcademico", "academico@bibliovirtual.com");

    // Tipos de licencias disponibles
    model.addAttribute("tienenPrestamoDigital", true);
    model.addAttribute("tienenCompraPermanente", true);
    model.addAttribute("tienenSuscripcion", true);
    model.addAttribute("tienenAccesoAbierto", true);
    model.addAttribute("tienenLicenciaAcademica", true);

    return "public/licencias";
  }

  @GetMapping("/loader")
  public String loader() {
    return "/loader";
  }

  // Métodos auxiliares para separar responsabilidades
  private void cargarContenidosDestacados(Model model) {
    try {
      List<ContenidoResponseDTO> contenidosDestacados = contenidoService.obtenerContenidosDestacados();
      model.addAttribute("contenidosDestacados", contenidosDestacados);
    } catch (Exception e) {
      model.addAttribute("contenidosDestacados", Collections.emptyList());
      model.addAttribute("errorMessage", "No se pudieron cargar los contenidos destacados");
    }
  }

}