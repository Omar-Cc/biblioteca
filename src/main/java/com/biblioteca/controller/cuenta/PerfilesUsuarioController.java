package com.biblioteca.controller.cuenta;

import com.biblioteca.dto.PerfilRequestDTO;
import com.biblioteca.dto.PerfilResponseDTO;
import com.biblioteca.dto.comercial.SuscripcionResponseDTO;
import com.biblioteca.exceptions.OperacionNoPermitidaException;
import com.biblioteca.service.PerfilService;
import com.biblioteca.service.PlanBeneficioService;
import com.biblioteca.service.SuscripcionService;
import com.biblioteca.service.UsuarioService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.HashMap; // Asegurar esta importación
import java.util.List;
import java.util.Map; // Asegurar esta importación
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mi-cuenta/perfiles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LECTOR')")
public class PerfilesUsuarioController {

  private final PerfilService perfilService;
  private final SuscripcionService suscripcionService;
  private final PlanBeneficioService planBeneficioService;
  private final UsuarioService usuarioService;

  private String getCurrentUsername(Principal principal) {
    if (principal == null) {
      throw new UsernameNotFoundException("Usuario no autenticado.");
    }
    return principal.getName();
  }

  /**
   * Obtiene información del plan del usuario y la configura en el modelo
   * para mostrar límites dinámicos en el formulario
   */
  private void obtenerYConfigurarInfoPlan(Model model, String username) {
    obtenerYConfigurarInfoPlan(model, username, null);
  }

  /**
   * Obtiene información del plan del usuario y la configura en el modelo
   * para mostrar límites dinámicos en el formulario
   */
  private void obtenerYConfigurarInfoPlan(Model model, String username, Long perfilIdExcluir) {
    try {
      // Obtener usuario
      var usuario = usuarioService.buscarPorUsername(username);
      if (usuario.isEmpty()) {
        // Sin usuario, usar límites por defecto
        model.addAttribute("limitePrestamosMax", 2);
        model.addAttribute("prestamosDisponibles", 2);
        model.addAttribute("perfilesPermitidos", 1);
        model.addAttribute("perfilesUsados", 0);
        model.addAttribute("perfilesDisponibles", 1);
        // No se añade planInfo, la plantilla lo manejará como null
        return;
      }

      // Obtener suscripción activa
      var suscripcionOpt = suscripcionService.obtenerSuscripcionActivaPorUsuario(usuario.get().getId());
      
      if (suscripcionOpt.isEmpty()) {
        // Sin suscripción activa, límites por defecto
        long perfilesUsados = perfilService.contarPerfilesPorUsuario(username);
        model.addAttribute("limitePrestamosMax", 2);
        model.addAttribute("prestamosDisponibles", 2);
        model.addAttribute("perfilesPermitidos", 1);
        model.addAttribute("perfilesUsados", perfilesUsados);
        model.addAttribute("perfilesDisponibles", 1 - (int) perfilesUsados);
        // No se añade planInfo, la plantilla lo manejará como null
        return;
      }

      SuscripcionResponseDTO suscripcion = suscripcionOpt.get();
      
      // Obtener límites del plan
      int limitePrestamos = 2; // Por defecto
      int limitePerfiles = 1; // Por defecto
      
      // Obtener beneficio de préstamos simultáneos (ID 2)
      var beneficioPrestamos = planBeneficioService.obtenerAsociacionPorPlanIdYBeneficioId(suscripcion.getPlanId(), 2L);
      if (beneficioPrestamos.isPresent()) {
        try {
          limitePrestamos = Integer.parseInt(beneficioPrestamos.get().getValor());
        } catch (NumberFormatException e) {
          limitePrestamos = 2; // Valor por defecto si no se puede parsear
        }
      }
      
      // Calcular préstamos ya asignados (excluyendo el perfil actual si se está editando)
      int prestamosAsignados = perfilService.obtenerPerfilesPorUsuario(username)
          .stream()
          .filter(perfil -> perfilIdExcluir == null || !perfil.getId().equals(perfilIdExcluir))
          .mapToInt(perfil -> perfil.getLimitePrestamosDesignado())
          .sum();
      
      int prestamosDisponibles = limitePrestamos - prestamosAsignados;
      
      // Obtener beneficio de número de perfiles (ID 11)
      var beneficioPerfiles = planBeneficioService.obtenerAsociacionPorPlanIdYBeneficioId(suscripcion.getPlanId(), 11L);
      if (beneficioPerfiles.isPresent()) {
        try {
          limitePerfiles = Integer.parseInt(beneficioPerfiles.get().getValor());
        } catch (NumberFormatException e) {
          limitePerfiles = 1; // Valor por defecto si no se puede parsear
        }
      }
      
      // Calcular perfiles usados y disponibles
      long perfilesUsados = perfilService.contarPerfilesPorUsuario(username);
      int perfilesDisponibles = limitePerfiles - (int) perfilesUsados;
      
      model.addAttribute("limitePrestamosMax", limitePrestamos);
      model.addAttribute("prestamosDisponibles", prestamosDisponibles);
      model.addAttribute("prestamosAsignados", prestamosAsignados);
      model.addAttribute("perfilesPermitidos", limitePerfiles);
      model.addAttribute("perfilesUsados", perfilesUsados);
      model.addAttribute("perfilesDisponibles", perfilesDisponibles);
      
      // Crear y popular el objeto planInfo con todos los datos necesarios
      Map<String, Object> planInfoMap = new HashMap<>();
      planInfoMap.put("nombrePlan", suscripcion.getPlanNombre());
      planInfoMap.put("perfilesUsados", (int) perfilesUsados);
      planInfoMap.put("maxPerfiles", limitePerfiles);
      planInfoMap.put("maxPrestamos", limitePrestamos);
      // Lógica para determinar si el plan es premium
      boolean esPremium = suscripcion.getPlanNombre() != null && 
                          (suscripcion.getPlanNombre().toLowerCase().contains("premium") || 
                           suscripcion.getPlanNombre().toLowerCase().contains("ilimitado"));
      planInfoMap.put("esPlanPremium", esPremium);
      model.addAttribute("planInfo", planInfoMap);
      // model.addAttribute("planActual", suscripcion.getPlanNombre()); // Esta línea se reemplaza por planInfo
      
    } catch (Exception e) {
      // En caso de error, usar límites por defecto
      long perfilesUsados = 0;
      try {
        perfilesUsados = perfilService.contarPerfilesPorUsuario(username);
      } catch (Exception ex) {
        // Si también falla contar perfiles, usar 0
      }
      model.addAttribute("limitePrestamosMax", 2);
      model.addAttribute("prestamosDisponibles", 2);
      model.addAttribute("prestamosAsignados", 0);
      model.addAttribute("perfilesPermitidos", 1);
      model.addAttribute("perfilesUsados", perfilesUsados);
      model.addAttribute("perfilesDisponibles", 1 - (int) perfilesUsados);
      // No se añade planInfo en caso de error, la plantilla lo manejará como null
    }
  }

