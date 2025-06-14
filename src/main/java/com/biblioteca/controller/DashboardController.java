package com.biblioteca.controller;

import com.biblioteca.dto.dashboard.DashboardEstadisticasDTO;
import com.biblioteca.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            DashboardEstadisticasDTO estadisticas = dashboardService.obtenerDashboardCompleto();
            model.addAttribute("estadisticas", estadisticas);
            
            model.addAttribute("actividades", estadisticas.getActividades() != null ? 
                estadisticas.getActividades() : Collections.emptyList());
            model.addAttribute("obrasPopulares", estadisticas.getObrasPopulares() != null ? 
                estadisticas.getObrasPopulares() : Collections.emptyList());
            
        } catch (Exception e) {
            log.error("Error cargando dashboard: {}", e.getMessage());
            model.addAttribute("error", "Error al cargar los datos del dashboard");
            
            DashboardEstadisticasDTO estadisticasVacias = DashboardEstadisticasDTO.builder()
                .totalUsuarios(0L)
                .nuevosUsuariosMes(0L)
                .totalObras(0L)
                .nuevasObrasMes(0L)
                .totalPrestamos(0L)
                .nuevosPrestamos(0L)
                .totalGeneros(0L)
                .prestamosPorMes(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
                .obrasPorGeneroJS(Map.of("Sin datos", 1))
                .build();
            
            model.addAttribute("estadisticas", estadisticasVacias);
            model.addAttribute("actividades", Collections.emptyList());
            model.addAttribute("obrasPopulares", Collections.emptyList());
        }

        return "admin/dashboard";
    }

    @GetMapping("/dashboard/refresh")
    @ResponseBody
    public ResponseEntity<DashboardEstadisticasDTO> refreshDashboard(
        @RequestParam(defaultValue = "12M") String periodo
    ) {
        try {
            DashboardEstadisticasDTO estadisticas = dashboardService.refrescarDashboard(periodo);
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            log.error("Error refrescando dashboard: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard/periodo/{periodo}")
    @ResponseBody
    public ResponseEntity<DashboardEstadisticasDTO> obtenerDashboardPorPeriodo(
        @PathVariable String periodo
    ) {
        try {
            DashboardEstadisticasDTO estadisticas = dashboardService.obtenerDashboardConPeriodo(periodo);
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            log.error("Error obteniendo dashboard para período {}: {}", periodo, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/dashboard/limpiar-cache")
    @ResponseBody
    public ResponseEntity<Map<String, String>> limpiarCache() {
        try {
            dashboardService.limpiarCacheDashboard();
            return ResponseEntity.ok(Map.of("mensaje", "Caché limpiado exitosamente"));
            
        } catch (Exception e) {
            log.error("Error limpiando caché: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error limpiando caché"));
        }
    }
}