package com.biblioteca.controller;

import com.biblioteca.service.PerfilService;
import com.biblioteca.service.SuscripcionService;
import com.biblioteca.service.PlanService;
import com.biblioteca.service.UsuarioService;
import com.biblioteca.models.acceso.Usuario;
import com.biblioteca.service.CarritoService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

  private final PerfilService perfilService;
  private final UsuarioService usuarioService;
  private final SuscripcionService suscripcionService;
  private final PlanService planService;
  private final CarritoService carritoService;

  @ModelAttribute
  public void configurarPerfilGlobal(Model model, HttpSession session) {
    Long perfilActivoId = (Long) session.getAttribute("perfilActivoId");
    model.addAttribute("tienePerfilActivo", perfilActivoId != null);
  }

  /**
   * 
   * Método para agregar atributos globales al modelo.
   * 
   * @param session La sesión HTTP actual.
   * @return El número total de perfiles del usuario autenticado.
   */
  @ModelAttribute("totalPerfiles")
  public Long getTotalPerfiles(HttpSession session) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    // Verificar si el usuario está autenticado y no es anónimo
    if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
      String username = auth.getName();
      return perfilService.contarPerfilesPorUsuario(username);
    }

    return 0L; // Valor predeterminado cuando no hay usuario autenticado
  }

  /**
   * Método para obtener la foto de perfil del usuario autenticado.
   * 
   * @param session La sesión HTTP actual.
   * @return La URL de la foto de perfil del usuario autenticado.
   */
  @ModelAttribute("fotoPerfil")
  public String getFotoPerfil(HttpSession session) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    // Verificar si el usuario está autenticado y no es anónimo
    if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
      String username = auth.getName();

      // Obtener el ID del perfil activo desde la sesión
      Long perfilActivoId = (Long) session.getAttribute("perfilActivoId");

      // Si hay un perfil activo, obtener su foto
      if (perfilActivoId != null) {
        String fotoPerfil = perfilService.obtenerPerfilPorIdYUsuario(perfilActivoId, username)
            .map(perfil -> perfil.getFotoPerfil())
            .orElse(null);

        // Asegurarnos de que la foto no sea una cadena vacía
        return (fotoPerfil != null && !fotoPerfil.isEmpty()) ? fotoPerfil : null;
      } else {
        // Si no hay perfil activo, intentar obtener el perfil principal
        String fotoPerfil = perfilService.obtenerPerfilPrincipal(username)
            .map(perfil -> perfil.getFotoPerfil())
            .orElse(null);

        // Asegurarnos de que la foto no sea una cadena vacía
        return (fotoPerfil != null && !fotoPerfil.isEmpty()) ? fotoPerfil : null;
      }
    }

    return null; // Valor predeterminado cuando no hay usuario autenticado
  }

  @ModelAttribute("fechaAntiguedad")
  public String getFechaAntiguedad(HttpSession session) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    // Verificar si el usuario está autenticado y no es anónimo
    if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
      String username = auth.getName();
      Usuario usuario = usuarioService.buscarPorUsername(username)
          .orElse(null);

      if (usuario != null && usuario.getFechaRegistro() != null) {
        // Usar DateTimeFormatter para formatear la fecha
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        return usuario.getFechaRegistro().format(formatter);
      }
    }

    return null; // Valor predeterminado cuando no hay usuario autenticado
  }

  /**
   * Información completa de la suscripción del usuario
   */
  @ModelAttribute("suscripcionInfo")
  public Map<String, Object> getSuscripcionInfo(HttpSession session) {
    Map<String, Object> suscripcionInfo = new HashMap<>();

    // Valores por defecto
    suscripcionInfo.put("tieneSuscripcion", false);
    suscripcionInfo.put("nombrePlan", "Plan Básico");
    suscripcionInfo.put("clasePlan", "plan-basico");
    suscripcionInfo.put("estadoSuscripcion", "INACTIVA");
    suscripcionInfo.put("diasHastaRenovacion", 0L);
    suscripcionInfo.put("porcentajeUso", 0);
    suscripcionInfo.put("limitesDisponibles", false);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
      try {
        // La información de suscripción debe estar disponible para todos los perfiles
        // del usuario
        String username = auth.getName();
        Usuario usuario = usuarioService.buscarPorUsername(username).orElse(null);

        if (usuario != null) {
          suscripcionService.obtenerSuscripcionActivaPorUsuario(usuario.getId())
              .ifPresent(suscripcion -> {
                try {
                  suscripcionInfo.put("tieneSuscripcion", true);
                  suscripcionInfo.put("estadoSuscripcion", suscripcion.getEstado().toString());

                  // Obtener información del plan
                  planService.obtenerPlanPorId(suscripcion.getPlanId())
                      .ifPresent(plan -> {
                        String nombrePlan = plan.getNombre();
                        suscripcionInfo.put("nombrePlan", nombrePlan);
                        suscripcionInfo.put("clasePlan", nombrePlan.toLowerCase().replace(" ", "-"));
                        suscripcionInfo.put("precioMensual", plan.getPrecioMensual());
                        suscripcionInfo.put("precioAnual", plan.getPrecioAnual());
                        suscripcionInfo.put("limitesDisponibles", true);
                      });

                  // Calcular días hasta renovación
                  if (suscripcion.getFechaRenovacion() != null) {
                    long diasHastaRenovacion = java.time.temporal.ChronoUnit.DAYS.between(
                        LocalDate.now(), suscripcion.getFechaRenovacion());
                    suscripcionInfo.put("diasHastaRenovacion", Math.max(0, diasHastaRenovacion));
                  }

                  // Calcular porcentaje de uso (ejemplo básico)
                  // Esto debería calcularse basado en los límites del plan y uso actual
                  suscripcionInfo.put("porcentajeUso", calcularPorcentajeUso(suscripcion));

                } catch (Exception e) {
                  // En caso de error, mantener valores por defecto
                }
              });
        }
      } catch (Exception e) {
        // En caso de error, mantener valores por defecto
      }
    }

    return suscripcionInfo;
  }

  /**
   * Solo el nombre del plan para casos simples
   */
  @ModelAttribute("nombrePlan")
  public String getNombrePlan(HttpSession session) {
    Map<String, Object> suscripcionInfo = getSuscripcionInfo(session);
    return (String) suscripcionInfo.get("nombrePlan");
  }

  /**
   * Clase CSS del plan para estilos
   */
  @ModelAttribute("clasePlan")
  public String getClasePlan(HttpSession session) {
    Map<String, Object> suscripcionInfo = getSuscripcionInfo(session);
    return (String) suscripcionInfo.get("clasePlan");
  }

  /**
   * Estado de la suscripción
   */
  @ModelAttribute("tieneSuscripcionActiva")
  public Boolean getTieneSuscripcionActiva(HttpSession session) {
    Map<String, Object> suscripcionInfo = getSuscripcionInfo(session);
    return (Boolean) suscripcionInfo.get("tieneSuscripcion");
  }

  /**
   * Días hasta renovación
   */
  @ModelAttribute("diasHastaRenovacion")
  public Long getDiasHastaRenovacion(HttpSession session) {
    Map<String, Object> suscripcionInfo = getSuscripcionInfo(session);
    return (Long) suscripcionInfo.get("diasHastaRenovacion");
  }

  /**
   * Información de uso de la suscripción
   */
  @ModelAttribute("usoSuscripcion")
  public Map<String, Object> getUsoSuscripcion(HttpSession session) {
    Map<String, Object> usoInfo = new HashMap<>();

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
      try {
        String username = auth.getName();
        Usuario usuario = usuarioService.buscarPorUsername(username).orElse(null);

        if (usuario != null) {
          usoInfo.put("prestamosUtilizados", 0);
          usoInfo.put("prestamosDisponibles", 5);
          usoInfo.put("descargasUtilizadas", 0);
          usoInfo.put("descargasDisponibles", 10);
          usoInfo.put("porcentajeUso", 0);
        }
      } catch (Exception e) {
      }
    }

    // Valores por defecto
    if (usoInfo.isEmpty()) {
      usoInfo.put("prestamosUtilizados", 0);
      usoInfo.put("prestamosDisponibles", 0);
      usoInfo.put("descargasUtilizadas", 0);
      usoInfo.put("descargasDisponibles", 0);
      usoInfo.put("porcentajeUso", 0);
    }

    return usoInfo;
  }

  /**
   * Estadísticas del usuario
   */
  @ModelAttribute("estadisticasUsuario")
  public Map<String, Object> getEstadisticasUsuario(HttpSession session) {
    Map<String, Object> estadisticas = new HashMap<>();

    // Valores por defecto
    estadisticas.put("lecturasTotales", 0);
    estadisticas.put("totalReseñas", 0);
    estadisticas.put("totalFavoritos", 0);
    estadisticas.put("itemsEnCarrito", getCarritoItems(session));
    estadisticas.put("notificacionesSinLeer", 0);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
      try {
        String username = auth.getName();
        // Aquí puedes agregar la lógica real para calcular las estadísticas
        // Por ahora, mantengo valores de ejemplo

        // Ejemplo de cómo podrías obtener datos reales:
        // estadisticas.put("lecturasTotales",
        // lecturaService.contarLecturasPorUsuario(username));
        // estadisticas.put("totalFavoritos",
        // favoritosService.contarFavoritosPorUsuario(username));
        // etc.
        estadisticas.put("itemsEnCarrito", getCarritoItems(session));
      } catch (Exception e) {
        // Mantener valores por defecto en caso de error
      }
    }

    return estadisticas;
  }

  /**
   * Atributos individuales para facilitar el acceso en templates
   */
  @ModelAttribute("lecturasTotales")
  public Integer getLecturasTotales(HttpSession session) {
    Map<String, Object> estadisticas = getEstadisticasUsuario(session);
    return (Integer) estadisticas.get("lecturasTotales");
  }

  @ModelAttribute("totalFavoritos")
  public Integer getTotalFavoritos(HttpSession session) {
    Map<String, Object> estadisticas = getEstadisticasUsuario(session);
    return (Integer) estadisticas.get("totalFavoritos");
  }

  @ModelAttribute("totalReseñas")
  public Integer getTotalReseñas(HttpSession session) {
    Map<String, Object> estadisticas = getEstadisticasUsuario(session);
    return (Integer) estadisticas.get("totalReseñas");
  }

  @ModelAttribute("itemsEnCarrito")
  public Integer getItemsEnCarrito(HttpSession session) {
    return getCarritoItems(session);
  }

  @ModelAttribute("notificacionesSinLeer")
  public Integer getNotificacionesSinLeer(HttpSession session) {
    Map<String, Object> estadisticas = getEstadisticasUsuario(session);
    return (Integer) estadisticas.get("notificacionesSinLeer");
  }

  /**
   * Obtener la cantidad de items únicos en el carrito del perfil activo
   */
  @ModelAttribute("carritoItems")
  public Integer getCarritoItems(HttpSession session) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    // Verificar si el usuario está autenticado y no es anónimo
    if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
      try {
        // Obtener el ID del perfil activo desde la sesión
        Long perfilActivoId = (Long) session.getAttribute("perfilActivoId");

        if (perfilActivoId != null) {
          // Obtener el carrito del perfil activo
          return carritoService.obtenerCarritoPorPerfil(perfilActivoId)
              .map(carrito -> {
                if (carrito.getItems() != null) {
                  // Conteo de items únicos
                  return carrito.getItems().size();
                }
                return 0;
              })
              .orElse(0);
        }
      } catch (Exception e) {
        // En caso de error, retornar 0
        return 0;
      }
    }

    return 0; // Valor predeterminado cuando no hay usuario autenticado
  }

  /**
   * Método auxiliar para calcular porcentaje de uso
   */
  private int calcularPorcentajeUso(Object suscripcion) {
    try {
      // Aquí implementarías la lógica real para calcular el porcentaje
      // basado en los límites del plan y el uso actual
      // Por ahora retorno un valor de ejemplo
      return 45; // 45% de uso
    } catch (Exception e) {
      return 0;
    }
  }
}