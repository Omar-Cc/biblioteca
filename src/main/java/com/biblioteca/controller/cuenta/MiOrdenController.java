package com.biblioteca.controller.cuenta;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
import com.biblioteca.enums.EstadoPago;
import com.biblioteca.models.acceso.Perfil;
import com.biblioteca.service.CarritoService;
import com.biblioteca.service.FacturaService;
import com.biblioteca.service.MetodoPagoService;
import com.biblioteca.service.OrdenService;
import com.biblioteca.service.PagoService;
import com.biblioteca.service.PerfilService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/mi-cuenta/orden")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LECTOR')")
public class MiOrdenController {

  private final OrdenService ordenService;
  private final CarritoService carritoService;
  private final PerfilService perfilService;
  private final MetodoPagoService metodoPagoService;
  private final PagoService pagoService;
  private final FacturaService facturaService;

  @GetMapping
  @Transactional(readOnly = true)
  public String listarOrdenes(
      @RequestParam(required = false) String estado,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
      Model model,
      HttpSession session) {

    // Obtener el perfil del usuario actual
    Perfil perfil = obtenerPerfilActual(session);
    if (perfil == null) {
      return "redirect:/mi-cuenta/perfiles/seleccionar?error=Se+requiere+un+perfil+para+ver+órdenes";
    }

    List<OrdenResponseDTO> ordenes;

    // Aplicar filtros si están presentes
    if (estado != null || fechaDesde != null || fechaHasta != null) {
      ordenes = ordenService.obtenerOrdenesPorPerfilConFiltros(perfil.getId(), estado, fechaDesde, fechaHasta);
    } else {
      ordenes = ordenService.obtenerOrdenesPorPerfil(perfil.getId());
    }

    model.addAttribute("ordenes", ordenes);

    // Calcular estadísticas basadas en los resultados filtrados
    long totalOrdenes = ordenes.size();
    long ordenesPendientes = ordenes.stream().filter(o -> "Pendiente".equals(o.getEstadoOrden())).count();
    long ordenesCompletadas = ordenes.stream().filter(o -> "Completada".equals(o.getEstadoOrden())).count();
    long ordenesCanceladas = ordenes.stream().filter(o -> "Cancelada".equals(o.getEstadoOrden())).count();

    model.addAttribute("totalOrdenes", totalOrdenes);
    model.addAttribute("ordenesPendientes", ordenesPendientes);
    model.addAttribute("ordenesCompletadas", ordenesCompletadas);
    model.addAttribute("ordenesCanceladas", ordenesCanceladas);

    // Mantener los filtros en el modelo para que se muestren en el formulario
    model.addAttribute("filtroEstado", estado);
    model.addAttribute("filtroFechaDesde", fechaDesde);
    model.addAttribute("filtroFechaHasta", fechaHasta);

    // Indicar si hay filtros activos
    boolean filtrosActivos = estado != null || fechaDesde != null || fechaHasta != null;
    model.addAttribute("filtrosActivos", filtrosActivos);

    return "mi-cuenta/ordenes/lista-mis-ordenes";
  }

  @GetMapping("/{id}")
  @Transactional(readOnly = true)
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

          return "mi-cuenta/ordenes/detalle-mi-orden";
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

    // Pasar datos del carrito específico del perfil
    carritoService.obtenerCarritoPorPerfil(perfil.getId())
        .ifPresent(carrito -> model.addAttribute("carrito", carrito));

    // Agregar perfil al modelo
    model.addAttribute("perfil", perfil);

    // Preparar DTO para la orden
    model.addAttribute("ordenForm", new OrdenRequestDTO());

