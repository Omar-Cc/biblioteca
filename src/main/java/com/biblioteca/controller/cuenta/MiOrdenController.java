package com.biblioteca.controller.cuenta;

import java.security.Principal;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca.dto.PerfilResponseDTO;
import com.biblioteca.dto.comercial.MetodoPagoResponseDTO;
import com.biblioteca.dto.comercial.OrdenRequestDTO;
import com.biblioteca.dto.comercial.OrdenResponseDTO;
import com.biblioteca.dto.comercial.PagoRequestDTO;
import com.biblioteca.models.Perfil;
import com.biblioteca.service.CarritoService;
import com.biblioteca.service.FacturaService;
import com.biblioteca.service.MetodoPagoService;
import com.biblioteca.service.OrdenService;
import com.biblioteca.service.PagoService;
import com.biblioteca.service.PerfilService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mi-cuenta/orden")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'LECTOR')")
public class MiOrdenController {

  private final OrdenService ordenService;
  private final CarritoService carritoService;
  private final PerfilService perfilService;
  private final MetodoPagoService metodoPagoService;
  private final PagoService pagoService;
  private final FacturaService facturaService;

  @GetMapping
  public String listarOrdenes(Model model, HttpSession session) {
    // Obtener el perfil del usuario actual
    Perfil perfil = obtenerPerfilActual(session);
    if (perfil == null) {
      return "redirect:/mi-cuenta/perfiles/seleccionar?error=Se+requiere+un+perfil+para+ver+órdenes";
    }

    List<OrdenResponseDTO> ordenes = ordenService.obtenerOrdenesPorPerfil(perfil.getId());
    model.addAttribute("ordenes", ordenes);

    return "ordenes/lista";
  }

  @GetMapping("/{id}")
  public String verDetalleOrden(@PathVariable Long id, Model model, HttpSession session,
      RedirectAttributes redirectAttributes) {
    Perfil perfil = obtenerPerfilActual(session);

    return ordenService.obtenerOrdenPorId(id)
        .map(orden -> {
          // Verificar que la orden pertenece al usuario actual
          if (!orden.getPerfilId().equals(perfil.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para ver esta orden");
            return "redirect:/mi-cuenta/orden";
          }

          model.addAttribute("orden", orden);
          model.addAttribute("items", ordenService.obtenerItemsDeOrden(id));

          // Verificar si la orden tiene factura
          facturaService.obtenerFacturaPorId(orden.getFactura().getId())
              .ifPresent(factura -> model.addAttribute("factura", factura));

          return "ordenes/detalle";
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Orden no encontrada");
          return "redirect:/mi-cuenta/orden";
        });
  }

  @GetMapping("/checkout")
  public String mostrarCheckout(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    // Obtener el perfil activo desde la sesión
    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      return "redirect:/mi-cuenta/perfiles/crear?error=Se+requiere+un+perfil+para+realizar+compras";
    }

    // Verificar si el carrito asociado al perfil tiene items
    if (!carritoService.obtenerCarritoPorPerfil(perfil.getId())
        .map(carrito -> !carrito.getItems().isEmpty())
        .orElse(false)) {
      redirectAttributes.addFlashAttribute("error", "El carrito está vacío");
      return "redirect:/mi-cuenta/carrito";
    }

    // Obtener métodos de pago disponibles
    List<MetodoPagoResponseDTO> metodosPago = metodoPagoService.obtenerMetodosPagoActivos();
    model.addAttribute("metodosPago", metodosPago);

    // Calcular total del carrito para el perfil específico
    double totalCarrito = carritoService.calcularTotalCarritoPorPerfil(perfil.getId());
    model.addAttribute("totalCarrito", totalCarrito);

    // Pasar datos del carrito específico del perfil
    carritoService.obtenerCarritoPorPerfil(perfil.getId())
        .ifPresent(carrito -> model.addAttribute("carrito", carrito));

    // Preparar DTO para la orden
    model.addAttribute("ordenForm", new OrdenRequestDTO());

    return "ordenes/checkout";
  }

  @PostMapping("/crear")
  public String crearOrden(
      @ModelAttribute("ordenForm") OrdenRequestDTO ordenDTO,
      @RequestParam Long metodoPagoId,
      Model model,
      RedirectAttributes redirectAttributes, HttpSession session) {

    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Se requiere un perfil para realizar compras");
      return "redirect:/mi-cuenta/perfiles/crear";
    }

