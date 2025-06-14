package com.biblioteca.controller.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca.dto.comercial.SuscripcionResponseDTO;
import com.biblioteca.service.PlanService;
import com.biblioteca.service.SuscripcionService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/suscripciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSuscripcionController {

    private final SuscripcionService suscripcionService;
    private final PlanService planService;

    @GetMapping
    public String listarTodasLasSuscripciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaInicio") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String planId,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) String busqueda,
            Model model) {

        try {
            // Obtener todas las suscripciones
            List<SuscripcionResponseDTO> todasLasSuscripciones = suscripcionService.obtenerTodasLasSuscripciones();

            // Filtrar por estado si se especifica
            if (estado != null && !estado.trim().isEmpty()) {
                todasLasSuscripciones = todasLasSuscripciones.stream()
                        .filter(s -> estado.equals(s.getEstado()))
                        .collect(java.util.stream.Collectors.toList());
            }

            // Filtrar por planId si se especifica
            if (planId != null && !planId.trim().isEmpty()) {
                try {
                    Long planIdLong = Long.parseLong(planId);
                    todasLasSuscripciones = todasLasSuscripciones.stream()
                            .filter(s -> planIdLong.equals(s.getPlanId()))
                            .collect(java.util.stream.Collectors.toList());
                } catch (NumberFormatException e) {
                    // Ignorar filtro por planId si no es un número válido
                }
            }

            // Filtrar por fechas si se especifican
            if (fechaDesde != null && !fechaDesde.trim().isEmpty()) {
                try {
                    LocalDate fechaDesdeDate = LocalDate.parse(fechaDesde);
                    todasLasSuscripciones = todasLasSuscripciones.stream()
                            .filter(s -> s.getFechaInicio() == null || !s.getFechaInicio().isBefore(fechaDesdeDate))
                            .collect(java.util.stream.Collectors.toList());
                } catch (Exception e) {
                    // Ignorar filtro de fecha si no es válida
                }
            }

            if (fechaHasta != null && !fechaHasta.trim().isEmpty()) {
                try {
                    LocalDate fechaHastaDate = LocalDate.parse(fechaHasta);
                    todasLasSuscripciones = todasLasSuscripciones.stream()
                            .filter(s -> s.getFechaInicio() == null || !s.getFechaInicio().isAfter(fechaHastaDate))
                            .collect(java.util.stream.Collectors.toList());
                } catch (Exception e) {
                    // Ignorar filtro de fecha si no es válida
                }
            }

            // Calcular estadísticas
            long suscripcionesActivas = todasLasSuscripciones.stream()
                    .filter(s -> "ACTIVA".equals(s.getEstado())).count();
            long suscripcionesPendientes = todasLasSuscripciones.stream()
                    .filter(s -> "PENDIENTE".equals(s.getEstado())).count();
            long suscripcionesVencidas = todasLasSuscripciones.stream()
                    .filter(s -> "VENCIDA".equals(s.getEstado())).count();
            long suscripcionesCanceladas = todasLasSuscripciones.stream()
                    .filter(s -> "CANCELADA".equals(s.getEstado())).count();
            long suscripcionesPausadas = todasLasSuscripciones.stream()
                    .filter(s -> "PAUSADA".equals(s.getEstado())).count();

            // Estadísticas para la vista
            Map<String, Object> estadisticas = new java.util.HashMap<>();
            estadisticas.put("totalSuscripciones", todasLasSuscripciones.size());
            estadisticas.put("activas", suscripcionesActivas);
            estadisticas.put("pendientes", suscripcionesPendientes);
            estadisticas.put("vencidas", suscripcionesVencidas);
            estadisticas.put("canceladas", suscripcionesCanceladas);
            estadisticas.put("pausadas", suscripcionesPausadas);

            model.addAttribute("suscripciones", todasLasSuscripciones);
            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("planes", planService.obtenerTodosLosPlanes());

            // Preparar parámetros para mantener los filtros en la vista
            model.addAttribute("filtros", Map.of(
                    "estado", estado != null ? estado : "",
                    "planId", planId != null ? planId : "",
                    "fechaDesde", fechaDesde != null ? fechaDesde : "",
                    "fechaHasta", fechaHasta != null ? fechaHasta : "",
                    "busqueda", busqueda != null ? busqueda : "",
                    "sort", sort,
                    "direction", direction));

            model.addAttribute("activeTab", "suscripciones");

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar las suscripciones: " + e.getMessage());
            model.addAttribute("suscripciones", List.of());
            model.addAttribute("estadisticas", Map.of(
                    "totalSuscripciones", 0,
                    "activas", 0,
                    "pendientes", 0,
                    "vencidas", 0,
                    "canceladas", 0,
                    "pausadas", 0));
            model.addAttribute("planes", List.of());
        }

        return "admin/suscripciones/lista";
    }

    @GetMapping("/por-vencer")
    public String listarSuscripcionesPorVencer(
            @RequestParam(defaultValue = "7") int dias,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        try {
            List<SuscripcionResponseDTO> suscripciones = suscripcionService.obtenerSuscripcionesPorVencer(dias);

            Map<String, Object> estadisticas = new java.util.HashMap<>();
            estadisticas.put("totalPorVencer", suscripciones.size());
            estadisticas.put("en1Dia", suscripciones.stream()
                    .filter(s -> s.getDiasParaRenovacion() != null && s.getDiasParaRenovacion() <= 1)
                    .count());
            estadisticas.put("en3Dias", suscripciones.stream()
                    .filter(s -> s.getDiasParaRenovacion() != null && s.getDiasParaRenovacion() <= 3)
                    .count());

            model.addAttribute("suscripciones", suscripciones);
            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("diasRestantes", dias);
            model.addAttribute("activeTab", "suscripciones");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar suscripciones por vencer: " + e.getMessage());
            model.addAttribute("suscripciones", List.of());
        }

        return "admin/suscripciones/por-vencer";
    }

    @GetMapping("/vencidas")
    public String listarSuscripcionesVencidas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        try {
            List<SuscripcionResponseDTO> suscripciones = suscripcionService.obtenerSuscripcionesVencidas();

            Map<String, Object> estadisticas = new java.util.HashMap<>();
            estadisticas.put("totalVencidas", suscripciones.size());
            estadisticas.put("vencidasRecientes", suscripciones.stream()
                    .filter(s -> s.getDiasParaRenovacion() != null && s.getDiasParaRenovacion() >= -30)
                    .count());

            model.addAttribute("suscripciones", suscripciones);
            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("activeTab", "suscripciones");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar suscripciones vencidas: " + e.getMessage());
            model.addAttribute("suscripciones", List.of());
        }

        return "admin/suscripciones/vencidas";
    }

    @GetMapping("/{id}")
    public String verDetalleSuscripcion(@PathVariable Long id, Model model) {
        return suscripcionService.obtenerSuscripcionPorId(id)
                .map(suscripcion -> {
                    Map<String, Object> estadisticas = new java.util.HashMap<>();
                    estadisticas.put("diasActiva", suscripcion.getDiasTranscurridos());
                    estadisticas.put("estado", suscripcion.getEstado());
                    estadisticas.put("planNombre", suscripcion.getPlanNombre());
                    estadisticas.put("modalidadPago", suscripcion.getModalidadPago());

                    model.addAttribute("suscripcion", suscripcion);
                    model.addAttribute("estadisticas", estadisticas);
                    model.addAttribute("historialPagos", suscripcion.getHistorialPagos());
                    model.addAttribute("activeTab", "suscripciones");
                    return "admin/suscripciones/detalle";
                })
                .orElse("redirect:/admin/suscripciones?error=no-encontrada");
    }

    @PostMapping("/{id}/extender")
    @ResponseBody
    public ResponseEntity<?> extenderSuscripcion(
            @PathVariable Long id,
            @RequestParam int meses,
            @RequestParam(required = false) String motivo) {
        try {
            suscripcionService.renovarSuscripcion(id);
            return ResponseEntity.ok().body(
                    Map.of("mensaje", "Suscripción renovada exitosamente (funcionalidad de extensión en desarrollo)"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/pausar")
    @ResponseBody
    public ResponseEntity<?> pausarSuscripcion(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        try {
            boolean resultado = suscripcionService.pausarSuscripcion(id);
            if (resultado) {
                return ResponseEntity.ok().body(Map.of("mensaje", "Suscripción pausada exitosamente"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "No se pudo pausar la suscripción"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reactivar")
    @ResponseBody
    public ResponseEntity<?> reactivarSuscripcion(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        try {
            boolean resultado = suscripcionService.reactivarSuscripcion(id);
            if (resultado) {
                return ResponseEntity.ok().body(Map.of("mensaje", "Suscripción reactivada exitosamente"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "No se pudo reactivar la suscripción"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancelar")
    @ResponseBody
    public ResponseEntity<?> cancelarSuscripcion(
            @PathVariable Long id,
            @RequestParam String motivo) {
        try {
            boolean resultado = suscripcionService.cancelarSuscripcion(id);
            if (resultado) {
                return ResponseEntity.ok().body(Map.of("mensaje", "Suscripción cancelada exitosamente"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "No se pudo cancelar la suscripción"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/enviar-recordatorio")
    @ResponseBody
    public ResponseEntity<?> enviarRecordatorioVencimiento(
            @RequestParam List<Long> suscripcionIds) {
        try {
            int enviados = suscripcionIds.size();
            return ResponseEntity.ok().body(Map.of(
                    "mensaje", "Funcionalidad de recordatorios en desarrollo",
                    "cantidad", enviados));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/renovar-automaticamente")
    @ResponseBody
    public ResponseEntity<?> renovarAutomaticamente(
            @RequestParam List<Long> suscripcionIds) {
        try {
            int renovadas = 0;
            for (Long id : suscripcionIds) {
                try {
                    suscripcionService.renovarSuscripcion(id);
                    renovadas++;
                } catch (Exception e) {
                    // Continuar con la siguiente suscripción
                }
            }
            return ResponseEntity.ok().body(Map.of(
                    "mensaje", "Proceso de renovación completado",
                    "cantidad", renovadas));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/reportes/ingresos")
    @ResponseBody
    public ResponseEntity<?> obtenerReporteIngresos(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(defaultValue = "mes") String periodo) {
        try {
            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : LocalDate.now().minusMonths(6);
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : LocalDate.now();

            Map<String, Object> reporte = new java.util.HashMap<>();
            reporte.put("fechaInicio", inicio.toString());
            reporte.put("fechaFin", fin.toString());
            reporte.put("periodo", periodo);
            reporte.put("mensaje", "Funcionalidad de reportes en desarrollo");
            reporte.put("ingresosTotales", 0);
            reporte.put("suscripcionesActivas", suscripcionService.obtenerTodasLasSuscripciones().size());

            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/reportes/metricas")
    @ResponseBody
    public ResponseEntity<?> obtenerMetricasDashboard() {
        try {
            List<SuscripcionResponseDTO> todasLasSuscripciones = suscripcionService.obtenerTodasLasSuscripciones();

            Map<String, Object> metricas = new java.util.HashMap<>();
            metricas.put("totalSuscripciones", todasLasSuscripciones.size());
            metricas.put("suscripcionesActivas", todasLasSuscripciones.stream()
                    .filter(s -> "ACTIVA".equals(s.getEstado())).count());
            metricas.put("suscripcionesPausadas", todasLasSuscripciones.stream()
                    .filter(s -> "PAUSADA".equals(s.getEstado())).count());
            metricas.put("suscripcionesVencidas", todasLasSuscripciones.stream()
                    .filter(s -> "VENCIDA".equals(s.getEstado())).count());
            metricas.put("mensaje", "Métricas básicas - funcionalidad completa en desarrollo");

            return ResponseEntity.ok(metricas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/exportar")
    public String exportarSuscripciones(
            @RequestParam String formato,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String planId,
            RedirectAttributes redirectAttributes) {
        try {
            String archivoGenerado = "suscripciones_export_" + LocalDate.now().toString() + "." + formato.toLowerCase();
            redirectAttributes.addFlashAttribute("successMessage",
                    "Funcionalidad de exportación en desarrollo. Archivo simulado: " + archivoGenerado);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error al exportar: " + e.getMessage());
        }
        return "redirect:/admin/suscripciones";
    }
}