    return "mi-cuenta/ordenes/checkout-orden";
  }

  @PostMapping("/crear")
  public String crearOrden(
      @ModelAttribute("ordenForm") OrdenRequestDTO ordenDTO,
      @RequestParam Long metodoPagoId,
      @RequestParam String tipoEntrega,
      @RequestParam(required = false) String nombre,
      @RequestParam(required = false) String telefono,
      @RequestParam(required = false) String direccion,
      @RequestParam(required = false) String ciudad,
      @RequestParam(required = false) String codigoPostal,
      @RequestParam(required = false) String numeroTarjeta,
      @RequestParam(required = false) String nombreTarjeta,
      @RequestParam(required = false) String cvv,
      @RequestParam(required = false) String mesVencimiento,
      @RequestParam(required = false) String anioVencimiento,
      Model model,
      RedirectAttributes redirectAttributes,
      HttpSession session) {

    log.info("Creando orden para método de pago: {}, tipo entrega: {}", metodoPagoId, tipoEntrega);

    Perfil perfil = obtenerPerfilActual(session);
    if (perfil == null) {
      redirectAttributes.addFlashAttribute("error", "Se requiere un perfil para realizar compras");
      return "redirect:/mi-cuenta/perfiles/crear";
    }

    // Validaciones específicas según tipo de entrega
    if ("DELIVERY".equals(tipoEntrega)) {
      if (nombre == null || nombre.trim().isEmpty() ||
          telefono == null || telefono.trim().isEmpty() ||
          direccion == null || direccion.trim().isEmpty() ||
          ciudad == null || ciudad.trim().isEmpty()) {
        redirectAttributes.addFlashAttribute("error",
            "Todos los campos de entrega son obligatorios para delivery");
        return "redirect:/mi-cuenta/orden/checkout";
      }
    }

    // Validar datos de tarjeta si es necesario
    MetodoPagoResponseDTO metodoPago = metodoPagoService.obtenerMetodoPagoPorId(metodoPagoId)
        .orElse(null);

    if (metodoPago != null &&
        (metodoPago.getTipo().contains("TARJETA") || metodoPago.getTipo().contains("CREDITO"))) {
      if (numeroTarjeta == null || numeroTarjeta.trim().isEmpty() ||
          nombreTarjeta == null || nombreTarjeta.trim().isEmpty() ||
          cvv == null || cvv.trim().isEmpty() ||
          mesVencimiento == null || mesVencimiento.trim().isEmpty() ||
          anioVencimiento == null || anioVencimiento.trim().isEmpty()) {
        redirectAttributes.addFlashAttribute("error",
            "Todos los campos de la tarjeta son obligatorios");
        return "redirect:/mi-cuenta/orden/checkout";
      }
    }

    try {
      // Crear la orden desde el carrito
      OrdenResponseDTO orden = ordenService.crearOrdenDesdeCarrito(perfil.getId());

      // Crear datos de pago con información adicional
      PagoRequestDTO pagoDTO = new PagoRequestDTO();
      pagoDTO.setOrdenId(orden.getId());
      pagoDTO.setMetodoPagoId(metodoPagoId);
      pagoDTO.setMonto(orden.getTotalOrden());
      pagoDTO.setEstado(EstadoPago.PENDIENTE.name());
      pagoDTO.setSuscripcionId(null);

      // Agregar información de entrega como referencia
      StringBuilder referencia = new StringBuilder();
      referencia.append("Tipo: ").append(tipoEntrega);

      if ("DELIVERY".equals(tipoEntrega)) {
        referencia.append(" | Entrega: ").append(nombre)
            .append(" | Tel: ").append(telefono)
            .append(" | Dir: ").append(direccion).append(", ").append(ciudad);
        if (codigoPostal != null && !codigoPostal.trim().isEmpty()) {
          referencia.append(" ").append(codigoPostal);
        }
      }

      // Agregar datos de tarjeta (solo últimos 4 dígitos por seguridad)
      if (numeroTarjeta != null && !numeroTarjeta.trim().isEmpty()) {
        String ultimosDigitos = numeroTarjeta.replaceAll("\\s", "").substring(
            Math.max(0, numeroTarjeta.replaceAll("\\s", "").length() - 4));
        referencia.append(" | Tarjeta: ****").append(ultimosDigitos);
      }

      pagoDTO.setReferenciaPago(referencia.toString());

      // Registrar el pago
      pagoService.registrarPago(pagoDTO);

      // Procesar la orden (esto generará la factura)
      OrdenResponseDTO ordenProcesada = ordenService.procesarOrden(orden.getId());

      log.info("Orden creada exitosamente: {}", ordenProcesada.getId());
      redirectAttributes.addFlashAttribute("mensaje",
          "Orden #" + ordenProcesada.getId() + " creada exitosamente");
      return "redirect:/mi-cuenta/orden/" + ordenProcesada.getId();

    } catch (Exception e) {
      log.error("Error al crear orden: {}", e.getMessage(), e);
      redirectAttributes.addFlashAttribute("error", "Error al crear la orden: " + e.getMessage());
      return "redirect:/mi-cuenta/orden/checkout";
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

    return "mi-cuenta/ordenes/historial-ordenes";
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

  private String getCurrentUsername(Principal principal) {
    if (principal == null) {
      throw new UsernameNotFoundException("Usuario no autenticado.");
    }
    return principal.getName();
  }

}