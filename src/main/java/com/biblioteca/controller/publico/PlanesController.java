package com.biblioteca.controller.publico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.biblioteca.dto.comercial.PlanResponseDTO;
import com.biblioteca.dto.comercial.PlanBeneficioResponseDTO;
import com.biblioteca.service.PlanBeneficioService;
import com.biblioteca.service.PlanService;

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

    public FilaCaracteristicaView(String nombreCaracteristica) {
      this.nombreCaracteristica = nombreCaracteristica;
    }
  }

  @GetMapping
  public String listarPlanes(Model model) {
    List<PlanResponseDTO> planes = planService.obtenerPlanesActivos();

    Map<Long, List<PlanBeneficioResponseDTO>> beneficiosPorPlanMap = new HashMap<>();
    Set<String> nombresBeneficiosUnicos = new LinkedHashSet<>();

    for (PlanResponseDTO plan : planes) {
      List<PlanBeneficioResponseDTO> beneficiosDelPlan = planBeneficioService.obtenerBeneficiosPorPlan(plan.getId());
      beneficiosPorPlanMap.put(plan.getId(), beneficiosDelPlan);
      beneficiosDelPlan.stream()
          .filter(pb -> pb.isActivo() && pb.getBeneficio() != null && pb.getBeneficio().isActivo())
          .map(pb -> pb.getBeneficio().getNombre())
          .forEach(nombresBeneficiosUnicos::add);
    }

    List<String> nombresBeneficiosOrdenados = new ArrayList<>(nombresBeneficiosUnicos);
    nombresBeneficiosOrdenados.sort(String.CASE_INSENSITIVE_ORDER);

    // Construir la estructura para la tabla comparativa
    List<FilaCaracteristicaView> filasTablaComparativa = new ArrayList<>();
    for (String nombreBeneficio : nombresBeneficiosOrdenados) {
      FilaCaracteristicaView filaView = new FilaCaracteristicaView(nombreBeneficio);
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

    model.addAttribute("planes", planes);
    model.addAttribute("beneficiosPorPlan", beneficiosPorPlanMap);
    // model.addAttribute("nombresBeneficios", nombresBeneficiosOrdenados);
    model.addAttribute("filasTablaComparativa", filasTablaComparativa);
    model.addAttribute("activeTab", "planes");
    return "public/planes/lista";
  }

  @GetMapping("/{id}")
  public String verDetallePlan(@PathVariable Long id, Model model) {
    if (planService.obtenerPlanPorId(id).isEmpty()) {
      return "redirect:/planes?error=Plan+no+encontrado";
    }

    model.addAttribute("plan", planService.obtenerPlanPorId(id).get());
    model.addAttribute("beneficiosAsociados", planBeneficioService.obtenerBeneficiosPorPlan(id));
    model.addAttribute("activeTab", "planes");

    return "public/planes/detalle";
  }
}