  @GetMapping
  public String listarPerfiles(Model model, Principal principal, RedirectAttributes redirectAttributes,
      HttpSession session) {
    try {
      // Verificar que haya un perfil activo
      if (session.getAttribute("perfilActivoId") == null) {
        return "redirect:/mi-cuenta/perfiles/seleccionar";
      }

      String username = getCurrentUsername(principal);
      List<PerfilResponseDTO> listaPerfiles = perfilService.obtenerPerfilesPorUsuario(username);
      model.addAttribute("perfiles", listaPerfiles);

      // Asegurar que la información del plan y límites de perfiles esté disponible
      obtenerYConfigurarInfoPlan(model, username);

      if (listaPerfiles.isEmpty()) {
        model.addAttribute("infoMessage", "Aún no tienes perfiles. ¡Crea uno para empezar!");
      }

      configurarNavbar(model);

      return "public/perfil/lista-perfiles";
    } catch (UsernameNotFoundException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para ver tus perfiles.");
      return "redirect:/login";
    }
  }

  @GetMapping("/nuevo")
  public String mostrarFormularioNuevoPerfil(Model model, Principal principal, RedirectAttributes redirectAttributes) {
    try {
      String username = getCurrentUsername(principal);
      
      // Obtener información del plan del usuario PRIMERO para verificar límites
      obtenerYConfigurarInfoPlan(model, username);

      Integer perfilesDisponibles = (Integer) model.getAttribute("perfilesDisponibles");

      // Si no hay perfiles disponibles, redirigir y mostrar mensaje
      if (perfilesDisponibles != null && perfilesDisponibles <= 0) {
        redirectAttributes.addFlashAttribute("warningMessage", "Has alcanzado el límite de perfiles de tu plan. Mejora tu plan para crear más.");
        return "redirect:/mi-cuenta/perfiles";
      }
      
      // Continuar con la lógica si hay perfiles disponibles
      // perfilService.contarPerfilesPorUsuario(username); // Esta línea ya no es necesaria aquí, obtenerYConfigurarInfoPlan ya lo hace.

      // Verificar si ya existe un perfil principal
      boolean existePerfilPrincipal = perfilService.existePerfilPrincipal(username);
      model.addAttribute("existePerfilPrincipal", existePerfilPrincipal);
      model.addAttribute("esEdicionPerfilPrincipal", false);

      // Obtener información del plan del usuario para mostrar límites
      obtenerYConfigurarInfoPlan(model, username);

      // Crear el DTO con valores por defecto
      PerfilRequestDTO perfilRequestDTO = new PerfilRequestDTO();
      // Si ya existe un perfil principal, este nuevo perfil no puede ser principal
      perfilRequestDTO.setEsPerfilPrincipal(!existePerfilPrincipal);

      model.addAttribute("perfilRequestDTO", perfilRequestDTO);
      model.addAttribute("pageTitle", "Crear Nuevo Perfil");
      model.addAttribute("formAction", "/mi-cuenta/perfiles/crear");

      configurarNavbar(model);

      return "public/perfil/form-perfil";
    } catch (UsernameNotFoundException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para crear un perfil.");
      return "redirect:/login";
    }
  }

