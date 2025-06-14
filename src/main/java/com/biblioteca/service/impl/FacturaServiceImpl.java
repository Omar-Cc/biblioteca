package com.biblioteca.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.comercial.FacturaRequestDTO;
import com.biblioteca.dto.comercial.FacturaResponseDTO;
import com.biblioteca.enums.EstadoPago;
import com.biblioteca.exceptions.RecursoNoEncontradoException;
import com.biblioteca.mapper.comercial.FacturaMapper;
import com.biblioteca.models.acceso.Perfil;
import com.biblioteca.models.comercial.Factura;
import com.biblioteca.models.comercial.ItemOrden;
import com.biblioteca.models.comercial.Orden;
import com.biblioteca.models.comercial.Pago;
import com.biblioteca.repositories.comercial.FacturaRepository;
import com.biblioteca.repositories.comercial.OrdenRepository;
import com.biblioteca.service.FacturaService;
import com.biblioteca.service.OrdenService; // Mantener si se usa para lógica compleja como cancelarOrden
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacturaServiceImpl implements FacturaService {

  private final FacturaRepository facturaRepository;
  private final OrdenRepository ordenRepository;
  private final FacturaMapper facturaMapper;
  private final OrdenService ordenService;
  private final ObjectMapper objectMapper;

  private static final String FORMATO_NUMERO_FACTURA = "FAC-%06d-%s";
  private static final DateTimeFormatter FORMATO_FECHA_NUM_FACTURA = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static final double PORCENTAJE_IMPUESTOS = 0.18; // 18%

  @Override
  @Transactional
  public FacturaResponseDTO crearFactura(FacturaRequestDTO facturaDTO) {
    Orden orden = ordenRepository.findById(facturaDTO.getOrdenId())
        .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con ID: " + facturaDTO.getOrdenId()));

    if (facturaRepository.existsByOrdenId(orden.getId())) {
      throw new IllegalStateException("La orden ya tiene una factura asociada.");
    }

    // Crear la factura
    Factura factura = new Factura(); // ID será generado por la BD
    factura.setFechaEmision(LocalDate.now());
    factura.setOrden(orden);
    factura.setDatosFacturacion(
        facturaDTO.getDatosFacturacion() != null ? facturaDTO.getDatosFacturacion() : generarDatosFacturacion(orden));

    // Calcular montos
    calcularYEstablecerMontosFactura(factura, orden);

    // Guardar factura para obtener ID y luego generar número de factura
    Factura facturaGuardadaPreliminar = facturaRepository.save(factura);
    facturaGuardadaPreliminar.setNumeroFactura(generarNumeroFactura(facturaGuardadaPreliminar.getId()));

    Factura facturaFinalGuardada = facturaRepository.save(facturaGuardadaPreliminar);

    // Actualizar la orden para referenciar esta factura (si la relación es
    // bidireccional y no se maneja por JPA automáticamente al guardar factura)
    // Esto usualmente no es necesario si la relación está bien mapeada y la factura
    // es la dueña de la relación con la orden (o viceversa con mappedBy)
    // orden.setFactura(facturaFinalGuardada); // Comprobar si es necesario
    // ordenRepository.save(orden);

    return facturaMapper.toResponseDTO(facturaFinalGuardada);
  }

  @Override
  @Transactional
  public Optional<Factura> generarFacturaDesdeOrden(Orden orden) {
    if (orden == null) {
      return Optional.empty();
    }
    if (orden.getId() == null) {
      throw new IllegalArgumentException("La orden debe estar persistida para generar una factura.");
    }

    // Verificar si ya existe una factura para esta orden
    if (facturaRepository.existsByOrdenId(orden.getId())) {
      return facturaRepository.findByOrdenId(orden.getId());
    }

    try {
      Factura factura = new Factura();
      factura.setFechaEmision(LocalDate.now());
      factura.setOrden(orden);

      // Generar datos de facturación más compactos
      factura.setDatosFacturacion(generarDatosFacturacionCompactos(orden));

      calcularYEstablecerMontosFactura(factura, orden);

      Factura facturaGuardada = facturaRepository.save(factura);

      // Generar número de factura después de tener el ID
      facturaGuardada.setNumeroFactura(generarNumeroFactura(facturaGuardada.getId()));

      Factura facturaFinal = facturaRepository.save(facturaGuardada);

      return Optional.of(facturaFinal);

    } catch (Exception e) {
      log.error("Error al generar factura para orden {}: {}", orden.getId(), e.getMessage());
      throw new RuntimeException("Error al generar factura: " + e.getMessage(), e);
    }
  }

  private void calcularYEstablecerMontosFactura(Factura factura, Orden orden) {
    int subtotal = 0;
    for (ItemOrden item : orden.getItems()) {
      int precio = item.getPrecioAlComprar() != null ? item.getPrecioAlComprar() : 0;
      int descuento = item.getDescuentoAplicado() != null ? item.getDescuentoAplicado() : 0;
      int cantidad = item.getCantidad() != null ? item.getCantidad() : 1;
      subtotal += ((precio - descuento) * cantidad);
    }
    int impuestos = (int) Math.round(subtotal * PORCENTAJE_IMPUESTOS);
    int total = subtotal + impuestos;

    factura.setSubtotal(subtotal);
    factura.setImpuestos(impuestos);
    factura.setTotal(total);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<FacturaResponseDTO> obtenerFacturaPorId(Long id) {
    return facturaRepository.findById(id)
        .map(factura -> {
          // Forzar carga de relaciones para el mapper
          if (factura.getOrden() != null) {
            if (factura.getOrden().getPerfil() != null) {
              factura.getOrden().getPerfil().getNombreVisible(); // Fuerza carga
              if (factura.getOrden().getPerfil().getUsuario() != null) {
                factura.getOrden().getPerfil().getUsuario().getEmail(); // Fuerza carga
              }
            }
          }
          return facturaMapper.toResponseDTO(factura);
        });
  }

  @Override
  @Transactional(readOnly = true)
  public List<FacturaResponseDTO> obtenerFacturasPorPerfil(Long perfilId) {
    return facturaRepository.findByOrden_Perfil_Id(perfilId).stream()
        .map(factura -> {
          // Forzar carga de relaciones para cada factura
          if (factura.getOrden() != null) {
            if (factura.getOrden().getPerfil() != null) {
              factura.getOrden().getPerfil().getNombreVisible(); // Fuerza carga
              if (factura.getOrden().getPerfil().getUsuario() != null) {
                factura.getOrden().getPerfil().getUsuario().getEmail(); // Fuerza carga
              }
            }
          }
          return facturaMapper.toResponseDTO(factura);
        })
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<FacturaResponseDTO> obtenerTodasLasFacturas() {
    return facturaRepository.findAll().stream()
        .map(facturaMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<FacturaResponseDTO> buscarFacturasPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
    return facturaRepository.findByFechaEmisionBetween(fechaInicio, fechaFin).stream()
        .map(facturaMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public boolean anularFactura(Long id, String motivo) {
    Optional<Factura> facturaOpt = facturaRepository.findById(id);
    if (facturaOpt.isPresent()) {
      Factura factura = facturaOpt.get();
      if ("ANULADA".equals(factura.getEstado())) {
        return false; // Ya está anulada
      }
      factura.setEstado("ANULADA"); // Asumiendo que Factura tiene un campo estado
      factura.setMotivoAnulacion(motivo); // Asumiendo que Factura tiene un campo motivoAnulacion
      facturaRepository.save(factura);

      if (factura.getOrden() != null) {
        try {
          // Usar OrdenService para lógica de cancelación de orden si es compleja
          ordenService.cancelarOrden(factura.getOrden().getId(), "Factura anulada: " + motivo);
        } catch (Exception e) {
          // Log error, pero la anulación de factura puede proceder
          System.err.println("Error al cancelar orden relacionada con factura " + id + ": " + e.getMessage());
        }
      }
      return true;
    }
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Factura> obtenerEntidadFacturaPorId(Long id) {
    try {
      return facturaRepository.findById(id)
          .map(factura -> {
            // Forzar la carga de las relaciones necesarias para el PDF
            if (factura.getOrden() != null) {
              // Cargar perfil
              if (factura.getOrden().getPerfil() != null) {
                factura.getOrden().getPerfil().getNombreVisible(); // ✅ Campo correcto
                if (factura.getOrden().getPerfil().getUsuario() != null) {
                  factura.getOrden().getPerfil().getUsuario().getEmail(); // Fuerza la carga
                }
              }
              // Cargar items
              if (factura.getOrden().getItems() != null) {
                factura.getOrden().getItems().size(); // Fuerza la carga de la colección
                factura.getOrden().getItems().forEach(item -> {
                  // Verificar y cargar contenido y obra
                  if (item.getContenido() != null) {
                    item.getContenido().getTipo(); // Fuerza carga del contenido
                    if (item.getContenido().getObra() != null) {
                      item.getContenido().getObra().getTitulo(); // Fuerza carga de la obra
                      if (item.getContenido().getObra().getAutoresObras() != null) {
                        item.getContenido().getObra().getAutoresObras().size(); // Fuerza carga de la colección
                        item.getContenido().getObra().getAutoresObras().forEach(autorObra -> {
                          if (autorObra.getAutor() != null) {
                            autorObra.getAutor().getNombre(); // Fuerza carga del autor
                            autorObra.getRol(); // Fuerza carga del rol
                          }
                        });
                      }
                    }
                  }
                });
              }
            }
            return factura;
          });
    } catch (Exception e) {
      log.error("Error obteniendo entidad factura por ID {}: {}", id, e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> generarReporteVentas(LocalDate fechaInicio, LocalDate fechaFin) {
    List<Factura> facturasEnRango = facturaRepository.findByFechaEmisionBetweenAndEstadoNot(fechaInicio, fechaFin,
        "ANULADA");

    int totalVentas = facturasEnRango.size();
    double montoTotalVentas = facturasEnRango.stream()
        .mapToDouble(f -> f.getTotal() / 100.0) // Asumiendo que total está en centavos
        .sum();

    Map<LocalDate, List<Factura>> ventasPorDia = facturasEnRango.stream()
        .collect(Collectors.groupingBy(Factura::getFechaEmision));

    Map<String, Double> ventasDiarias = new HashMap<>();
    ventasPorDia.forEach((fecha, listaFacturas) -> {
      String fechaStr = fecha.format(DateTimeFormatter.ISO_DATE);
      double monto = listaFacturas.stream().mapToDouble(f -> f.getTotal() / 100.0).sum();
      ventasDiarias.put(fechaStr, monto);
    });

    Map<String, Object> reporte = new HashMap<>();
    reporte.put("fechaInicio", fechaInicio.toString());
    reporte.put("fechaFin", fechaFin.toString());
    reporte.put("totalVentas", totalVentas);
    reporte.put("montoTotalVentas", montoTotalVentas);
    reporte.put("ventasDiarias", ventasDiarias);
    return reporte;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> obtenerEstadisticasVentas() {
    List<Factura> todasLasFacturasNoAnuladas = facturaRepository.findByEstadoNot("ANULADA");

    int totalFacturas = todasLasFacturasNoAnuladas.size();
    double montoTotal = todasLasFacturasNoAnuladas.stream().mapToDouble(f -> f.getTotal() / 100.0).sum();

    LocalDate hoy = LocalDate.now();
    LocalDate inicioDeMes = hoy.withDayOfMonth(1);
    List<Factura> facturasMesActual = facturaRepository.findByFechaEmisionBetweenAndEstadoNot(inicioDeMes, hoy,
        "ANULADA");

    int totalFacturasMesActual = facturasMesActual.size();
    double montoTotalMesActual = facturasMesActual.stream().mapToDouble(f -> f.getTotal() / 100.0).sum();

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
  private String generarNumeroFactura(Long idFactura) {
    String fechaStr = LocalDate.now().format(FORMATO_FECHA_NUM_FACTURA);
    return String.format(FORMATO_NUMERO_FACTURA, idFactura, fechaStr);
  }

  /**
   * Genera un objeto JSON con los datos de facturación a partir de una orden.
   */
  private String generarDatosFacturacion(Orden orden) {
    try {
      Map<String, Object> datos = new HashMap<>();
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

      if (!orden.getPagos().isEmpty()) {
        Pago ultimoPago = orden.getPagos().stream()
            .filter(p -> EstadoPago.EXITOSO.equals(p.getEstado())) // Asumiendo estado "COMPLETADO"
            .findFirst().orElse(null);
        if (ultimoPago != null) {
          Map<String, Object> pagoInfo = new HashMap<>();
          pagoInfo.put("metodoPago",
              ultimoPago.getMetodoPago() != null ? ultimoPago.getMetodoPago().getNombre() : "Desconocido");
          pagoInfo.put("referencia", ultimoPago.getReferenciaPago());
          pagoInfo.put("fecha", ultimoPago.getFechaPago() != null ? ultimoPago.getFechaPago().toString() : "N/A");
          datos.put("pago", pagoInfo);
        }
      }

      List<Map<String, Object>> itemsFactura = orden.getItems().stream().map(itemOrden -> {
        Map<String, Object> itemData = new HashMap<>();
        if (itemOrden.getContenido() != null && itemOrden.getContenido().getObra() != null) {
          itemData.put("titulo", itemOrden.getContenido().getObra().getTitulo());
        } else {
          itemData.put("titulo", "Contenido Desconocido");
        }
        itemData.put("cantidad", itemOrden.getCantidad());
        itemData.put("precioUnitario", itemOrden.getPrecioAlComprar());
        itemData.put("descuentoAplicado", itemOrden.getDescuentoAplicado());
        itemData.put("subtotalItem",
            (itemOrden.getPrecioAlComprar() - itemOrden.getDescuentoAplicado()) * itemOrden.getCantidad());
        return itemData;
      }).collect(Collectors.toList());
      datos.put("items", itemsFactura);

      return objectMapper.writeValueAsString(datos);
    } catch (Exception e) {
      System.err.println("Error al generar datos de facturación JSON: " + e.getMessage());
      return "{\"error\":\"Error al generar datos de facturación\"}";
    }
  }

  private String generarDatosFacturacionCompactos(Orden orden) {
    try {
      Map<String, Object> datosBasicos = new HashMap<>();

      // Datos del perfil (solo lo esencial)
      Perfil perfil = orden.getPerfil();
      if (perfil != null) {
        Map<String, Object> perfilData = new HashMap<>();
        perfilData.put("id", perfil.getId());
        perfilData.put("nombre", perfil.getNombreVisible());
        perfilData.put("email", perfil.getUsuario() != null ? perfil.getUsuario().getEmail() : "");
        datosBasicos.put("perfil", perfilData);
      }

      // Datos de la orden (solo lo esencial)
      Map<String, Object> ordenData = new HashMap<>();
      ordenData.put("id", orden.getId());
      ordenData.put("fecha", orden.getFechaCreacion().toString());
      ordenData.put("total", orden.getTotalOrden());
      datosBasicos.put("orden", ordenData);

      // Items resumidos
      List<Map<String, Object>> itemsResumidos = orden.getItems().stream()
          .map(item -> {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("contenidoId", item.getContenido().getId());
            itemData.put("titulo", item.getContenido().getObra().getTitulo());
            itemData.put("cantidad", item.getCantidad());
            itemData.put("precio", item.getPrecioAlComprar());
            return itemData;
          })
          .collect(Collectors.toList());

      datosBasicos.put("items", itemsResumidos);

      return objectMapper.writeValueAsString(datosBasicos);

    } catch (Exception e) {
      // Agregar import para log si no existe
      System.err.println("Error al generar datos de facturación JSON, usando formato simple: " + e.getMessage());
      // Fallback a formato simple si falla el JSON
      return String.format("Factura para Orden #%d - Perfil: %s - Total: $%.2f",
          orden.getId(),
          orden.getPerfil().getNombreVisible(),
          orden.getTotalOrden() / 100.0);
    }
  }
}