    try {
      // Crear la orden desde el carrito
      OrdenResponseDTO orden = ordenService.crearOrdenDesdeCarrito(perfil.getId());

      // Registrar el pago
      PagoRequestDTO pagoDTO = new PagoRequestDTO();
      pagoDTO.setOrdenId(orden.getId());
      pagoDTO.setMetodoPagoId(metodoPagoId);
      pagoDTO.setMonto(orden.getTotalOrden());
      pagoDTO.setEstado("Pendiente");

      pagoService.registrarPago(pagoDTO);

      // Procesar la orden
      ordenService.procesarOrden(orden.getId());

      redirectAttributes.addFlashAttribute("mensaje", "Orden creada exitosamente");
      return "redirect:/mi-cuenta/orden/" + orden.getId();

    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error al crear la orden: " + e.getMessage());
      return "redirect:/mi-cuenta/carrito";
    }
  }

  @PostMapping("/{id}/cancelar")
  public String cancelarOrden(
      @PathVariable Long id,
      @RequestParam(required = false) String motivo,
      RedirectAttributes redirectAttributes, HttpSession session) {

    Perfil perfil = obtenerPerfilActual(session);
    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Se requiere un perfil para cancelar órdenes");
      return "redirect:/mi-cuenta/perfiles/crear";
    }

    return ordenService.obtenerOrdenPorId(id)
        .map(orden -> {
          // Verificar que la orden pertenece al usuario actual
          if (!orden.getPerfilId().equals(perfil.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para cancelar esta orden");
            return "redirect:/mi-cuenta/orden";
          }

          try {
            ordenService.cancelarOrden(id, motivo);
            redirectAttributes.addFlashAttribute("mensaje", "Orden cancelada exitosamente");
          } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar la orden: " + e.getMessage());
          }

          return "redirect:/mi-cuenta/orden/" + id;
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Orden no encontrada");
          return "redirect:/mi-cuenta/orden";
        });
  }

  @GetMapping("/{id}/factura")
  public String generarFactura(
      @PathVariable Long id,
      RedirectAttributes redirectAttributes, Principal principal) {

    String username = getCurrentUsername(principal);
    PerfilResponseDTO perfil = perfilService.obtenerPerfilPorIdYUsuario(id, username)
        .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado o no pertenece al usuario."));

    return ordenService.obtenerOrdenPorId(id)
        .map(orden -> {
          // Verificar que la orden pertenece al usuario actual
          if (!orden.getPerfilId().equals(perfil.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para ver esta factura");
            return "redirect:/mi-cuenta/orden";
          }

          // Verificar si la orden ya tiene factura
          if (orden.getFactura().getId() != null) {
            return "redirect:/mi-cuenta/factura/" + orden.getFactura().getId();
          }

          // Generar factura
          boolean generada = ordenService.generarFactura(id);

          if (generada) {
            // Obtener la nueva factura
            return ordenService.obtenerOrdenPorId(id)
                .map(ordenActualizada -> {
                  redirectAttributes.addFlashAttribute("mensaje", "Factura generada exitosamente");
                  return "redirect:/mi-cuenta/factura/" + ordenActualizada.getFactura().getId();
                })
                .orElse("redirect:/mi-cuenta/orden/" + id);
          } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo generar la factura");
            return "redirect:/mi-cuenta/orden/" + id;
          }
        })
        .orElseGet(() -> {
          redirectAttributes.addFlashAttribute("error", "Orden no encontrada");
          return "redirect:/mi-cuenta/orden";
        });
  }

  @GetMapping("/historial")
  public String verHistorialCompras(Model model, HttpSession session) {
    Perfil perfil = obtenerPerfilActual(session);
    if (perfil == null) {
      return "redirect:/mi-cuenta/perfiles/crear?error=Se+requiere+un+perfil+para+ver+historial";
    }

    List<OrdenResponseDTO> ordenes = ordenService.obtenerOrdenesPorPerfil(perfil.getId());
    model.addAttribute("ordenes", ordenes);

    return "ordenes/historial";
  }

  private Perfil obtenerPerfilActual(HttpSession session) {
    // Obtener el ID del perfil activo desde la sesión
    Long perfilActivoId = (Long) session.getAttribute("perfilActivoId");

    if (perfilActivoId == null) {
      return null;
    }

    // Obtener el perfil desde la base de datos usando el ID
    return perfilService.obtenerPerfilPorId(perfilActivoId)
        .orElse(null);
  }

  private String getCurrentUsername(Principal principal) {
    if (principal == null) {
      throw new UsernameNotFoundException("Usuario no autenticado.");
    }
    return principal.getName();
  }
  
}