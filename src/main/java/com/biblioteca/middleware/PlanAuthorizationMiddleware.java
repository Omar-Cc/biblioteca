package com.biblioteca.middleware;

import com.biblioteca.service.PlanBeneficioService;
import com.biblioteca.service.PlanService;
import com.biblioteca.service.UsuarioService;
import com.biblioteca.dto.comercial.PlanBeneficioResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanAuthorizationMiddleware {

  private final UsuarioService usuarioService;
  private final PlanBeneficioService planBeneficioService;
  private final PlanService planService;

  /**
   * Verifica si un usuario puede acceder a un recurso específico basado en su
   * plan actual
   */
  public boolean puedeAccederRecurso(Long usuarioId, String recurso) {
    try {
      return usuarioService.obtenerPlanActualUsuario(usuarioId)
          .map(planNombre -> verificarAccesoPorPlan(planNombre, recurso))
          .orElse(false);
    } catch (Exception e) {
      log.error("Error verificando acceso al recurso {} para usuario {}: {}",
          recurso, usuarioId, e.getMessage());
      return false;
    }
  }

  /**
   * Verifica acceso basado en el plan del usuario
   */
  private boolean verificarAccesoPorPlan(String planNombre, String recurso) {
    // Obtener el ID del plan por nombre (necesitarás agregar este método)
    Optional<Long> planId = obtenerPlanIdPorNombre(planNombre);

    if (planId.isEmpty()) {
      log.warn("No se encontró plan con nombre: {}", planNombre);
      return false;
    }

    return verificarAccesoPorPlanId(planId.get(), recurso);
  }

  /**
   * Verifica si un plan específico tiene acceso a un recurso
   */
  public boolean verificarAccesoPorPlanId(Long planId, String recurso) {
    List<PlanBeneficioResponseDTO> beneficios = planBeneficioService.obtenerBeneficiosPorPlanId(planId);

    return beneficios.stream()
        .filter(pb -> pb.isActivo()) // Solo beneficios activos
        .anyMatch(pb -> tieneAccesoARecurso(pb, recurso));
  }

  /**
   * Determina si un beneficio específico otorga acceso a un recurso
   */
  private boolean tieneAccesoARecurso(PlanBeneficioResponseDTO planBeneficio, String recurso) {
    String nombreBeneficio = planBeneficio.getBeneficio().getNombre();
    String valor = planBeneficio.getValor();

    return switch (recurso) {
      // Recursos de catálogo
      case "leer_muestra" -> true; // Todos los planes
      case "buscar_catalogo" -> true; // Todos los planes
      case "leer_completo" -> tieneBeneficioAccesoDigital(planBeneficio);

      // Recursos de descarga
      case "descargar_libro" -> tieneDescargasDisponibles(planBeneficio);
      case "lectura_offline" -> tieneDescargasDisponibles(planBeneficio);

      // Recursos de audio
      case "acceso_audiolibros" -> tieneAccesoAudiolibros(planBeneficio);

      // Recursos sociales
      case "marcar_favoritos" -> tieneFuncionalidadesBasicas(planBeneficio);
      case "crear_listas" -> tieneFuncionalidadesBasicas(planBeneficio);
      case "grupos_lectura" -> tieneAccesoGrupos(planBeneficio);

      // Recursos premium
      case "acceso_anticipado" -> tieneAccesoAnticipado(planBeneficio);
      case "contenido_exclusivo" -> tieneAccesoAnticipado(planBeneficio);
      case "recomendaciones_ia" -> tieneRecomendacionesPersonalizadas(planBeneficio);
      case "analisis_lectura" -> tieneEstadisticasAvanzadas(planBeneficio);
      case "sin_publicidad" -> tieneSinPublicidad(planBeneficio);

      // Recursos familiares
      case "perfiles_multiples" -> tienePerfilesMultiples(planBeneficio);
      case "control_parental" -> tieneControlParental(planBeneficio);
      case "biblioteca_compartida" -> tienePerfilesMultiples(planBeneficio);

      // Recursos de funcionalidades
      case "marcadores_notas" -> tieneMarcadoresNotas(planBeneficio);
      case "sincronizacion" -> tieneSincronizacion(planBeneficio);
      case "busqueda_contenido" -> tieneBusquedaDentroLibros(planBeneficio);
      case "diccionario" -> tieneDiccionario(planBeneficio);
      case "exportar_notas" -> tieneExportacionNotas(planBeneficio);

      default -> {
        log.warn("Recurso no reconocido: {}", recurso);
        yield false;
      }
    };
  }

  // Métodos helper para verificar beneficios específicos
  private boolean tieneBeneficioAccesoDigital(PlanBeneficioResponseDTO pb) {
    return "Acceso a contenido digital".equals(pb.getBeneficio().getNombre())
        && "true".equals(pb.getValor());
  }

  private boolean tieneDescargasDisponibles(PlanBeneficioResponseDTO pb) {
    if ("Descarga para lectura offline".equals(pb.getBeneficio().getNombre())) {
      try {
        return Integer.parseInt(pb.getValor()) > 0;
      } catch (NumberFormatException e) {
        return false;
      }
    }
    return false;
  }

  private boolean tieneAccesoAudiolibros(PlanBeneficioResponseDTO pb) {
    if ("Acceso a audiolibros".equals(pb.getBeneficio().getNombre())) {
      try {
        return Integer.parseInt(pb.getValor()) > 0;
      } catch (NumberFormatException e) {
        return !"false".equals(pb.getValor()) && !pb.getValor().isEmpty();
      }
    }
    return false;
  }

  private boolean tieneFuncionalidadesBasicas(PlanBeneficioResponseDTO pb) {
    return "Marcadores y notas".equals(pb.getBeneficio().getNombre())
        && !"false".equals(pb.getValor());
  }

  private boolean tieneAccesoGrupos(PlanBeneficioResponseDTO pb) {
    return "Grupos de lectura".equals(pb.getBeneficio().getNombre())
        && !"false".equals(pb.getValor());
  }

  private boolean tieneAccesoAnticipado(PlanBeneficioResponseDTO pb) {
    return "Acceso anticipado a novedades".equals(pb.getBeneficio().getNombre())
        && "true".equals(pb.getValor());
  }

  private boolean tieneRecomendacionesPersonalizadas(PlanBeneficioResponseDTO pb) {
    return "Recomendaciones personalizadas".equals(pb.getBeneficio().getNombre())
        && "IA personalizada".equals(pb.getValor());
  }

  private boolean tieneEstadisticasAvanzadas(PlanBeneficioResponseDTO pb) {
    return "Estadísticas de lectura".equals(pb.getBeneficio().getNombre())
        && ("Completas".equals(pb.getValor()) || "Avanzadas".equals(pb.getValor()));
  }

  private boolean tieneSinPublicidad(PlanBeneficioResponseDTO pb) {
    return "Sin publicidad".equals(pb.getBeneficio().getNombre())
        && "true".equals(pb.getValor());
  }

  private boolean tienePerfilesMultiples(PlanBeneficioResponseDTO pb) {
    if ("Número de perfiles".equals(pb.getBeneficio().getNombre())) {
      try {
        return Integer.parseInt(pb.getValor()) > 1;
      } catch (NumberFormatException e) {
        return false;
      }
    }
    return false;
  }

  private boolean tieneControlParental(PlanBeneficioResponseDTO pb) {
    return "Control parental".equals(pb.getBeneficio().getNombre())
        && "true".equals(pb.getValor());
  }

  private boolean tieneMarcadoresNotas(PlanBeneficioResponseDTO pb) {
    return "Marcadores y notas".equals(pb.getBeneficio().getNombre())
        && !"false".equals(pb.getValor());
  }

  private boolean tieneSincronizacion(PlanBeneficioResponseDTO pb) {
    return "Sincronización entre dispositivos".equals(pb.getBeneficio().getNombre())
        && "true".equals(pb.getValor());
  }

  private boolean tieneBusquedaDentroLibros(PlanBeneficioResponseDTO pb) {
    return "Búsqueda dentro de libros".equals(pb.getBeneficio().getNombre())
        && "true".equals(pb.getValor());
  }

  private boolean tieneDiccionario(PlanBeneficioResponseDTO pb) {
    return "Diccionario integrado".equals(pb.getBeneficio().getNombre())
        && "true".equals(pb.getValor());
  }

  private boolean tieneExportacionNotas(PlanBeneficioResponseDTO pb) {
    return "Exportación de notas y citas".equals(pb.getBeneficio().getNombre())
        && "true".equals(pb.getValor());
  }

  /**
   * Obtiene el ID del plan por su nombre
   */
  private Optional<Long> obtenerPlanIdPorNombre(String planNombre) {
    return planService.obtenerPlanIdPorNombre(planNombre);
  }

  /**
   * Método de conveniencia para verificar múltiples recursos
   */
  public boolean puedeAccederRecursos(Long usuarioId, String... recursos) {
    for (String recurso : recursos) {
      if (!puedeAccederRecurso(usuarioId, recurso)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Obtiene todos los recursos disponibles para un usuario
   */
  public List<String> obtenerRecursosDisponibles(Long usuarioId) {
    return List.of(
        "leer_muestra", "buscar_catalogo", "leer_completo", "descargar_libro",
        "lectura_offline", "acceso_audiolibros", "marcar_favoritos", "crear_listas",
        "grupos_lectura", "acceso_anticipado", "contenido_exclusivo",
        "recomendaciones_ia", "analisis_lectura", "sin_publicidad",
        "perfiles_multiples", "control_parental", "biblioteca_compartida",
        "marcadores_notas", "sincronizacion", "busqueda_contenido",
        "diccionario", "exportar_notas").stream()
        .filter(recurso -> puedeAccederRecurso(usuarioId, recurso))
        .toList();
  }

}