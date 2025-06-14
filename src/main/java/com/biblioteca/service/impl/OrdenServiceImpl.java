package com.biblioteca.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.comercial.ItemOrdenRequestDTO;
import com.biblioteca.dto.comercial.ItemOrdenResponseDTO;
import com.biblioteca.dto.comercial.OrdenRequestDTO;
import com.biblioteca.dto.comercial.OrdenResponseDTO;
import com.biblioteca.exceptions.OperacionNoPermitidaException;
import com.biblioteca.exceptions.RecursoNoEncontradoException;
import com.biblioteca.mapper.comercial.ItemOrdenMapper;
import com.biblioteca.mapper.comercial.OrdenMapper;
import com.biblioteca.models.acceso.Perfil;
import com.biblioteca.models.comercial.Carrito;
import com.biblioteca.models.comercial.Factura;
import com.biblioteca.models.comercial.ItemCarrito;
import com.biblioteca.models.comercial.ItemOrden;
import com.biblioteca.models.comercial.Orden;
import com.biblioteca.models.contenido.Contenido;
import com.biblioteca.repositories.comercial.ItemOrdenRepository;
import com.biblioteca.repositories.comercial.OrdenRepository;
import com.biblioteca.repositories.contenido.ContenidoRepository;
import com.biblioteca.service.CarritoService;
import com.biblioteca.service.ContenidoService;
import com.biblioteca.service.FacturaService;
import com.biblioteca.service.OrdenService;
import com.biblioteca.service.PerfilService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrdenServiceImpl implements OrdenService {

  private final OrdenRepository ordenRepository;
  private final OrdenMapper ordenMapper;
  private final ItemOrdenMapper itemOrdenMapper;
  private final PerfilService perfilService;
  private final CarritoService carritoService;
  private final ContenidoRepository contenidoRepository;
  private final FacturaService facturaService; // Mantener @Lazy

  // Estados posibles de una orden
  public static final String ESTADO_PENDIENTE = "Pendiente";
  public static final String ESTADO_PROCESANDO = "Procesando";
  public static final String ESTADO_COMPLETADA = "Completada";
  public static final String ESTADO_CANCELADA = "Cancelada";
  public static final String ESTADO_FALLIDA = "Fallida";

  public OrdenServiceImpl(
      OrdenRepository ordenRepository,
      ItemOrdenRepository itemOrdenRepository,
      OrdenMapper ordenMapper,
      ItemOrdenMapper itemOrdenMapper,
      PerfilService perfilService,
      CarritoService carritoService,
      ContenidoService contenidoService,
      ContenidoRepository contenidoRepository,
      @Lazy FacturaService facturaService) {
    this.ordenRepository = ordenRepository;
    this.ordenMapper = ordenMapper;
    this.itemOrdenMapper = itemOrdenMapper;
    this.perfilService = perfilService;
    this.carritoService = carritoService;
    this.contenidoRepository = contenidoRepository;
    this.facturaService = facturaService;
  }

  @Override
  @Transactional
  public OrdenResponseDTO crearOrden(Long perfilId, OrdenRequestDTO ordenDTO) {
    Perfil perfil = perfilService.obtenerEntidadPerfilPorId(perfilId) // Asumiendo que PerfilService tiene este método
        .orElseThrow(() -> new RecursoNoEncontradoException("Perfil no encontrado con ID: " + perfilId));

    Orden orden = ordenMapper.toEntity(ordenDTO);
    // El ID de la orden será generado por la BD
    orden.setPerfil(perfil);
    orden.setFechaCreacion(LocalDateTime.now());
    orden.setEstadoOrden(ESTADO_PENDIENTE); // Estado inicial

    // Si OrdenRequestDTO puede llevar ítems, se procesarían aquí.
    // Por ahora, asumimos que los ítems se añaden desde el carrito o
    // posteriormente.
    // Si no hay ítems, el total es 0.
    if (orden.getItems() == null || orden.getItems().isEmpty()) {
      orden.setTotalOrden(0);
    } else {
      // Si los items vienen en el DTO, se deben persistir y calcular el total
      List<ItemOrden> itemsPersistidos = new ArrayList<>();
      for (ItemOrden item : orden.getItems()) {
        item.setOrden(orden); // Asegurar relación bidireccional
        // Obtener y validar contenido si es necesario
        Contenido contenido = contenidoRepository.findById(item.getContenido().getId())
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "Contenido no encontrado para item con ID: " + item.getContenido().getId()));
        item.setContenido(contenido);
        item.setPrecioAlComprar(contenido.getPrecio()); // Tomar precio actual del contenido
        // item.setDescuentoAplicado(...); // Lógica de descuento si aplica
        itemsPersistidos.add(item); // No se guarda aquí si hay cascada desde Orden
      }
      orden.setItems(new HashSet<>(itemsPersistidos));
      recalcularTotalOrden(orden);
    }

    Orden ordenGuardada = ordenRepository.save(orden);
    return ordenMapper.toResponseDTO(ordenGuardada);
  }

  @Override
  @Transactional
  public OrdenResponseDTO crearOrdenDesdeCarrito(Long perfilId) {
    Perfil perfil = perfilService.obtenerEntidadPerfilPorId(perfilId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Perfil no encontrado con ID: " + perfilId));

    Carrito carrito = carritoService.obtenerEntidadCarritoPorPerfil(perfilId)
        .orElseThrow(() -> new OperacionNoPermitidaException(
            "Carrito no encontrado para el perfil: " + perfil.getNombreVisible()));

    if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
      throw new OperacionNoPermitidaException("El carrito está vacío.");
    }

    Orden orden = new Orden();
    // El ID de la orden será generado por la BD
    orden.setPerfil(perfil);
    // orden.setCarrito(carrito); // No es usual almacenar la referencia directa al
    // carrito en la orden persistida
    orden.setFechaCreacion(LocalDateTime.now());
    orden.setEstadoOrden(ESTADO_PENDIENTE);

    List<ItemOrden> itemsDeOrden = new ArrayList<>();
    for (ItemCarrito itemCarrito : carrito.getItems()) {
      if (itemCarrito.getContenido() == null)
        continue; // Saltar si no hay contenido

      ItemOrden itemOrden = new ItemOrden();
      // El ID del itemOrden será generado por la BD
      itemOrden.setContenido(itemCarrito.getContenido());
      itemOrden.setCantidad(itemCarrito.getCantidad());
      itemOrden.setPrecioAlComprar(itemCarrito.getPrecio()); // Precio ya fijado en el carrito
      itemOrden.setDescuentoAplicado(itemCarrito.getDescuento()); // Descuento ya fijado en el carrito
      itemOrden.setOrden(orden); // Establecer la relación bidireccional
      itemsDeOrden.add(itemOrden);
    }
    orden.setItems(new HashSet<>(itemsDeOrden));
    recalcularTotalOrden(orden); // Calcular el total basado en los ítems

    // Guardar la orden (y sus ítems en cascada si está configurado)
    Orden ordenGuardada = ordenRepository.save(orden);

    // Vaciar el carrito después de crear la orden
    carritoService.vaciarCarritoPorPerfil(perfilId);

    return ordenMapper.toResponseDTO(ordenGuardada);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrdenResponseDTO> obtenerOrdenesPorPerfilConFiltros(Long perfilId, String estado,
      LocalDate fechaDesde, LocalDate fechaHasta) {
    // Convertir fechas a LocalDateTime para la consulta
    LocalDateTime fechaDesdeDateTime = fechaDesde != null ? fechaDesde.atStartOfDay() : null;
    LocalDateTime fechaHastaDateTime = fechaHasta != null ? fechaHasta.atTime(23, 59, 59) : null;

    List<Orden> ordenes = ordenRepository.findByPerfilIdWithFilters(perfilId, estado, fechaDesdeDateTime,
        fechaHastaDateTime);

    return ordenes.stream()
        .map(ordenMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrdenResponseDTO> obtenerOrdenesPorPerfil(Long perfilId) {
    // Usar query simple y dejar que el mapper y las anotaciones @Transactional
    // manejen la carga lazy
    List<Orden> ordenes = ordenRepository.findByPerfilIdOrderByFechaDesc(perfilId);

    return ordenes.stream()
        .map(ordenMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<OrdenResponseDTO> obtenerOrdenPorId(Long id) {
    return ordenRepository.findByIdSimple(id)
        .map(ordenMapper::toResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrdenResponseDTO> obtenerTodasLasOrdenes() {
    return ordenRepository.findAll().stream()
        .map(ordenMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Optional<OrdenResponseDTO> actualizarOrden(Long id, OrdenRequestDTO ordenDTO) {
    return ordenRepository.findById(id)
        .map(ordenExistente -> {
          // Actualizar campos simples de la orden.
          // La actualización de ítems es más compleja y se omite aquí
          // a menos que OrdenRequestDTO contenga información de ítems para actualizar.
          // Por ahora, solo actualizamos campos de la orden principal.
          if (ordenDTO.getEstadoOrden() != null) {
            ordenExistente.setEstadoOrden(ordenDTO.getEstadoOrden());
          }
          // ordenExistente.setDireccionEnvio(ordenDTO.getDireccionEnvio()); // Si existe
          // en DTO y entidad
          // ordenExistente.setMetodoPago(ordenDTO.getMetodoPago()); // Si existe en DTO y
          // entidad

          // No se permite cambiar el perfil de una orden existente.
          // Los ítems y el total se manejarían con métodos separados o una lógica más
          // compleja aquí.

          Orden ordenActualizada = ordenRepository.save(ordenExistente);
          return ordenMapper.toResponseDTO(ordenActualizada);
        });
  }

  @Override
  @Transactional
  public boolean eliminarOrden(Long id) {
    if (ordenRepository.existsById(id)) {
      Orden orden = ordenRepository.findById(id).get();
      if (ESTADO_PENDIENTE.equals(orden.getEstadoOrden()) || ESTADO_FALLIDA.equals(orden.getEstadoOrden())) {
        // Antes de eliminar la orden, eliminar sus ItemOrden asociados si no hay
        // cascada de eliminación
        // o si se quiere asegurar. Con CascadeType.ALL y orphanRemoval=true en
        // Orden.items, esto es automático.
        // itemOrdenRepository.deleteByOrdenId(id); // Si es necesario
        ordenRepository.deleteById(id);
        return true;
      } else {
        throw new OperacionNoPermitidaException("Solo se pueden eliminar órdenes en estado PENDIENTE o FALLIDA.");
      }
    }
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Orden> obtenerEntidadOrdenPorId(Long id) {
    return ordenRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrdenResponseDTO> obtenerOrdenesPorEstado(String estado) {
    return ordenRepository.findByEstadoOrden(estado).stream()
        .map(ordenMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  private Orden cambiarEstadoOrden(Long id, String nuevoEstado, String motivoSiCancelada) {
    Orden orden = ordenRepository.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con ID: " + id));

    // Lógica de transición de estados (simplificada)
    // TODO: Implementar una máquina de estados más robusta si es necesario
    if (ESTADO_CANCELADA.equals(nuevoEstado) && (motivoSiCancelada == null || motivoSiCancelada.isBlank())) {
      throw new IllegalArgumentException("Se requiere un motivo para cancelar la orden.");
    }

    orden.setEstadoOrden(nuevoEstado);
    if (ESTADO_CANCELADA.equals(nuevoEstado)) {
      orden.setMotivoCancelacion(motivoSiCancelada); // Asumiendo que Orden tiene este campo
    }
    if (ESTADO_COMPLETADA.equals(nuevoEstado)) {
      orden.setFechaCompletada(LocalDateTime.now()); // Asumiendo que Orden tiene este campo
    }

    return ordenRepository.save(orden);
  }

  @Override
  @Transactional
  public OrdenResponseDTO procesarOrden(Long id) {
    Orden orden = cambiarEstadoOrden(id, ESTADO_PROCESANDO, null);

    // Generar factura al procesar la orden con manejo de errores
    try {
      if (orden.getFactura() == null) {
        Optional<Factura> facturaOpt = facturaService.generarFacturaDesdeOrden(orden);
        if (facturaOpt.isPresent()) {
          // Actualizar la orden con la factura generada
          orden.setFactura(facturaOpt.get());
          orden = ordenRepository.save(orden);
          log.info("Factura generada exitosamente para orden {}", id);
        } else {
          log.warn("No se pudo generar factura para orden {}", id);
        }
      }
    } catch (Exception e) {
      log.error("Error al generar factura para orden {}: {}", id, e.getMessage(), e);
      // NO propagar la excepción para evitar rollback de toda la transacción
      // La orden se procesa exitosamente aunque falle la factura
    }

    return ordenMapper.toResponseDTO(orden);
  }

  @Override
  @Transactional
  public OrdenResponseDTO completarOrden(Long id) {
    Orden orden = cambiarEstadoOrden(id, ESTADO_COMPLETADA, null);
    // Aquí se podría disparar la generación de factura si no se ha hecho ya
    if (orden.getFactura() == null) {
      generarFactura(id);
    }
    return ordenMapper.toResponseDTO(orden);
  }

  @Override
  @Transactional
  public OrdenResponseDTO cancelarOrden(Long id, String motivo) {
    Orden orden = cambiarEstadoOrden(id, ESTADO_CANCELADA, motivo);
    // Lógica adicional: revertir stock, etc.
    return ordenMapper.toResponseDTO(orden);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ItemOrdenResponseDTO> obtenerItemsDeOrden(Long ordenId) {
    Orden orden = ordenRepository.findById(ordenId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con ID: " + ordenId));
    return orden.getItems().stream()
        .map(itemOrdenMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrdenResponseDTO> obtenerOrdenesPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay().minusNanos(1);
    return ordenRepository.findByFechaCreacionBetween(inicio, fin).stream()
        .map(ordenMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public double calcularTotalOrdenes(List<Long> ordenIds) {
    return ordenRepository.findAllById(ordenIds).stream()
        .mapToDouble(orden -> orden.getTotalOrden() / 100.0) // Asumiendo que totalOrden está en centavos
        .sum();
  }

  @Override
  @Transactional(readOnly = true)
  public double calcularTotalOrdenesPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay().minusNanos(1);
    return ordenRepository.findByFechaCreacionBetween(inicio, fin).stream()
        .mapToDouble(orden -> orden.getTotalOrden() / 100.0) // Asumiendo que totalOrden está en centavos
        .sum();
  }

  @Override
  @Transactional
  public boolean generarFactura(Long ordenId) {
    Orden orden = ordenRepository.findById(ordenId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con ID: " + ordenId));

    if (!ESTADO_COMPLETADA.equals(orden.getEstadoOrden()) && !ESTADO_PROCESANDO.equals(orden.getEstadoOrden())) {
      throw new OperacionNoPermitidaException(
          "Solo se puede generar factura para órdenes completadas o en procesamiento.");
    }

    if (orden.getFactura() != null) {
      // Ya existe una factura, no generar otra o decidir política de regeneración.
      return true; // O false si se considera un fallo no poder regenerar.
    }

    Optional<Factura> facturaOpt = facturaService.generarFacturaDesdeOrden(orden);
    if (facturaOpt.isPresent()) {
      // La relación Orden <-> Factura debe ser bidireccional y manejada por JPA.
      // Si FacturaService.generarFacturaDesdeOrden no actualiza la Orden, hacerlo
      // aquí.
      // orden.setFactura(facturaOpt.get()); // Esto debería hacerse dentro de
      // generarFacturaDesdeOrden o por cascada
      // ordenRepository.save(orden);
      return true;
    }
    return false;
  }

  // Método para agregar un ítem a una orden
  @Override
  @Transactional
  public ItemOrdenResponseDTO agregarItemAOrden(Long ordenId, ItemOrdenRequestDTO itemDTO) {
    Orden orden = obtenerEntidadOrdenPorId(ordenId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con ID: " + ordenId));

    // Verificar que la orden está en un estado que permite agregar ítems
    if (!ESTADO_PENDIENTE.equals(orden.getEstadoOrden())) {
      throw new OperacionNoPermitidaException(
          "No se pueden agregar ítems a una orden que no está en estado pendiente. Estado actual: "
              + orden.getEstadoOrden());
    }

    // Verificar que el contenido existe y obtener la entidad Contenido
    Contenido contenido = contenidoRepository.findById(itemDTO.getContenidoId()) // <--- CAMBIO AQUÍ
        .orElseThrow(
            () -> new RecursoNoEncontradoException("Contenido no encontrado con ID: " + itemDTO.getContenidoId()));

    // Crear el ítem de orden
    ItemOrden itemOrden = itemOrdenMapper.toEntity(itemDTO);
    // El ID de itemOrden será generado por la BD al persistir.
    itemOrden.setContenido(contenido);
    itemOrden.setPrecioAlComprar(contenido.getPrecio()); // Tomar precio actual del contenido
    // itemOrden.setDescuentoAplicado(0); // Establecer descuento si es necesario, o
    // tomar de DTO

    itemOrden.setOrden(orden); // Establecer la relación bidireccional

    // Agregar a la colección de la orden
    if (orden.getItems() == null) {
      orden.setItems(new HashSet<>());
    }
    orden.getItems().add(itemOrden);

    // Recalcular total de la orden
    recalcularTotalOrden(orden);

    // Guardar la orden para persistir el nuevo item (si hay cascada) y el total
    // actualizado
    Orden ordenGuardada = ordenRepository.save(orden);

    // Encontrar el item persistido para devolverlo con su ID.
    // Esto es crucial si el ID del itemOrden no se actualiza automáticamente en la
    // instancia 'itemOrden'.
    // Buscamos el ítem que acabamos de añadir basándonos en sus propiedades
    // (contenidoId, cantidad, precio)
    // ya que su ID es generado por la BD.
    final Long contenidoIdFinal = contenido.getId();
    ItemOrden itemPersistido = ordenGuardada.getItems().stream()
        .filter(i -> i.getContenido().getId().equals(contenidoIdFinal) &&
            i.getCantidad() == itemDTO.getCantidad() &&
            i.getPrecioAlComprar().equals(contenido.getPrecio()) &&
            i.getId() != null) // Asegurarse que tenga ID
        .reduce((a, b) -> b) // Si hay múltiples coincidencias (poco probable para un item recién añadido),
                             // tomar la última.
        .orElseThrow(() -> new IllegalStateException(
            "No se pudo encontrar el ítem recién añadido con ID en la orden guardada."));

    return itemOrdenMapper.toResponseDTO(itemPersistido);
  }

  // Método para eliminar un ítem de una orden
  @Override
  @Transactional
  public boolean eliminarItemDeOrden(Long ordenId, Long itemOrdenId) {
    Orden orden = obtenerEntidadOrdenPorId(ordenId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con ID: " + ordenId));

    // Verificar que la orden está en un estado que permite modificar ítems
    if (!ESTADO_PENDIENTE.equals(orden.getEstadoOrden())) {
      throw new OperacionNoPermitidaException(
          "No se pueden eliminar ítems de una orden que no está en estado pendiente. Estado actual: "
              + orden.getEstadoOrden());
    }

    if (orden.getItems() == null) {
      return false; // No hay ítems para eliminar
    }

    Optional<ItemOrden> itemAEliminarOpt = orden.getItems().stream()
        .filter(item -> item.getId().equals(itemOrdenId))
        .findFirst();

    if (itemAEliminarOpt.isPresent()) {
      ItemOrden itemAEliminar = itemAEliminarOpt.get();
      orden.getItems().remove(itemAEliminar);
      // Si la relación Orden -> ItemOrden tiene orphanRemoval=true, JPA se encargará
      // de eliminar el ItemOrden de la BD.
      // Si no, y si ItemOrden es una entidad gestionada independientemente en algunos
      // casos,
      // podrías necesitar: itemOrdenRepository.delete(itemAEliminar);
      // Pero es preferible que la cascada lo maneje.

      recalcularTotalOrden(orden);
      ordenRepository.save(orden); // Guardar la orden para persistir la eliminación del item y el total
                                   // actualizado
      return true;
    }

    return false; // Ítem no encontrado en la orden
  }

  // Método para actualizar la cantidad de un ítem
  @Override
  @Transactional
  public Optional<ItemOrdenResponseDTO> actualizarCantidadItem(Long ordenId, Long itemOrdenId, int nuevaCantidad) {
    if (nuevaCantidad <= 0) {
      // Considera si esto debería ser una OperacionNoPermitidaException o
      // IllegalArgumentException
      throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
    }

    Orden orden = obtenerEntidadOrdenPorId(ordenId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con ID: " + ordenId));

    // Verificar que la orden está en un estado que permite modificar ítems
    if (!ESTADO_PENDIENTE.equals(orden.getEstadoOrden())) {
      throw new OperacionNoPermitidaException(
          "No se pueden modificar ítems de una orden que no está en estado pendiente. Estado actual: "
              + orden.getEstadoOrden());
    }

    if (orden.getItems() == null) {
      return Optional.empty(); // No hay ítems para actualizar
    }

    Optional<ItemOrden> itemOpt = orden.getItems().stream()
        .filter(item -> item.getId().equals(itemOrdenId))
        .findFirst();

    if (itemOpt.isPresent()) {
      ItemOrden item = itemOpt.get();
      item.setCantidad(nuevaCantidad);
      // El precioAlComprar y descuentoAplicado no se cambian aquí, solo la cantidad.

      recalcularTotalOrden(orden);
      ordenRepository.save(orden);

      // Devolver el DTO del ítem actualizado
      // Es importante que el 'item' aquí sea la instancia gestionada por JPA y
      // actualizada.
      return Optional.of(itemOrdenMapper.toResponseDTO(item));
    }

    return Optional.empty(); // Ítem no encontrado en la orden
  }

  // Método auxiliar para recalcular el total de una orden
  private void recalcularTotalOrden(Orden orden) {
    if (orden.getItems() == null) {
      orden.setTotalOrden(0);
      return;
    }
    int total = orden.getItems().stream()
        .mapToInt(item -> (item.getPrecioAlComprar() - item.getDescuentoAplicado()) * item.getCantidad())
        .sum();
    orden.setTotalOrden(total);
  }
}