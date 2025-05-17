package com.biblioteca.controller.publico;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.biblioteca.service.BeneficioService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/beneficios")
@RequiredArgsConstructor
public class BeneficiosController {

  private final BeneficioService beneficioService;

  @GetMapping
  public String listarBeneficios(Model model) {
    model.addAttribute("beneficios", beneficioService.obtenerBeneficiosActivos());
    return "beneficios/lista";
  }

  @GetMapping("/tipo")
  public String listarBeneficiosPorTipo(@RequestParam String tipoDato, Model model) {
    model.addAttribute("beneficios", beneficioService.obtenerBeneficiosPorTipo(tipoDato));
    model.addAttribute("tipoFiltrado", tipoDato);
    return "beneficios/lista";
  }

  @GetMapping("/{id}")
  public String verDetalleBeneficio(@PathVariable Long id, Model model) {
    return beneficioService.obtenerBeneficioPorId(id)
        .map(beneficio -> {
          model.addAttribute("beneficio", beneficio);
          return "beneficios/detalle";
        })
        .orElse("redirect:/beneficios?error=Beneficio+no+encontrado");
  }
}