package com.biblioteca.service.impl;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.biblioteca.dto.comercial.ItemOrdenRequestDTO;
import com.biblioteca.dto.comercial.ItemOrdenResponseDTO;
import com.biblioteca.dto.comercial.OrdenRequestDTO;
import com.biblioteca.dto.comercial.OrdenResponseDTO;
import com.biblioteca.mapper.comercial.ItemOrdenMapper;
import com.biblioteca.mapper.comercial.OrdenMapper;
import com.biblioteca.models.Perfil;
import com.biblioteca.models.comercial.Carrito;
import com.biblioteca.models.comercial.Factura;
import com.biblioteca.models.comercial.ItemCarrito;
import com.biblioteca.models.comercial.ItemOrden;
import com.biblioteca.models.comercial.Orden;
import com.biblioteca.models.contenido.Contenido;
import com.biblioteca.service.CarritoService;
import com.biblioteca.service.ContenidoService;
import com.biblioteca.service.FacturaService;
import com.biblioteca.service.OrdenService;
import com.biblioteca.service.PerfilService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class OrdenServiceImpl implements OrdenService {

  private final Map<Long, Orden> ordenes = new ConcurrentHashMap<>();
  private final AtomicLong ordenIdCounter = new AtomicLong(0);
  private final AtomicLong itemOrdenIdCounter = new AtomicLong(0);

  private final OrdenMapper ordenMapper;
  private final ItemOrdenMapper itemOrdenMapper;
  private final PerfilService perfilService;
  private final CarritoService carritoService;
  private final ContenidoService contenidoService;

  private final FacturaService facturaService;
  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;

  // Estados posibles de una orden
  public static final String ESTADO_PENDIENTE = "Pendiente";
  public static final String ESTADO_PROCESANDO = "Procesando";
  public static final String ESTADO_COMPLETADA = "Completada";
  public static final String ESTADO_CANCELADA = "Cancelada";
  public static final String ESTADO_FALLIDA = "Fallida";

  public OrdenServiceImpl(
      OrdenMapper ordenMapper,
      ItemOrdenMapper itemOrdenMapper,
      PerfilService perfilService,
      CarritoService carritoService,
      ContenidoService contenidoService,
      @Lazy FacturaService facturaService, 
      ObjectMapper objectMapper,
      ResourceLoader resourceLoader) {

    this.ordenMapper = ordenMapper;
    this.itemOrdenMapper = itemOrdenMapper;
    this.perfilService = perfilService;
    this.carritoService = carritoService;
    this.contenidoService = contenidoService;
    this.facturaService = facturaService;
    this.objectMapper = objectMapper;
    this.resourceLoader = resourceLoader;
  }

  @PostConstruct
  public void initOrdenesData() {
    try {
      InputStream inputStream = resourceLoader.getResource("classpath:data/ordenes-data.json").getInputStream();
      List<OrdenRequestDTO> ordenesDTO = objectMapper.readValue(inputStream,
          new TypeReference<List<OrdenRequestDTO>>() {
          });

      ordenesDTO.forEach(dto -> {
        try {
          crearOrden(dto.getPerfilId(), dto);
        } catch (Exception e) {
          System.err.println("Error al crear orden para perfil " + dto.getPerfilId() + ": " + e.getMessage());
        }
      });

      System.out.println("Datos iniciales de Órdenes cargados desde JSON: " + ordenes.size() + " órdenes.");
    } catch (Exception e) {
      System.err.println("Error al cargar datos iniciales de órdenes desde JSON: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public OrdenResponseDTO crearOrden(Long perfilId, OrdenRequestDTO ordenDTO) {
    // Verificar que el perfil existe
    Perfil perfil = perfilService.obtenerPerfilPorId(perfilId)
        .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado con ID: " + perfilId));

    // Crear la orden
    Orden orden = ordenMapper.toEntity(ordenDTO);
    orden.setId(ordenIdCounter.incrementAndGet());
    orden.setPerfil(perfil);
    orden.setFechaCreacion(LocalDateTime.now());
    orden.setEstadoOrden(ESTADO_PENDIENTE);
    orden.setTotalOrden(0); // Se calculará al agregar items

    ordenes.put(orden.getId(), orden);
    return ordenMapper.toResponseDTO(orden);
  }

  @Override
  public OrdenResponseDTO crearOrdenDesdeCarrito(Long perfilId) {
    // Verificar que el perfil existe
    Perfil perfil = perfilService.obtenerPerfilPorId(perfilId)
        .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado con ID: " + perfilId));

    // Obtener el carrito del usuario
    Carrito carrito = carritoService.obtenerEntidadCarritoPorPerfil(perfilId)
        .orElseThrow(
            () -> new IllegalArgumentException("Carrito no encontrado para el perfil: " + perfil.getNombreVisible()));

    if (carrito.getItems().isEmpty()) {
      throw new IllegalStateException("No se puede crear una orden desde un carrito vacío");
    }

    // Crear la orden
    Orden orden = new Orden();
    orden.setId(ordenIdCounter.incrementAndGet());
    orden.setPerfil(perfil);
    orden.setCarrito(carrito);
    orden.setFechaCreacion(LocalDateTime.now());
    orden.setEstadoOrden(ESTADO_PENDIENTE);
    orden.setTotalOrden(0); // Se calculará al agregar items

    // Convertir items del carrito a items de orden
    int totalOrden = 0;
    for (ItemCarrito itemCarrito : carrito.getItems()) {
      ItemOrden itemOrden = new ItemOrden();
      itemOrden.setId(itemOrdenIdCounter.incrementAndGet());
      itemOrden.setContenido(itemCarrito.getContenido());
      itemOrden.setCantidad(itemCarrito.getCantidad());
      itemOrden.setPrecioAlComprar(itemCarrito.getContenido().getPrecio());

      // Aplicar descuentos si existen
      Integer descuento = 0;
      itemOrden.setDescuentoAplicado(descuento);

      orden.addItem(itemOrden);

      // Actualizar total de la orden
      totalOrden += (itemOrden.getPrecioAlComprar() - itemOrden.getDescuentoAplicado()) * itemOrden.getCantidad();
    }

    orden.setTotalOrden(totalOrden);
    ordenes.put(orden.getId(), orden);

    // Vaciar el carrito después de crear la orden
    carritoService.vaciarCarritoPorPerfil(perfilId);

    return ordenMapper.toResponseDTO(orden);
  }

  @Override
  public Optional<OrdenResponseDTO> obtenerOrdenPorId(Long id) {
    return Optional.ofNullable(ordenes.get(id)).map(ordenMapper::toResponseDTO);
  }

  @Override
  public List<OrdenResponseDTO> obtenerOrdenesPorPerfil(Long perfilId) {
    return ordenes.values().stream()
        .filter(o -> o.getPerfil().getId().equals(perfilId))
        .map(ordenMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<OrdenResponseDTO> obtenerTodasLasOrdenes() {
    return ordenes.values().stream()
        .map(ordenMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<OrdenResponseDTO> actualizarOrden(Long id, OrdenRequestDTO ordenDTO) {
    return obtenerEntidadOrdenPorId(id)
        .map(orden -> {
          return ordenMapper.toResponseDTO(orden);
        });
  }

  @Override
  public boolean eliminarOrden(Long id) {
    return ordenes.remove(id) != null;
  }

  @Override
  public Optional<Orden> obtenerEntidadOrdenPorId(Long id) {
    return Optional.ofNullable(ordenes.get(id));
  }

  @Override
  public List<OrdenResponseDTO> obtenerOrdenesPorEstado(String estado) {
    return ordenes.values().stream()
        .filter(o -> o.getEstadoOrden().equals(estado))
        .map(ordenMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public OrdenResponseDTO procesarOrden(Long id) {
    Orden orden = obtenerEntidadOrdenPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + id));

    if (!ESTADO_PENDIENTE.equals(orden.getEstadoOrden())) {
      throw new IllegalStateException("No se puede procesar una orden que no está en estado pendiente");
    }

    orden.setEstadoOrden(ESTADO_PROCESANDO);
    return ordenMapper.toResponseDTO(orden);
  }

  @Override
  public OrdenResponseDTO completarOrden(Long id) {
    Orden orden = obtenerEntidadOrdenPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + id));

    if (!ESTADO_PROCESANDO.equals(orden.getEstadoOrden())) {
      throw new IllegalStateException("No se puede completar una orden que no está en proceso");
    }

    orden.setEstadoOrden(ESTADO_COMPLETADA);
    return ordenMapper.toResponseDTO(orden);
  }

  @Override
  public OrdenResponseDTO cancelarOrden(Long id, String motivo) {
    Orden orden = obtenerEntidadOrdenPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + id));

    if (ESTADO_COMPLETADA.equals(orden.getEstadoOrden())) {
      throw new IllegalStateException("No se puede cancelar una orden ya completada");
    }

    orden.setEstadoOrden(ESTADO_CANCELADA);
    // Guardar el motivo o en un registro de eventos

    return ordenMapper.toResponseDTO(orden);
  }

  @Override
  public List<ItemOrdenResponseDTO> obtenerItemsDeOrden(Long ordenId) {
    return obtenerEntidadOrdenPorId(ordenId)
        .map(orden -> orden.getItems().stream()
            .map(itemOrdenMapper::toResponseDTO)
            .collect(Collectors.toList()))
        .orElse(new ArrayList<>());
  }

  @Override
  public List<OrdenResponseDTO> obtenerOrdenesPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay();

    return ordenes.values().stream()
        .filter(o -> !o.getFechaCreacion().isBefore(inicio) && o.getFechaCreacion().isBefore(fin))
        .map(ordenMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public double calcularTotalOrdenes(List<Long> ordenIds) {
    return ordenIds.stream()
        .map(this::obtenerEntidadOrdenPorId)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .mapToDouble(orden -> orden.getTotalOrden() / 100.0) // Convertir centavos a unidades
        .sum();
  }

  @Override
  public double calcularTotalOrdenesPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay();

    return ordenes.values().stream()
        .filter(o -> !o.getFechaCreacion().isBefore(inicio) && o.getFechaCreacion().isBefore(fin))
        .mapToDouble(orden -> orden.getTotalOrden() / 100.0) // Convertir centavos a unidades
        .sum();
  }

  @Override
  public boolean generarFactura(Long ordenId) {
    Orden orden = obtenerEntidadOrdenPorId(ordenId)
        .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + ordenId));

    if (!ESTADO_COMPLETADA.equals(orden.getEstadoOrden())) {
      return false; // Solo se puede generar factura para órdenes completadas
    }

    if (orden.getFactura() != null) {
      return true; // Ya tiene factura
    }

    try {
      Factura factura = facturaService.generarFacturaDesdeOrden(orden)
          .orElseThrow(() -> new IllegalStateException("Error al generar factura para la orden"));

      orden.setFactura(factura);
      return true;
    } catch (Exception e) {
      System.err.println("Error al generar factura: " + e.getMessage());
      return false;
    }
  }

  // Método para agregar un ítem a una orden
  public ItemOrdenResponseDTO agregarItemAOrden(ItemOrdenRequestDTO itemDTO) {
    Orden orden = obtenerEntidadOrdenPorId(itemDTO.getOrdenId())
        .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + itemDTO.getOrdenId()));

    // Verificar que la orden está en un estado que permite agregar ítems
    if (!ESTADO_PENDIENTE.equals(orden.getEstadoOrden())) {
      throw new IllegalStateException("No se pueden agregar ítems a una orden que no está en estado pendiente");
    }

    // Verificar que el contenido existe
    Contenido contenido = contenidoService.obtenerContenidoPorId(itemDTO.getContenidoId())
        .map(dto -> {
          
          return new ItemOrdenMapper.ContenidoProxy(dto.getId());
        })
        .orElseThrow(() -> new IllegalArgumentException("Contenido no encontrado con ID: " + itemDTO.getContenidoId()));

    // Crear el ítem de orden
    ItemOrden itemOrden = itemOrdenMapper.toEntity(itemDTO);
    itemOrden.setId(itemOrdenIdCounter.incrementAndGet());
    itemOrden.setContenido(contenido);

    // Agregar a la orden
    orden.addItem(itemOrden);

    // Recalcular total de la orden
    recalcularTotalOrden(orden);

    return itemOrdenMapper.toResponseDTO(itemOrden);
  }

  // Método para eliminar un ítem de una orden
  public boolean eliminarItemDeOrden(Long ordenId, Long itemId) {
    Orden orden = obtenerEntidadOrdenPorId(ordenId)
        .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + ordenId));

    // Verificar que la orden está en un estado que permite modificar ítems
    if (!ESTADO_PENDIENTE.equals(orden.getEstadoOrden())) {
      throw new IllegalStateException("No se pueden eliminar ítems de una orden que no está en estado pendiente");
    }

    boolean eliminado = orden.getItems().removeIf(item -> item.getId().equals(itemId));

    if (eliminado) {
      // Recalcular total de la orden
      recalcularTotalOrden(orden);
    }

    return eliminado;
  }

  // Método para actualizar la cantidad de un ítem
  public Optional<ItemOrdenResponseDTO> actualizarCantidadItem(Long ordenId, Long itemId, int nuevaCantidad) {
    if (nuevaCantidad <= 0) {
      throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
    }

    Orden orden = obtenerEntidadOrdenPorId(ordenId)
        .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + ordenId));

    // Verificar que la orden está en un estado que permite modificar ítems
    if (!ESTADO_PENDIENTE.equals(orden.getEstadoOrden())) {
      throw new IllegalStateException("No se pueden modificar ítems de una orden que no está en estado pendiente");
    }

    Optional<ItemOrden> itemOpt = orden.getItems().stream()
        .filter(item -> item.getId().equals(itemId))
        .findFirst();

    if (itemOpt.isPresent()) {
      ItemOrden item = itemOpt.get();
      item.setCantidad(nuevaCantidad);

      // Recalcular total de la orden
      recalcularTotalOrden(orden);

      return Optional.of(itemOrdenMapper.toResponseDTO(item));
    }

    return Optional.empty();
  }

  // Método auxiliar para recalcular el total de una orden
  private void recalcularTotalOrden(Orden orden) {
    int total = 0;

    for (ItemOrden item : orden.getItems()) {
      int precioItem = item.getPrecioAlComprar() != null ? item.getPrecioAlComprar() : 0;
      int descuentoItem = item.getDescuentoAplicado() != null ? item.getDescuentoAplicado() : 0;
      int cantidad = item.getCantidad() != null ? item.getCantidad() : 1;

      total += (precioItem - descuentoItem) * cantidad;
    }

    orden.setTotalOrden(total);
  }
}