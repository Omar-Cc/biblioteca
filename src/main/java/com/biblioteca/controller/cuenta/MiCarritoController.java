package com.biblioteca.controller.cuenta;

import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.biblioteca.dto.comercial.CarritoResponseDTO;
import com.biblioteca.dto.comercial.ItemCarritoRequestDTO;
import com.biblioteca.models.acceso.Perfil;
import com.biblioteca.service.CarritoService;
import com.biblioteca.service.PerfilService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/mi-cuenta/carrito")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LECTOR')")
@Slf4j
public class MiCarritoController {

  private final CarritoService carritoService;
  private final PerfilService perfilService;

  @GetMapping
  public String verCarrito(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para ver el carrito");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    Optional<CarritoResponseDTO> carritoOpt = carritoService.obtenerCarritoPorPerfil(perfil.getId());

    log.debug("Carrito obtenido para perfil {}: {}", perfil.getId(),
        carritoOpt.isPresent() ? "Presente" : "No presente");

    if (carritoOpt.isPresent() && carritoOpt.get().getItems() != null && !carritoOpt.get().getItems().isEmpty()) {
      CarritoResponseDTO carrito = carritoOpt.get();
      log.debug("Carrito con {} items. Subtotal: {}, Total: {}",
          carrito.getItems().size(), carrito.getSubtotal(), carrito.getTotal());

      // Log de cada item
      carrito.getItems().forEach(item -> log.debug("Item ID: {}, Precio: {}, Cantidad: {}, Subtotal: {}",
          item.getId(), item.getPrecio(), item.getCantidad(), item.getSubtotal()));

      model.addAttribute("carrito", carrito);
      model.addAttribute("carritoVacio", false);
    } else {
      log.debug("Carrito vacío o sin items");
      model.addAttribute("carritoVacio", true);
      carritoOpt.ifPresent(carrito -> model.addAttribute("carrito", carrito));
    }

    return "public/carrito/ver";
  }

