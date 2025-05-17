package com.biblioteca.controller;

import com.biblioteca.dto.AutorRequestDTO;
import com.biblioteca.service.AutorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/autores")
@RequiredArgsConstructor
public class AutorController {

    private final AutorService autorService;

    @GetMapping
    public String listarAutores(Model model) {
        model.addAttribute("autores", autorService.obtenerTodosLosAutores());
        model.addAttribute("activeTab", "autores");
        return "admin/autores/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoAutor(Model model) {
        model.addAttribute("autorRequestDTO", AutorRequestDTO.builder().build());
        model.addAttribute("activeTab", "autores");
        return "admin/autores/formulario";
    }

    @PostMapping("/guardar")
    public String guardarAutor(@Valid @ModelAttribute("autorRequestDTO") AutorRequestDTO autorRequestDTO,
            BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/autores/formulario";
        }
        autorService.crearAutor(autorRequestDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Autor creado exitosamente.");
        return "redirect:/admin/autores";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarAutor(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        return autorService.obtenerAutorPorId(id)
                .map(autor -> {
                    AutorRequestDTO requestDTO = AutorRequestDTO.builder()
                            .nombre(autor.getNombre())
                            .biografia(autor.getBiografia())
                            .fechaNacimiento(autor.getFechaNacimiento())
                            .nacionalidad(autor.getNacionalidad())
                            /*
                             * .website(autor.getWebsite())
                             * .fotoUrl(autor.getFotoUrl())
                             */
                            .build();
                    model.addAttribute("autorRequestDTO", requestDTO);
                    model.addAttribute("autorId", id);
                    model.addAttribute("activeTab", "autores");
                    System.out.println("Fecha de Nacimiento : " + requestDTO.getFechaNacimiento());
                    return "admin/autores/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Autor no encontrado.");
                    return "redirect:/admin/autores";
                });
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarAutor(@PathVariable Long id,
            @Valid @ModelAttribute("autorRequestDTO") AutorRequestDTO autorRequestDTO,
            BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/autores/formulario";
        }
        autorService.actualizarAutor(id, autorRequestDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Autor actualizado exitosamente.");
        return "redirect:/admin/autores";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarAutor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        autorService.eliminarAutor(id, false, "Eliminado desde admin");
        redirectAttributes.addFlashAttribute("successMessage", "Autor eliminado/desactivado exitosamente.");
        return "redirect:/admin/autores";
    }
}