  @PostMapping("/crear")
  public String crearPerfil(@Valid @ModelAttribute("perfilRequestDTO") PerfilRequestDTO perfilDto,
      BindingResult result, Principal principal, RedirectAttributes redirectAttributes, Model model,
      HttpSession session, @RequestParam(value = "fotoPerfilFile", required = false) MultipartFile fotoPerfilFile,
      @RequestParam(value = "fotoPerfilFileSelected", required = false, defaultValue = "false") String fotoPerfilFileSelected) {

    System.out.println("=== INICIO CREAR PERFIL ===");
    System.out.println("Datos recibidos: " + perfilDto);
    System.out.println("FotoPerfil original: " + perfilDto.getFotoPerfil());
    System.out.println("Archivo recibido: " + (fotoPerfilFile != null ? fotoPerfilFile.getOriginalFilename() : "NULL"));
    System.out.println("Tamaño archivo: " + (fotoPerfilFile != null ? fotoPerfilFile.getSize() : "0") + " bytes");
    System.out.println("Content-Type: " + (fotoPerfilFile != null ? fotoPerfilFile.getContentType() : "NULL"));
    System.out.println("Archivo seleccionado flag: " + fotoPerfilFileSelected);

    String username = getCurrentUsername(principal);
    try {
      // Manejar la foto de perfil con más logging
      if ("true".equals(fotoPerfilFileSelected) && fotoPerfilFile != null && !fotoPerfilFile.isEmpty()) {
        System.out.println("🔄 Procesando archivo subido...");
        try {
          validateImageFile(fotoPerfilFile);
          System.out.println("✅ Archivo validado correctamente");

          String filename = saveUploadedFile(fotoPerfilFile);
          String relativePath = "/uploads/perfiles/" + filename;
          perfilDto.setFotoPerfil(relativePath);

          System.out.println("✅ Archivo guardado:");
          System.out.println("   - Filename: " + filename);
          System.out.println("   - Path relativo: " + relativePath);

        } catch (IOException e) {
          System.out.println("❌ Error de validación/guardado de archivo: " + e.getMessage());
          e.printStackTrace();
          result.rejectValue("fotoPerfil", "file.invalid", e.getMessage());
        }
      } else if (perfilDto.getFotoPerfil() != null && !perfilDto.getFotoPerfil().trim().isEmpty()) {
        System.out.println("🔄 Procesando URL de imagen: " + perfilDto.getFotoPerfil());
        if (perfilDto.getFotoPerfil().startsWith("data:image")) {
          System.out.println("❌ Detectado base64, limpiando...");
          perfilDto.setFotoPerfil(null);
        } else {
          System.out.println("✅ URL válida detectada: " + perfilDto.getFotoPerfil());
        }
      } else {
        System.out.println("ℹ️ No se proporcionó imagen (archivo ni URL)");
        perfilDto.setFotoPerfil(null);
      }

      // Verificar si ya existe un perfil principal
      boolean existePerfilPrincipal = perfilService.existePerfilPrincipal(username);
      if (existePerfilPrincipal) {
        perfilDto.setEsPerfilPrincipal(false);
      }

      if (result.hasErrors()) {
        System.out.println("❌ Errores de validación: " + result.getAllErrors());
        boolean existePerfilPrincipalForm = perfilService.existePerfilPrincipal(username);
        model.addAttribute("existePerfilPrincipal", existePerfilPrincipalForm);
        model.addAttribute("esEdicionPerfilPrincipal", false);
        model.addAttribute("pageTitle", "Crear Nuevo Perfil");
        model.addAttribute("formAction", "/mi-cuenta/perfiles/crear");
        configurarNavbar(model);
        return "public/perfil/form-perfil";
      }

      System.out.println("Datos finales antes de guardar:");
      System.out.println("- Nombre: " + perfilDto.getNombreVisible());
      System.out.println("- FotoPerfil: " + perfilDto.getFotoPerfil());
      System.out.println("- Idioma: " + perfilDto.getIdiomaPreferido());
      System.out.println("- Límite: " + perfilDto.getLimitePrestamosDesignado());

      PerfilResponseDTO perfilCreado = perfilService.crearPerfil(perfilDto, username);
      System.out.println("✅ Perfil creado exitosamente: " + perfilCreado.getId());

      redirectAttributes.addFlashAttribute("successMessage",
          "Perfil '" + perfilCreado.getNombreVisible() + "' creado exitosamente.");

      Long perfilActivoId = (Long) session.getAttribute("perfilActivoId");
      if (perfilActivoId != null) {
        return "redirect:/mi-cuenta/perfiles";
      } else {
        session.setAttribute("perfilActivoId", perfilCreado.getId());
        return "redirect:/";
      }

    } catch (OperacionNoPermitidaException e) {
      System.out.println("❌ Límite de plan excedido: " + e.getMessage());
      
      // Recargar información del plan para mostrar en el formulario
      obtenerYConfigurarInfoPlan(model, username);
      
      boolean existePerfilPrincipal = perfilService.existePerfilPrincipal(username);
      model.addAttribute("existePerfilPrincipal", existePerfilPrincipal);
      model.addAttribute("esEdicionPerfilPrincipal", false);
      model.addAttribute("errorMessage", e.getMessage());
      model.addAttribute("pageTitle", "Crear Nuevo Perfil");
      model.addAttribute("formAction", "/mi-cuenta/perfiles/crear");
      configurarNavbar(model);
      return "public/perfil/form-perfil";
      
    } catch (Exception e) {
      System.out.println("❌ Error inesperado: " + e.getMessage());
      e.printStackTrace();

      boolean existePerfilPrincipal = perfilService.existePerfilPrincipal(username);
      model.addAttribute("existePerfilPrincipal", existePerfilPrincipal);
      model.addAttribute("esEdicionPerfilPrincipal", false);
      model.addAttribute("errorMessage", "Error al crear el perfil: " + e.getMessage());
      model.addAttribute("pageTitle", "Crear Nuevo Perfil");
      model.addAttribute("formAction", "/mi-cuenta/perfiles/crear");
      configurarNavbar(model);
      return "public/perfil/form-perfil";
    }
  }

