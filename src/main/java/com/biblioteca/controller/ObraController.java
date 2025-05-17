package com.biblioteca.controller;

import com.biblioteca.dto.AutorResponseDTO;
import com.biblioteca.dto.GeneroResponseDTO;
import com.biblioteca.dto.ObraRequestDTO;
import com.biblioteca.dto.ObraResponseDTO;
import com.biblioteca.service.AutorService;
import com.biblioteca.service.EditorialService;
import com.biblioteca.service.GeneroService;
import com.biblioteca.service.ObraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/obras")
@RequiredArgsConstructor
public class ObraController {

  private final ObraService obraService;
  private final EditorialService editorialService;
  private final AutorService autorService;
  private final GeneroService generoService;

  @GetMapping
  public String listarObras(Model model) {
    model.addAttribute("obras", obraService.obtenerTodasLasObras());
    model.addAttribute("activeTab", "obras");
    return "admin/obras/lista";
  }

  @GetMapping("/nueva")
  public String mostrarFormularioNuevaObra(Model model) {
    model.addAttribute("obraRequestDTO", ObraRequestDTO.builder().build());
    model.addAttribute("activeTab", "obras");
    cargarDatosFormulario(model);
    return "admin/obras/formulario";
  }

  @PostMapping("/guardar")
  public String guardarObra(@Valid @ModelAttribute("obraRequestDTO") ObraRequestDTO obraRequestDTO,
      BindingResult result, Model model, RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
      cargarDatosFormulario(model);
      return "admin/obras/formulario";
    }
    try {
      obraService.crearObra(obraRequestDTO);
      redirectAttributes.addFlashAttribute("successMessage", "Obra creada exitosamente.");
      return "redirect:/admin/obras";
    } catch (IllegalArgumentException e) {
      model.addAttribute("errorMessage", e.getMessage());
      cargarDatosFormulario(model);
      return "admin/obras/formulario";
    }
  }

  @GetMapping("/editar/{id}")
  public String mostrarFormularioEditarObra(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    Optional<ObraResponseDTO> obraOpt = obraService.obtenerObraPorId(id);
    if (obraOpt.isEmpty()) {
      redirectAttributes.addFlashAttribute("errorMessage", "Obra no encontrada.");
      return "redirect:/admin/obras";
    }

    ObraResponseDTO obraResponse = obraOpt.get();

    // Extraer IDs de autores
    List<Long> autoresIds = obraResponse.getAutores() != null
        ? obraResponse.getAutores().stream().map(AutorResponseDTO::getId).collect(Collectors.toList())
        : new ArrayList<>();

    List<String> autorRoles = new ArrayList<>();
    for (int i = 0; i < autoresIds.size(); i++) {
      autorRoles.add("principal");
    }

    // Extraer IDs de géneros
    List<Long> generosIds = obraResponse.getGeneros() != null
        ? obraResponse.getGeneros().stream().map(GeneroResponseDTO::getId).collect(Collectors.toList())
        : new ArrayList<>();

    ObraRequestDTO requestDTO = ObraRequestDTO.builder()
        .titulo(obraResponse.getTitulo())
        .descripcion(obraResponse.getDescripcion())
        .anioPublicacion(obraResponse.getAnioPublicacion())
        .isbn(obraResponse.getIsbn())
        .editorialId(obraResponse.getEditorial() != null ? obraResponse.getEditorial().getId() : null)
        .idioma(obraResponse.getIdioma())
        .autorIds(autoresIds)
        .autorRoles(autorRoles)
        .generoIds(generosIds)
        .build();

    model.addAttribute("obraRequestDTO", requestDTO);
    model.addAttribute("obraId", id);
    model.addAttribute("activeTab", "obras");
    cargarDatosFormulario(model);
    return "admin/obras/formulario";
  }

  @PostMapping("/actualizar/{id}")
  public String actualizarObra(@PathVariable Long id,
      @Valid @ModelAttribute("obraRequestDTO") ObraRequestDTO obraRequestDTO,
      BindingResult result, Model model, RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
      model.addAttribute("obraId", id);
      cargarDatosFormulario(model);
      return "admin/obras/formulario";
    }
    try {
      obraService.actualizarObra(id, obraRequestDTO);
      redirectAttributes.addFlashAttribute("successMessage", "Obra actualizada exitosamente.");
      return "redirect:/admin/obras";
    } catch (IllegalArgumentException e) {
      model.addAttribute("errorMessage", e.getMessage());
      model.addAttribute("obraId", id);
      cargarDatosFormulario(model);
      return "admin/obras/formulario";
    }
  }

  @PostMapping("/eliminar/{id}")
  public String eliminarObra(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    boolean eliminado = obraService.eliminarObra(id, false, "Eliminado desde admin");
    if (eliminado) {
      redirectAttributes.addFlashAttribute("successMessage", "Obra eliminada exitosamente.");
    } else {
      redirectAttributes.addFlashAttribute("errorMessage", "No se pudo eliminar la obra o no fue encontrada.");
    }
    return "redirect:/admin/obras";
  }

  private void cargarDatosFormulario(Model model) {
    model.addAttribute("editoriales", editorialService.obtenerTodasLasEditoriales());
    model.addAttribute("autores", autorService.obtenerTodosLosAutores());
    model.addAttribute("generos", generoService.obtenerTodosLosGeneros());
  }
}