package com.biblioteca.controller.cuenta;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca.dto.comercial.FacturaResponseDTO;
import com.biblioteca.models.acceso.Perfil;
import com.biblioteca.models.comercial.Orden;
import com.biblioteca.service.EmailService;
import com.biblioteca.service.FacturaService;
import com.biblioteca.service.OrdenService;
import com.biblioteca.service.PdfService;
import com.biblioteca.service.PerfilService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/mi-cuenta/facturas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LECTOR')")
public class MiFacturaController {

  private final FacturaService facturaService;
  private final PerfilService perfilService;
  private final OrdenService ordenService;
  private final PdfService pdfService;
  private final EmailService emailService;

  @GetMapping
  public String listarFacturasUsuario(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    // Obtener el perfil activo desde la sesión
    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para ver las facturas");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    // Mostrar facturas del perfil activo, no del usuario completo
    model.addAttribute("facturas", facturaService.obtenerFacturasPorPerfil(perfil.getId()));
    return "mi-cuenta/facturas/lista-mis-facturas";
  }

  @GetMapping("/{id}")
  public String verFactura(@PathVariable Long id, Model model,
      HttpSession session, RedirectAttributes redirectAttributes) {
    Perfil perfilActual = obtenerPerfilActual(session);

    if (perfilActual == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para ver las facturas");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    return facturaService.obtenerFacturaPorId(id)
        .map(factura -> {
          // Verificar que la factura pertenece al perfil actual
          if (!facturaPertenecePerfil(factura, perfilActual.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para ver esta factura");
            return "redirect:/mi-cuenta/facturas";
          }

          model.addAttribute("factura", factura);
          return "facturas/detalle";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Factura no encontrada");
          return "redirect:/mi-cuenta/facturas";
        });
  }

  @GetMapping("/{id}/imprimir")
  public String prepararImpresionFactura(@PathVariable Long id, Model model,
      HttpSession session, RedirectAttributes redirectAttributes) {
    Perfil perfilActual = obtenerPerfilActual(session);

    if (perfilActual == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para imprimir facturas");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    return facturaService.obtenerFacturaPorId(id)
        .map(factura -> {
          // Verificar que la factura pertenece al perfil actual
          if (!facturaPertenecePerfil(factura, perfilActual.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para imprimir esta factura");
            return "redirect:/mi-cuenta/facturas";
          }

          model.addAttribute("factura", factura);
          return "facturas/imprimir";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Factura no encontrada");
          return "redirect:/mi-cuenta/facturas";
        });
  }

  @GetMapping("/{id}/descargar")
  public ResponseEntity<byte[]> descargarFactura(@PathVariable Long id,
      HttpSession session, RedirectAttributes redirectAttributes) {

    Perfil perfilActual = obtenerPerfilActual(session);

    if (perfilActual == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    return facturaService.obtenerFacturaPorId(id)
        .map(factura -> {
          // Verificar que la factura pertenece al perfil actual
          if (!facturaPertenecePerfil(factura, perfilActual.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).<byte[]>build();
          }

          try {
            byte[] pdfBytes = pdfService.generarFacturaOrden(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                    .filename("factura-" + factura.getNumeroFactura() + ".pdf")
                    .build());

            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);

          } catch (Exception e) {
            log.error("Error generando PDF para factura {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<byte[]>build();
          }
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/enviar-email")
  public String enviarFacturaPorEmail(@PathVariable Long id,
      HttpSession session, RedirectAttributes redirectAttributes) {

    Perfil perfilActual = obtenerPerfilActual(session);

    if (perfilActual == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    return facturaService.obtenerFacturaPorId(id)
        .map(factura -> {
          if (!facturaPertenecePerfil(factura, perfilActual.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para esta factura");
            return "redirect:/mi-cuenta/facturas";
          }

          try {
            // Generar PDF
            byte[] pdfBytes = pdfService.generarFacturaOrden(id);

            // Preparar adjuntos
            Map<String, byte[]> adjuntos = new HashMap<>();
            adjuntos.put("factura-" + factura.getNumeroFactura() + ".pdf", pdfBytes);

            // Enviar email
            String email = perfilActual.getUsuario().getEmail();
            String asunto = "Factura " + factura.getNumeroFactura() + " - Biblioteca Digital";

            Map<String, Object> variables = new HashMap<>();
            variables.put("factura", factura);
            variables.put("perfil", perfilActual);

            // Llamada para enviar email con adjunto
            boolean enviado = emailService.enviarEmailConPlantillaYAdjuntos(
                email,
                asunto,
                "email/factura/documento-adjunto", // Plantilla de email
                variables,
                adjuntos // PDF adjunto
            );

            if (enviado) {
              redirectAttributes.addFlashAttribute("success", "Factura enviada por email correctamente");
            } else {
              redirectAttributes.addFlashAttribute("error", "Error enviando la factura por email");
            }

          } catch (Exception e) {
            log.error("Error enviando factura por email: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error procesando la solicitud");
          }

          return "redirect:/mi-cuenta/facturas";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Factura no encontrada");
          return "redirect:/mi-cuenta/facturas";
        });
  }

  private Perfil obtenerPerfilActual(HttpSession session) {
    // Obtener el ID del perfil activo desde la sesión
    Long perfilActivoId = (Long) session.getAttribute("perfilActivoId");

    if (perfilActivoId == null) {
      return null;
    }

    // Obtener el perfil desde la base de datos usando el ID
    return perfilService.obtenerEntidadPerfilPorId(perfilActivoId)
        .orElse(null);
  }

  private boolean facturaPertenecePerfil(FacturaResponseDTO factura, Long perfilId) {
    try {
      // Verificar si la factura pertenece al perfil actual
      Orden orden = ordenService.obtenerEntidadOrdenPorId(factura.getOrdenId())
          .orElseThrow(() -> new NullPointerException("Orden no encontrada"));

      return orden.getPerfil().getId().equals(perfilId);
    } catch (NullPointerException e) {
      return false;
    }
  }
}