  // Método auxiliar para guardar archivos
  // Actualizar el método en PerfilesUsuarioController.java
  private String saveUploadedFile(MultipartFile file) throws IOException {
    // Usar directorio relativo al proyecto, no dentro de src/
    String baseUploadDir = "uploads/perfiles/";
    Path baseUploadPath = Paths.get(baseUploadDir);

    // Crear directorio base si no existe
    if (!Files.exists(baseUploadPath)) {
      Files.createDirectories(baseUploadPath);
      System.out.println("✅ Directorio creado: " + baseUploadPath.toAbsolutePath());
    }

    // Validar archivo
    if (file.isEmpty()) {
      throw new IOException("El archivo está vacío");
    }

    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || originalFilename.trim().isEmpty()) {
      throw new IOException("Nombre de archivo inválido");
    }

    // Obtener extensión
    String extension = "";
    int lastDotIndex = originalFilename.lastIndexOf(".");
    if (lastDotIndex > 0 && lastDotIndex < originalFilename.length() - 1) {
      extension = originalFilename.substring(lastDotIndex).toLowerCase();
    } else {
      // Si no tiene extensión, determinar por content type
      String contentType = file.getContentType();
      if (contentType != null) {
        switch (contentType) {
          case "image/jpeg":
            extension = ".jpg";
            break;
          case "image/png":
            extension = ".png";
            break;
          case "image/gif":
            extension = ".gif";
            break;
          case "image/webp":
            extension = ".webp";
            break;
          default:
            extension = ".jpg";
        }
      } else {
        extension = ".jpg";
      }
    }

