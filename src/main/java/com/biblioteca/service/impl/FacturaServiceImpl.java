package com.biblioteca.service.impl;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.biblioteca.dto.comercial.FacturaRequestDTO;
import com.biblioteca.dto.comercial.FacturaResponseDTO;
import com.biblioteca.mapper.comercial.FacturaMapper;
import com.biblioteca.models.Perfil;
import com.biblioteca.models.comercial.Factura;
import com.biblioteca.models.comercial.ItemOrden;
import com.biblioteca.models.comercial.Orden;
import com.biblioteca.models.comercial.Pago;
import com.biblioteca.service.FacturaService;
import com.biblioteca.service.OrdenService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FacturaServiceImpl implements FacturaService {

  private final Map<Long, Factura> facturas = new ConcurrentHashMap<>();
  private final AtomicLong facturaIdCounter = new AtomicLong(0);
  private final FacturaMapper facturaMapper;
  private final OrdenService ordenService;
  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;

  // Formato para generar números de factura
  private static final String FORMATO_NUMERO_FACTURA = "FAC-%d-%s";
  private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyyMMdd");

  // Porcentaje de impuestos (configurable)
  private static final double PORCENTAJE_IMPUESTOS = 0.18; // 18%

  @PostConstruct
  public void initFacturasData() {
    try {
      InputStream inputStream = resourceLoader.getResource("classpath:data/facturas-data.json").getInputStream();
      List<FacturaRequestDTO> facturasDTOs = objectMapper.readValue(inputStream,
          new TypeReference<List<FacturaRequestDTO>>() {
          });

      for (FacturaRequestDTO dto : facturasDTOs) {
        try {
          crearFactura(null, dto); // El username se ignorará porque ya tenemos la orden
        } catch (Exception e) {
          System.err.println("Error al cargar factura con orden ID " + dto.getOrdenId() + ": " + e.getMessage());
        }
      }

      System.out.println("Datos iniciales de Facturas cargados desde JSON: " + facturas.size() + " facturas.");
    } catch (Exception e) {
      System.err.println("Error al cargar datos iniciales de facturas desde JSON: " + e.getMessage());
    }
  }

  @Override
  public FacturaResponseDTO crearFactura(String username, FacturaRequestDTO facturaDTO) {
    // Verificar que la orden existe
    Orden orden = ordenService.obtenerEntidadOrdenPorId(facturaDTO.getOrdenId())
        .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + facturaDTO.getOrdenId()));

    // Verificar si la orden ya tiene una factura
    if (orden.getFactura() != null) {
      throw new IllegalStateException("La orden ya tiene una factura asociada");
    }

    // Crear la factura
    Factura factura = facturaMapper.toEntity(facturaDTO);
    factura.setId(facturaIdCounter.incrementAndGet());
    factura.setFechaEmision(LocalDate.now());
    factura.setOrden(orden);

    // Si no se especifica un número de factura, generarlo automáticamente
    if (factura.getNumeroFactura() == null || factura.getNumeroFactura().isEmpty()) {
      factura.setNumeroFactura(generarNumeroFactura(factura.getId()));
    }

    // Actualizar la orden para referenciar esta factura
    orden.setFactura(factura);

    facturas.put(factura.getId(), factura);
    return facturaMapper.toResponseDTO(factura);
  }

  @Override
  public Optional<Factura> generarFacturaDesdeOrden(Orden orden) {
    if (orden == null) {
      return Optional.empty();
    }

    // Verificar si la orden ya tiene una factura
    if (orden.getFactura() != null) {
      return Optional.of(orden.getFactura());
    }

    // Calcular subtotal: suma de (precio - descuento) * cantidad de cada ítem
    int subtotal = 0;
    for (ItemOrden item : orden.getItems()) {
      int precio = item.getPrecioAlComprar() != null ? item.getPrecioAlComprar() : 0;
      int descuento = item.getDescuentoAplicado() != null ? item.getDescuentoAplicado() : 0;
      int cantidad = item.getCantidad() != null ? item.getCantidad() : 1;

      subtotal += ((precio - descuento) * cantidad);
    }

    // Calcular impuestos
    int impuestos = (int) Math.round(subtotal * PORCENTAJE_IMPUESTOS);

    // Calcular total
    int total = subtotal + impuestos;

    // Crear datos de facturación
    String datosFacturacion = generarDatosFacturacion(orden);

    // Crear la factura
    Factura factura = Factura.builder()
        .id(facturaIdCounter.incrementAndGet())
        .numeroFactura(generarNumeroFactura(facturaIdCounter.get()))
        .fechaEmision(LocalDate.now())
        .subtotal(subtotal)
        .impuestos(impuestos)
        .total(total)
        .datosFacturacion(datosFacturacion)
        .orden(orden)
        .build();

    // Actualizar la orden para referenciar esta factura
    orden.setFactura(factura);

    facturas.put(factura.getId(), factura);
    return Optional.of(factura);
  }

  @Override
  public Optional<FacturaResponseDTO> obtenerFacturaPorId(Long id) {
    return Optional.ofNullable(facturas.get(id))
        .map(facturaMapper::toResponseDTO);
  }

  @Override
  public List<FacturaResponseDTO> obtenerFacturasPorPerfil(Long perfilId) {
    return facturas.values().stream()
        .filter(f -> f.getOrden() != null && f.getOrden().getPerfil() != null &&
            perfilId.equals(f.getOrden().getPerfil().getId()))
        .map(facturaMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<FacturaResponseDTO> obtenerTodasLasFacturas() {
    return facturas.values().stream()
        .map(facturaMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<FacturaResponseDTO> buscarFacturasPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
    return facturas.values().stream()
        .filter(f -> !f.getFechaEmision().isBefore(fechaInicio) &&
            !f.getFechaEmision().isAfter(fechaFin))
        .map(facturaMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public boolean anularFactura(Long id, String motivo) {
    return obtenerEntidadFacturaPorId(id)
        .map(factura -> {
          // Marcar la factura como anulada y se registraría el motivo
          System.out.println("Factura " + id + " anulada. Motivo: " + motivo);

          // Actualizaríamos el estado de la orden
          if (factura.getOrden() != null) {
            try {
              ordenService.cancelarOrden(factura.getOrden().getId(), "Factura anulada: " + motivo);
            } catch (Exception e) {
              System.err.println("Error al cancelar orden relacionada: " + e.getMessage());
            }
          }

          return true;
        })
        .orElse(false);
  }

  @Override
  public Optional<Factura> obtenerEntidadFacturaPorId(Long id) {
    return Optional.ofNullable(facturas.get(id));
  }

  @Override
  public Map<String, Object> generarReporteVentas(LocalDate fechaInicio, LocalDate fechaFin) {
    List<Factura> facturasEnRango = facturas.values().stream()
        .filter(f -> !f.getFechaEmision().isBefore(fechaInicio) &&
            !f.getFechaEmision().isAfter(fechaFin))
        .collect(Collectors.toList());

    // Calcular totales
    int totalVentas = facturasEnRango.size();
    double montoTotalVentas = facturasEnRango.stream()
        .mapToDouble(f -> f.getTotal() / 100.0)
        .sum();

    // Agrupar por día
    Map<LocalDate, List<Factura>> ventasPorDia = facturasEnRango.stream()
        .collect(Collectors.groupingBy(Factura::getFechaEmision));

    // Convertir a formato de respuesta
    Map<String, Double> ventasDiarias = new HashMap<>();
    for (Map.Entry<LocalDate, List<Factura>> entry : ventasPorDia.entrySet()) {
      String fecha = entry.getKey().format(DateTimeFormatter.ISO_DATE);
      double monto = entry.getValue().stream()
          .mapToDouble(f -> f.getTotal() / 100.0)
          .sum();
      ventasDiarias.put(fecha, monto);
    }

    // Crear el reporte
    Map<String, Object> reporte = new HashMap<>();
    reporte.put("fechaInicio", fechaInicio);
    reporte.put("fechaFin", fechaFin);
    reporte.put("totalVentas", totalVentas);
    reporte.put("montoTotalVentas", montoTotalVentas);
    reporte.put("ventasDiarias", ventasDiarias);

    return reporte;
  }

  @Override
  public Map<String, Object> obtenerEstadisticasVentas() {
    // Obtener todas las facturas
    List<Factura> todasLasFacturas = new ArrayList<>(facturas.values());

    // Calcular estadísticas generales
    int totalFacturas = todasLasFacturas.size();
    double montoTotal = todasLasFacturas.stream()
        .mapToDouble(f -> f.getTotal() / 100.0)
        .sum();

    // Calcular ventas del mes actual
    LocalDate hoy = LocalDate.now();
    LocalDate inicioDeMes = hoy.withDayOfMonth(1);

    List<Factura> facturasMesActual = todasLasFacturas.stream()
        .filter(f -> !f.getFechaEmision().isBefore(inicioDeMes) &&
            !f.getFechaEmision().isAfter(hoy))
        .collect(Collectors.toList());

    int totalFacturasMesActual = facturasMesActual.size();
    double montoTotalMesActual = facturasMesActual.stream()
        .mapToDouble(f -> f.getTotal() / 100.0)
        .sum();

    // Crear mapa de estadísticas
    Map<String, Object> estadisticas = new HashMap<>();
    estadisticas.put("totalFacturas", totalFacturas);
    estadisticas.put("montoTotal", montoTotal);
    estadisticas.put("totalFacturasMesActual", totalFacturasMesActual);
    estadisticas.put("montoTotalMesActual", montoTotalMesActual);

    return estadisticas;
  }

  /**
   * Genera un número de factura único basado en el ID y la fecha actual.
   */
  private String generarNumeroFactura(Long id) {
    String fechaStr = LocalDate.now().format(FORMATO_FECHA);
    return String.format(FORMATO_NUMERO_FACTURA, id, fechaStr);
  }

  /**
   * Genera un objeto JSON con los datos de facturación a partir de una orden.
   */
  private String generarDatosFacturacion(Orden orden) {
    try {
      Map<String, Object> datos = new HashMap<>();

      // Información del cliente
      if (orden.getPerfil() != null) {
        Perfil perfil = orden.getPerfil();
        Map<String, Object> cliente = new HashMap<>();

        if (perfil.getUsuario() != null) {
          cliente.put("email", perfil.getUsuario().getEmail());
          cliente.put("username", perfil.getUsuario().getUsername());
        }

        cliente.put("nombreVisible", perfil.getNombreVisible());

        datos.put("cliente", cliente);
      }

      // Información del pago
      if (!orden.getPagos().isEmpty()) {
        Pago ultimoPago = orden.getPagos().stream()
            .filter(p -> "Completado".equals(p.getEstado()))
            .findFirst()
            .orElse(null);

        if (ultimoPago != null) {
          Map<String, Object> pago = new HashMap<>();
          pago.put("metodoPago",
              ultimoPago.getMetodoPago() != null ? ultimoPago.getMetodoPago().getNombre() : "Desconocido");
          pago.put("referencia", ultimoPago.getReferenciaPago());
          pago.put("fecha", ultimoPago.getFechaPago().toString());

          datos.put("pago", pago);
        }
      }

      // Información de los ítems
      List<Map<String, Object>> items = new ArrayList<>();
      for (ItemOrden item : orden.getItems()) {
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("contenido", item.getContenido());
        itemData.put("cantidad", item.getCantidad());
        itemData.put("precioUnitario", item.getPrecioAlComprar());
        itemData.put("descuento", item.getDescuentoAplicado());

        if (item.getContenido() != null && item.getContenido().getObra() != null) {
          itemData.put("titulo", item.getContenido().getObra().getTitulo());
        }

        items.add(itemData);
      }
      datos.put("items", items);

      // Convertir a JSON
      return objectMapper.writeValueAsString(datos);
    } catch (Exception e) {
      System.err.println("Error al generar datos de facturación: " + e.getMessage());
      return "{}";
    }
  }
}