  @PostMapping("/agregar")
  public String agregarItemAlCarrito(
      @Valid @ModelAttribute("itemForm") ItemCarritoRequestDTO itemDTO,
      BindingResult result,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    if (result.hasErrors()) {
      redirectAttributes.addFlashAttribute("error", "Datos de item inválidos");
      return "redirect:/mi-cuenta/carrito";
    }

    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para agregar productos al carrito");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      carritoService.agregarItemAlCarritoPorPerfil(perfil.getId(), itemDTO);
      actualizarContadorCarritoEnSesion(session, perfil.getId());
      redirectAttributes.addFlashAttribute("mensaje", "Producto agregado al carrito");
      return "redirect:/mi-cuenta/carrito";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error al agregar al carrito: " + e.getMessage());
      return "redirect:/mi-cuenta/carrito";
    }
  }

  @PostMapping("/actualizar/{itemId}")
  public String actualizarCantidadItem(
      @PathVariable Long itemId,
      @RequestParam int cantidad,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    if (cantidad <= 0) {
      redirectAttributes.addFlashAttribute("error", "La cantidad debe ser mayor que cero");
      return "redirect:/mi-cuenta/carrito";
    }

    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para actualizar el carrito");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      carritoService.actualizarCantidadItemPorPerfil(perfil.getId(), itemId, cantidad);
      actualizarContadorCarritoEnSesion(session, perfil.getId());
      redirectAttributes.addFlashAttribute("mensaje", "Cantidad actualizada correctamente");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error al actualizar cantidad: " + e.getMessage());
    }

    return "redirect:/mi-cuenta/carrito";
  }

  @PostMapping("/eliminar/{itemId}")
  public String eliminarItemDelCarrito(
      @PathVariable Long itemId,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para eliminar productos del carrito");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    boolean eliminado = carritoService.eliminarItemDelCarritoPorPerfil(perfil.getId(), itemId);
    if (eliminado) {
      actualizarContadorCarritoEnSesion(session, perfil.getId());
      redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado del carrito");
    } else {
      redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el producto");
    }

    return "redirect:/mi-cuenta/carrito";
  }

  @PostMapping("/vaciar")
  public String vaciarCarrito(HttpSession session, RedirectAttributes redirectAttributes) {
    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para vaciar el carrito");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    boolean vaciado = carritoService.vaciarCarritoPorPerfil(perfil.getId());
    if (vaciado) {
      actualizarContadorCarritoEnSesion(session, perfil.getId());
      redirectAttributes.addFlashAttribute("mensaje", "Carrito vaciado correctamente");
    } else {
      redirectAttributes.addFlashAttribute("error", "No se pudo vaciar el carrito");
    }

    return "redirect:/mi-cuenta/carrito";
  }

  @PostMapping("/aplicar-cupon")
  public String aplicarCuponDescuento(
      @RequestParam String codigoCupon,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para aplicar cupones");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    boolean aplicado = carritoService.aplicarCuponDescuentoPorPerfil(perfil.getId(), codigoCupon);
    if (aplicado) {
      redirectAttributes.addFlashAttribute("mensaje", "Cupón aplicado correctamente");
    } else {
      redirectAttributes.addFlashAttribute("error", "El cupón no es válido o no se pudo aplicar");
    }

    return "redirect:/mi-cuenta/carrito";
  }

  @GetMapping("/comprar")
  public String procesarCompra(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para realizar la compra");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    // Verificar si el carrito tiene items
    CarritoResponseDTO carrito = carritoService.obtenerCarritoPorPerfil(perfil.getId()).orElse(null);
    if (carrito == null || carrito.getItems().isEmpty()) {
      redirectAttributes.addFlashAttribute("error", "No hay productos en el carrito para comprar");
      return "redirect:/mi-cuenta/carrito";
    }

    // Verificar disponibilidad de todos los items
    boolean disponible = carritoService.verificarDisponibilidadItemsPorPerfil(perfil.getId());
    if (!disponible) {
      redirectAttributes.addFlashAttribute("error",
          "Algunos productos no están disponibles. Por favor revise su carrito.");
      return "redirect:/mi-cuenta/carrito";
    }

    // Redirigir al checkout
    return "redirect:/mi-cuenta/orden/checkout";
  }

  @GetMapping("/agregar-rapido/{contenidoId}")
  public String agregarRapidoAlCarrito(
      @PathVariable Long contenidoId,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    Perfil perfil = obtenerPerfilActual(session);

    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Debe seleccionar un perfil para agregar productos al carrito");
      return "redirect:/mi-cuenta/perfiles/seleccionar";
    }

    try {
      // Crear un item con cantidad 1
      ItemCarritoRequestDTO itemDTO = new ItemCarritoRequestDTO();
      itemDTO.setContenidoId(contenidoId);
      itemDTO.setCantidad(1);

      carritoService.agregarItemAlCarritoPorPerfil(perfil.getId(), itemDTO);
      actualizarContadorCarritoEnSesion(session, perfil.getId());
      redirectAttributes.addFlashAttribute("mensaje", "Producto agregado al carrito");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error al agregar al carrito: " + e.getMessage());
    }

    return "redirect:/mi-cuenta/carrito";
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

  /**
   * Método auxiliar para actualizar el contador del carrito en la sesión
   */
  private void actualizarContadorCarritoEnSesion(HttpSession session, Long perfilId) {
    try {
      int itemsUnicos = carritoService.obtenerCarritoPorPerfil(perfilId)
          .map(carrito -> {
            if (carrito.getItems() != null) {
              return carrito.getItems().size();
            }
            return 0;
          })
          .orElse(0);

      session.setAttribute("carritoItems", itemsUnicos);
    } catch (Exception e) {
      log.error("Error al actualizar contador del carrito en sesión", e);
      session.setAttribute("carritoItems", 0);
    }
  }
}