    // Generar nombre único
    String uniqueFilename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    Path filePath = baseUploadPath.resolve(uniqueFilename);

    // Verificar que no exista (por seguridad)
    int counter = 1;
    while (Files.exists(filePath)) {
      String nameWithoutExt = uniqueFilename.substring(0, uniqueFilename.lastIndexOf('.'));
      uniqueFilename = nameWithoutExt + "_" + counter + extension;
      filePath = baseUploadPath.resolve(uniqueFilename);
      counter++;
    }

    try {
      // Guardar archivo
      Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
      System.out.println("✅ Archivo guardado exitosamente:");
      System.out.println("   - Nombre original: " + originalFilename);
      System.out.println("   - Nombre guardado: " + uniqueFilename);
      System.out.println("   - Ruta completa: " + filePath.toAbsolutePath());
      System.out.println("   - Tamaño: " + file.getSize() + " bytes");

      // Verificar que se guardó
      if (Files.exists(filePath)) {
        System.out.println("✅ Verificación: El archivo existe en el sistema de archivos");
        return uniqueFilename;
      } else {
        throw new IOException("El archivo no se pudo verificar después de guardarse");
      }

    } catch (IOException e) {
      System.err.println("❌ Error al guardar archivo: " + e.getMessage());
      e.printStackTrace();
      throw new IOException("Error al guardar el archivo: " + e.getMessage(), e);
    }
  }

  @GetMapping("/{id}/editar")
  public String mostrarFormularioEditarPerfil(@PathVariable Long id, Model model, Principal principal,
      RedirectAttributes redirectAttributes) {
    try {
      String username = getCurrentUsername(principal);
      PerfilResponseDTO perfil = perfilService.obtenerPerfilPorIdYUsuario(id, username)
          .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado o no pertenece al usuario."));

      // Verificar si ya existe un perfil principal
      boolean existePerfilPrincipal = perfilService.existePerfilPrincipal(username);
      model.addAttribute("existePerfilPrincipal", existePerfilPrincipal);
      // Si el perfil que editamos es el principal, permitir editar ese checkbox
      model.addAttribute("esEdicionPerfilPrincipal", perfil.getEsPerfilPrincipal());

      // Obtener información del plan del usuario para mostrar límites (excluyendo este perfil)
      obtenerYConfigurarInfoPlan(model, username, id);

      PerfilRequestDTO requestDTO = new PerfilRequestDTO();
      requestDTO.setNombreVisible(perfil.getNombreVisible());
      requestDTO.setFotoPerfil(perfil.getFotoPerfil());
      requestDTO.setIdiomaPreferido(perfil.getIdiomaPreferido());
      requestDTO.setLimitePrestamosDesignado(perfil.getLimitePrestamosDesignado());
      requestDTO.setEsInfantil(perfil.getEsInfantil());
      requestDTO.setEsPerfilPrincipal(perfil.getEsPerfilPrincipal());

      model.addAttribute("perfilRequestDTO", requestDTO);
      model.addAttribute("perfilId", id);
      model.addAttribute("pageTitle", "Editar Perfil: " + perfil.getNombreVisible());
      model.addAttribute("formAction", "/mi-cuenta/perfiles/" + id + "/actualizar");

      configurarNavbar(model);

      return "public/perfil/form-perfil";
    } catch (UsernameNotFoundException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error de autenticación.");
      return "redirect:/login";
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return "redirect:/mi-cuenta/perfiles";
    }
  }

  @PostMapping("/{id}/actualizar")
  public String actualizarPerfil(@PathVariable Long id,
      @Valid @ModelAttribute("perfilRequestDTO") PerfilRequestDTO perfilDto,
      BindingResult result, Principal principal, RedirectAttributes redirectAttributes, Model model,
      HttpSession session,
      @RequestParam(value = "fotoPerfilFile", required = false) MultipartFile fotoPerfilFile,
      @RequestParam(value = "fotoPerfilFileSelected", required = false, defaultValue = "false") String fotoPerfilFileSelected) {

    try {
      String username = getCurrentUsername(principal);

      // Manejar la foto de perfil ANTES de validar
      if ("true".equals(fotoPerfilFileSelected) && fotoPerfilFile != null && !fotoPerfilFile.isEmpty()) {
        // VALIDAR EL ARCHIVO ANTES DE GUARDARLO
        try {
          validateImageFile(fotoPerfilFile);
          String filename = saveUploadedFile(fotoPerfilFile);
          perfilDto.setFotoPerfil("/uploads/perfiles/" + filename);
          System.out.println("✅ Archivo validado y actualizado: " + filename);
        } catch (IOException e) {
          System.out.println("❌ Error de validación de archivo: " + e.getMessage());
          // Agregar error específico para el archivo
          result.rejectValue("fotoPerfil", "file.invalid", e.getMessage());
        }
      } else {
        // Se está usando URL o no hay imagen
        if (perfilDto.getFotoPerfil() != null) {
          if (perfilDto.getFotoPerfil().startsWith("data:image")) {
            System.out.println("❌ Detectado base64, limpiando...");
            perfilDto.setFotoPerfil(null);
          } else if (perfilDto.getFotoPerfil().trim().isEmpty()) {
            perfilDto.setFotoPerfil(null);
          } else {
            System.out.println("✅ URL válida detectada: " + perfilDto.getFotoPerfil());
          }
        }
      }

      if (result.hasErrors()) {
        // Obtener el perfil actual para saber si es principal
        PerfilResponseDTO perfil = perfilService.obtenerPerfilPorIdYUsuario(id, username)
            .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado"));

        boolean existePerfilPrincipal = perfilService.existePerfilPrincipal(username);
        model.addAttribute("existePerfilPrincipal", existePerfilPrincipal);
        model.addAttribute("esEdicionPerfilPrincipal", perfil.getEsPerfilPrincipal());
        model.addAttribute("perfilId", id);
        model.addAttribute("pageTitle", "Editar Perfil");
        model.addAttribute("formAction", "/mi-cuenta/perfiles/" + id + "/actualizar");
        configurarNavbar(model);

        System.out.println("❌ Errores en el formulario de actualización: " + result.getAllErrors());
        return "public/perfil/form-perfil";
      }

      System.out.println("=== DATOS RECIBIDOS PARA ACTUALIZAR ===");
      System.out.println("ID: " + id);
      System.out.println("Nombre visible: " + perfilDto.getNombreVisible());
      System.out.println("Idioma: " + perfilDto.getIdiomaPreferido());
      System.out.println("Límite préstamos: " + perfilDto.getLimitePrestamosDesignado());
      System.out.println("Es infantil: " + perfilDto.getEsInfantil());
      System.out.println("Es principal: " + perfilDto.getEsPerfilPrincipal());
      System.out.println("Foto perfil: " + perfilDto.getFotoPerfil());
      System.out.println("Archivo seleccionado flag: " + fotoPerfilFileSelected);
      System.out.println("=======================================");

      Optional<PerfilResponseDTO> perfilActualizado;
      try {
        perfilActualizado = perfilService.actualizarPerfil(id, perfilDto, username);
      } catch (OperacionNoPermitidaException e) {
        System.out.println("❌ Límite de plan excedido en actualización: " + e.getMessage());
        
        // Recargar información del plan para mostrar en el formulario (excluyendo este perfil)
        obtenerYConfigurarInfoPlan(model, username, id);
        
        // Obtener el perfil actual para mantener el contexto del formulario
        PerfilResponseDTO perfil = perfilService.obtenerPerfilPorIdYUsuario(id, username)
            .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado"));
        
        boolean existePerfilPrincipal = perfilService.existePerfilPrincipal(username);
        model.addAttribute("existePerfilPrincipal", existePerfilPrincipal);
        model.addAttribute("esEdicionPerfilPrincipal", perfil.getEsPerfilPrincipal());
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("perfilId", id);
        model.addAttribute("pageTitle", "Editar Perfil: " + perfil.getNombreVisible());
        model.addAttribute("formAction", "/mi-cuenta/perfiles/" + id + "/actualizar");
        configurarNavbar(model);
        return "public/perfil/form-perfil";
      }

      if (perfilActualizado.isPresent()) {
        // Si el perfil actualizado es el perfil activo, actualizar la sesión
        Long perfilActivoId = (Long) session.getAttribute("perfilActivoId");
        if (perfilActivoId != null && perfilActivoId.equals(id)) {
          PerfilResponseDTO perfilActualizadoDTO = perfilActualizado.get();
          session.setAttribute("perfilActivo", perfilActualizadoDTO);
          session.setAttribute("perfilActivoNombre", perfilActualizadoDTO.getNombreVisible());
          session.setAttribute("esPerfilPrincipal", perfilActualizadoDTO.getEsPerfilPrincipal());
        }

        redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado exitosamente.");
        System.out.println("✅ Perfil actualizado correctamente");
      } else {
        redirectAttributes.addFlashAttribute("errorMessage", "No se pudo actualizar el perfil.");
        System.out.println("❌ Error: No se pudo actualizar el perfil");
      }

      return "redirect:/mi-cuenta/perfiles";
    } catch (UsernameNotFoundException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error de autenticación.");
      return "redirect:/login";
    } catch (IllegalArgumentException e) {
      model.addAttribute("errorMessage", e.getMessage());
      model.addAttribute("perfilId", id);
      model.addAttribute("pageTitle", "Editar Perfil");
      model.addAttribute("formAction", "/mi-cuenta/perfiles/" + id + "/actualizar");
      configurarNavbar(model);
      System.out.println("❌ Error en actualización: " + e.getMessage());
      return "public/perfil/form-perfil";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el perfil: " + e.getMessage());
      System.out.println("❌ Error inesperado en actualización: " + e.getMessage());
      e.printStackTrace();
      return "redirect:/mi-cuenta/perfiles";
    }
  }

  @PostMapping("/{id}/eliminar")
  public String eliminarPerfil(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
    try {
      String username = getCurrentUsername(principal);
      if (perfilService.eliminarPerfil(id, username)) {
        redirectAttributes.addFlashAttribute("successMessage", "Perfil eliminado exitosamente.");
      } else {
        redirectAttributes.addFlashAttribute("errorMessage", "No se pudo eliminar el perfil o no fue encontrado.");
      }
    } catch (UsernameNotFoundException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error de autenticación.");
      return "redirect:/login";
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    }
    return "redirect:/mi-cuenta/perfiles";
  }

  // Método para mostrar la página de selección de perfiles estilo Netflix
  @GetMapping("/seleccionar")
  public String seleccionarPerfil(Model model, Principal principal, RedirectAttributes redirectAttributes,
      HttpSession session) {
    try {
      String username = getCurrentUsername(principal);
      List<PerfilResponseDTO> listaPerfiles = perfilService.obtenerPerfilesPorUsuario(username);
      model.addAttribute("perfiles", listaPerfiles);

      boolean noHayPerfiles = listaPerfiles.isEmpty();
      model.addAttribute("sinPerfiles", noHayPerfiles);

      // Llamar a obtenerYConfigurarInfoPlan para poblar los datos del plan y perfiles
      obtenerYConfigurarInfoPlan(model, username);

      // Ya no se redirige si hay un perfil activo, permitiendo siempre el acceso a esta página.
      // Long perfilActivoId = (Long) session.getAttribute("perfilActivoId");
      // if (perfilActivoId != null && !noHayPerfiles) {
      //   // Lógica anterior que podría haber redirigido (eliminada o comentada)
      // }

      // Si no hay perfiles, mostrar mensaje
      if (noHayPerfiles) {
        model.addAttribute("infoMessage", "¡Bienvenido! Parece que aún no tienes perfiles. Crea uno para empezar a disfrutar de la biblioteca.");
      }

      // Verificar si hay una URL pendiente para mostrar en la vista
      String urlPendiente = (String) session.getAttribute("URL_AFTER_PROFILE_SELECTION");
      if (urlPendiente != null) {
        model.addAttribute("urlPendiente", urlPendiente);
        System.out.println("🔍 DEBUG - Mostrando selección de perfil con URL pendiente: " + urlPendiente);
      }

      configurarNavbar(model);
      return "public/perfil/seleccionar-perfil";
      
    } catch (UsernameNotFoundException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para ver tus perfiles.");
      return "redirect:/login";
    }
  }

  // Método para activar un perfil
  @PostMapping("/{id}/activar")
  public String activarPerfil(@PathVariable Long id, Principal principal,
      HttpSession session, RedirectAttributes redirectAttributes) {
    try {
      String username = getCurrentUsername(principal);
      PerfilResponseDTO perfil = perfilService.obtenerPerfilPorIdYUsuario(id, username)
          .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado o no pertenece al usuario."));

      System.out.println("Activando perfil: " + perfil.getNombreVisible() + " (ID: " + id + ")");
      
      // Actualizar la fecha de última actividad
      perfilService.actualizarFechaUltimoActividad(id);

      // Guardar el perfil activo en la sesión
      session.setAttribute("perfilActivo", perfil);
      session.setAttribute("perfilActivoId", perfil.getId());
      session.setAttribute("perfilActivoNombre", perfil.getNombreVisible());
      session.setAttribute("esPerfilPrincipal", perfil.getEsPerfilPrincipal());

      // Marcar que se acaba de activar un perfil
      session.setAttribute("perfilRecienActivado", true);

      // Verificar si hay una URL guardada para redirección
      String urlAfterProfileSelection = (String) session.getAttribute("URL_AFTER_PROFILE_SELECTION");
      if (urlAfterProfileSelection != null && !urlAfterProfileSelection.isEmpty()) {
        // Limpiar la URL de la sesión
        session.removeAttribute("URL_AFTER_PROFILE_SELECTION");
        session.removeAttribute("REQUESTED_URL");
        session.removeAttribute("SPRING_SECURITY_SAVED_REQUEST");
        
        System.out.println("🔍 DEBUG - Redirigiendo a URL guardada después de selección de perfil: " + urlAfterProfileSelection);
        
        // Extraer solo el path de la URL completa
        try {
          java.net.URL url = new java.net.URL(urlAfterProfileSelection);
          String path = url.getPath();
          String query = url.getQuery();
          
          String redirectUrl = path;
          if (query != null && !query.isEmpty()) {
            redirectUrl += "?" + query;
          }
          
          // Agregar el parámetro perfilActivado para evitar que el interceptor interfiera
          String separator = query != null ? "&" : "?";
          redirectUrl += separator + "perfilActivado=true";
          
          System.out.println("🔍 DEBUG - URL final de redirección: " + redirectUrl);
          return "redirect:" + redirectUrl;
          
        } catch (Exception e) {
          System.out.println("⚠️ WARNING - Error al parsear URL guardada: " + e.getMessage());
          // Fallback a home
          return "redirect:/?perfilActivado=true";
        }
      }

      // Si no hay URL guardada, ir a home
      System.out.println("🔍 DEBUG - No hay URL guardada, redirigiendo a home");
      return "redirect:/?perfilActivado=true";
      
    } catch (UsernameNotFoundException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error de autenticación.");
      return "redirect:/login";
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }
  }

  // Método auxiliar para configurar navbar en todas las vistas
  private void configurarNavbar(Model model) {
    model.addAttribute("activeTab", "perfil");
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
      model.addAttribute("carritoItems", 0);
    }
  }

  // Método de validación de archivos de imagen
  private void validateImageFile(MultipartFile file) throws IOException {
    // Validar que el archivo no esté vacío
    if (file.isEmpty()) {
      throw new IOException("El archivo está vacío");
    }

    // Validar tamaño (5MB máximo)
    long maxSize = 5_000_000; // 5MB
    if (file.getSize() > maxSize) {
      throw new IOException("El archivo es demasiado grande. Tamaño máximo permitido: 5MB");
    }

    // Validar nombre de archivo
    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || originalFilename.trim().isEmpty()) {
      throw new IOException("Nombre de archivo inválido");
    }

    // Validar extensiones permitidas
    String extension = originalFilename.toLowerCase();
    if (!extension.endsWith(".jpg") && !extension.endsWith(".jpeg") &&
        !extension.endsWith(".png") && !extension.endsWith(".gif") &&
        !extension.endsWith(".webp")) {
      throw new IOException("Tipo de archivo no permitido. Solo se permiten: JPG, JPEG, PNG, GIF, WEBP");
    }

    // Validar content type
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new IOException("El archivo debe ser una imagen válida");
    }

    // Validaciones adicionales de seguridad
    if (contentType.equals("image/svg+xml")) {
      throw new IOException("Los archivos SVG no están permitidos por seguridad");
    }

    // Validar que el content type coincida con la extensión
    boolean contentTypeValid = false;
    switch (contentType) {
      case "image/jpeg":
        contentTypeValid = extension.endsWith(".jpg") || extension.endsWith(".jpeg");
        break;
      case "image/png":
        contentTypeValid = extension.endsWith(".png");
        break;
      case "image/gif":
        contentTypeValid = extension.endsWith(".gif");
        break;
      case "image/webp":
        contentTypeValid = extension.endsWith(".webp");
        break;
    }

    if (!contentTypeValid) {
      throw new IOException("El tipo de archivo no coincide con la extensión");
    }

    System.out.println("✅ Archivo validado correctamente: " + originalFilename + " (" + contentType + ")");
  }
}