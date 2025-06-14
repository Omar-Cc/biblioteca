package com.biblioteca.controller;

import com.biblioteca.dto.ContenidoResponseDTO;
import com.biblioteca.enums.TipoContenido;
import com.biblioteca.service.ContenidoService;
import com.biblioteca.service.EditorialService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BusquedaController {

  private final ContenidoService contenidoService;
  private final EditorialService editorialService;

  @GetMapping("/buscar")
  public String buscarContenido(
      @RequestParam(value = "q", required = false) String query,
      @RequestParam(value = "tipo", required = false) TipoContenido tipo,
      @RequestParam(value = "editorial", required = false) Long editorialId,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "12") int size,
      Model model) {

    if (query == null || query.trim().isEmpty()) {
      return "redirect:/catalogo";
    }

    try {
      List<ContenidoResponseDTO> resultados = contenidoService.buscarContenido(
          query.trim(),
          Optional.ofNullable(tipo),
          Optional.ofNullable(editorialId),
          page,
          size);

      long totalResultados = contenidoService.contarResultadosBusqueda(
          query.trim(),
          Optional.ofNullable(tipo),
          Optional.ofNullable(editorialId));

      model.addAttribute("contenidos", resultados);
      model.addAttribute("query", query);
      model.addAttribute("totalResultados", totalResultados);
      model.addAttribute("paginaActual", page);
      model.addAttribute("tamanioPagina", size);
      model.addAttribute("totalPaginas", (int) Math.ceil((double) totalResultados / size));
      model.addAttribute("editoriales", editorialService.obtenerTodasLasEditoriales());
      model.addAttribute("tiposContenido", TipoContenido.values());
      model.addAttribute("activeTab", "busqueda");

      return "public/busqueda/resultados";
    } catch (Exception e) {
      log.error("Error en búsqueda: ", e);
      model.addAttribute("error", "Error al realizar la búsqueda");
      return "redirect:/catalogo?error=busqueda";
    }
  }

  // API para búsqueda en tiempo real
  @GetMapping("/api/buscar/tiempo-real")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> busquedaTiempoReal(
      @RequestParam("q") String query,
      @RequestParam(value = "limite", defaultValue = "8") int limite) {

    try {
      log.debug("Búsqueda en tiempo real para: {}", query);

      if (query == null || query.trim().length() < 3) {
        return ResponseEntity.ok(Map.of(
            "resultados", List.of(),
            "total", 0,
            "query", query != null ? query : ""));
      }

      // Buscar contenidos limitados para vista previa
      List<ContenidoResponseDTO> resultados = contenidoService.buscarContenido(
          query.trim(),
          Optional.empty(),
          Optional.empty(),
          0,
          limite);

      // Contar total para mostrar "ver todos los X resultados"
      long totalResultados = contenidoService.contarResultadosBusqueda(
          query.trim(),
          Optional.empty(),
          Optional.empty());

      log.debug("Encontrados {} resultados para: {}", totalResultados, query);

      return ResponseEntity.ok(Map.of(
          "resultados", resultados,
          "total", totalResultados,
          "query", query.trim(),
          "tieneMs", totalResultados > limite));

    } catch (Exception e) {
      log.error("Error en búsqueda tiempo real: ", e);
      return ResponseEntity.ok(Map.of(
          "resultados", List.of(),
          "total", 0,
          "query", query != null ? query.trim() : "",
          "error", "Error interno del servidor"));
    }
  }

  @GetMapping("/api/buscar/sugerencias")
  @ResponseBody
  public ResponseEntity<List<Map<String, Object>>> obtenerSugerencias(@RequestParam("q") String query) {
    try {
      if (query == null || query.trim().length() < 3) {
        return ResponseEntity.ok(List.of());
      }
      List<Map<String, Object>> sugerencias = contenidoService.obtenerSugerenciasBusqueda(query.trim());
      return ResponseEntity.ok(sugerencias);
    } catch (Exception e) {
      log.error("Error en sugerencias: ", e);
      return ResponseEntity.ok(List.of());
    }
  }

  @GetMapping("/buscar/avanzada")
  public String busquedaAvanzada(Model model) {
    model.addAttribute("activeTab", "busqueda");
    return "public/busqueda/avanzada";
  }

  @GetMapping("/test-busqueda")
  @ResponseBody
  public String testBusqueda() {
    try {
      List<ContenidoResponseDTO> resultados = contenidoService.buscarContenido(
          "Dune", Optional.empty(), Optional.empty(), 0, 10);
      return "Resultados encontrados: " + resultados.size();
    } catch (Exception e) {
      log.error("Error en test búsqueda: ", e);
      return "Error: " + e.getMessage();
    }
  }
}