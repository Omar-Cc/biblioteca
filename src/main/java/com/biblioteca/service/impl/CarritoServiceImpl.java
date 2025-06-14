package com.biblioteca.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.comercial.CarritoResponseDTO;
import com.biblioteca.dto.comercial.ItemCarritoRequestDTO;
import com.biblioteca.dto.comercial.ItemCarritoResponseDTO;
import com.biblioteca.exceptions.ContenidoNoDisponibleException;
import com.biblioteca.exceptions.RecursoNoEncontradoException;
import com.biblioteca.mapper.comercial.CarritoMapper;
import com.biblioteca.mapper.comercial.ItemCarritoMapper;
import com.biblioteca.models.acceso.Perfil;
import com.biblioteca.models.comercial.Carrito;
import com.biblioteca.models.comercial.EstadoCarrito;
import com.biblioteca.models.comercial.ItemCarrito;
import com.biblioteca.models.contenido.Contenido;
import com.biblioteca.models.contenido.ContenidoFisico;
import com.biblioteca.repositories.comercial.CarritoRepository;
import com.biblioteca.repositories.comercial.ItemCarritoRepository;
import com.biblioteca.repositories.contenido.ContenidoRepository;
import com.biblioteca.service.CarritoService;
import com.biblioteca.service.PerfilService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarritoServiceImpl implements CarritoService {

  private final CarritoRepository carritoRepository;
  private final ItemCarritoRepository itemCarritoRepository;
  private final PerfilService perfilService;
  private final ContenidoRepository contenidoRepository;
  private final CarritoMapper carritoMapper;
  private final ItemCarritoMapper itemCarritoMapper;
  
  // ✅ CONSTANTES CONFIGURABLES
  @Value("${carrito.max-items-per-carrito:50}")
  private int maxItemsPorCarrito;
  
  @Value("${carrito.max-cantidad-por-item:10}")
  private int maxCantidadPorItem;
  
  @Value("${carrito.dias-expiracion:30}")
  private int diasExpiracion;

  @Override
  @Transactional(readOnly = true)
  public Optional<CarritoResponseDTO> obtenerCarritoPorPerfil(Long perfilId) {
    return carritoRepository.findByPerfil_Id(perfilId)
        .map(carritoMapper::toResponseDTO);
  }

  @Override
  @Transactional
  @Retryable(value = {OptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
  public CarritoResponseDTO agregarItemAlCarritoPorPerfil(Long perfilId, ItemCarritoRequestDTO itemDTO) {
    log.info("Agregando item al carrito - Perfil: {}, Contenido: {}, Cantidad: {}", 
        perfilId, itemDTO.getContenidoId(), itemDTO.getCantidad());
    
    Carrito carrito = obtenerOCrearCarritoPorPerfilId(perfilId);
    Contenido contenido = validarYObtenerContenido(itemDTO.getContenidoId());
    
    // Validar cantidad solicitada
    validarCantidadSolicitada(contenido, itemDTO.getCantidad());
    
    // Buscar item existente de manera más eficiente
    Optional<ItemCarrito> itemExistenteOpt = itemCarritoRepository
        .findByCarritoIdAndContenidoId(carrito.getId(), contenido.getId());

    if (itemExistenteOpt.isPresent()) {
      actualizarItemExistente(itemExistenteOpt.get(), itemDTO.getCantidad(), contenido);
    } else {
      crearNuevoItem(carrito, contenido, itemDTO);
    }

    Carrito carritoGuardado = carritoRepository.save(carrito);
    log.info("Item agregado exitosamente al carrito {}", carrito.getId());
    return carritoMapper.toResponseDTO(carritoGuardado);
  }

  @Override
  @Transactional
  public Optional<ItemCarritoResponseDTO> actualizarCantidadItemPorPerfil(Long perfilId, Long itemCarritoId, int cantidad) {
    if (cantidad <= 0) {
      throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
    }
    return carritoRepository.findByPerfil_Id(perfilId)
        .flatMap(carrito -> carrito.getItems().stream()
            .filter(item -> item.getId().equals(itemCarritoId))
            .findFirst()
            .map(item -> {
              item.setCantidad(cantidad);
              item.setFechaUltimaModificacion(LocalDateTime.now());
              ItemCarrito itemGuardado = itemCarritoRepository.save(item);
              return itemCarritoMapper.toResponseDTO(itemGuardado);
            }));
  }

  @Override
  @Transactional
  public boolean eliminarItemDelCarritoPorPerfil(Long perfilId, Long itemCarritoId) {
    Optional<Carrito> carritoOpt = carritoRepository.findByPerfil_Id(perfilId);
    if (carritoOpt.isPresent()) {
      Carrito carrito = carritoOpt.get();
      Optional<ItemCarrito> itemOpt = carrito.getItems().stream()
          .filter(item -> item.getId() != null && item.getId().equals(itemCarritoId))
          .findFirst();

      if (itemOpt.isPresent()) {
        carrito.removeItem(itemOpt.get());
        carritoRepository.save(carrito);
        return true;
      }
    }
    return false;
  }

  @Override
  @Transactional
  public boolean vaciarCarritoPorPerfil(Long perfilId) {
    Optional<Carrito> carritoOpt = carritoRepository.findByPerfil_Id(perfilId);
    if (carritoOpt.isPresent()) {
      Carrito carrito = carritoOpt.get();
      if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
        return false;
      }
      carrito.getItems().clear();
      carrito.setFechaUltimaModificacion(LocalDateTime.now());
      carritoRepository.save(carrito);
      return true;
    }
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Carrito> obtenerEntidadCarritoPorPerfil(Long perfilId) {
    return carritoRepository.findByPerfil_Id(perfilId);
  }

  @Override
  @Transactional(readOnly = true)
  public int obtenerCantidadTotalItemsPorPerfil(Long perfilId) {
    Optional<Carrito> carritoOpt = carritoRepository.findByPerfil_Id(perfilId);
    if (carritoOpt.isEmpty()) {
      return 0;
    }
    
    return carritoOpt.get().getItems().stream()
        .mapToInt(ItemCarrito::getCantidad)
        .sum();
  }

  @Override
  @Transactional(readOnly = true)
  public Integer calcularSubtotalPorPerfil(Long perfilId) {
    Optional<Carrito> carritoOpt = carritoRepository.findByPerfil_Id(perfilId);
    if (carritoOpt.isEmpty()) {
      return 0;
    }
    
    return carritoOpt.get().getItems().stream()
        .mapToInt(item -> item.getPrecio() * item.getCantidad())
        .sum();
  }

  @Override
  @Transactional
  public int limpiarCarritosAbandonados(int diasInactividad) {
    log.info("Iniciando limpieza de carritos abandonados con {} días de inactividad", diasInactividad);
    
    LocalDateTime fechaLimite = LocalDateTime.now().minusDays(diasInactividad);
    List<Carrito> carritosAbandonados = carritoRepository.findCarritosAbandonados(fechaLimite);
    
    int carritoEliminados = 0;
    for (Carrito carrito : carritosAbandonados) {
      carrito.setEstado(EstadoCarrito.ABANDONADO);
      carritoRepository.save(carrito);
      carritoEliminados++;
    }
    
    log.info("Limpieza completada: {} carritos marcados como abandonados", carritoEliminados);
    return carritoEliminados;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean validarLimiteCantidadItem(Long contenidoId, int cantidadSolicitada) {
    log.debug("Validando límite de cantidad para contenido: {}, cantidad: {}", contenidoId, cantidadSolicitada);
    
    // Validar que la cantidad sea positiva
    if (cantidadSolicitada <= 0) {
      return false;
    }
    
    // Validar límite máximo por item (configurable)
    if (cantidadSolicitada > maxCantidadPorItem) {
      log.warn("Cantidad solicitada {} excede el máximo permitido {}", cantidadSolicitada, maxCantidadPorItem);
      return false;
    }
    
    // Verificar stock disponible para contenido físico
    Optional<Contenido> contenidoOpt = contenidoRepository.findById(contenidoId);
    if (contenidoOpt.isEmpty()) {
      log.warn("Contenido no encontrado: {}", contenidoId);
      return false;
    }
    
    Contenido contenido = contenidoOpt.get();
    if (contenido instanceof ContenidoFisico) {
      ContenidoFisico contenidoFisico = (ContenidoFisico) contenido;
      return contenidoFisico.getStockDisponible() >= cantidadSolicitada;
    }
    
    // Para contenido digital, asumimos disponibilidad ilimitada
    return true;
  }

  // ✅ MÉTODOS AUXILIARES PRIVADOS
  
  private Carrito obtenerOCrearCarritoPorPerfilId(Long perfilId) {
    return carritoRepository.findByPerfil_Id(perfilId)
        .orElseGet(() -> crearNuevoCarrito(perfilId));
  }
  
  private Carrito crearNuevoCarrito(Long perfilId) {
    Perfil perfil = perfilService.obtenerEntidadPerfilPorId(perfilId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Perfil no encontrado: " + perfilId));
    
    return Carrito.builder()
        .perfil(perfil)
        .estado(EstadoCarrito.ACTIVO)
        .fechaCreacion(LocalDateTime.now())
        .fechaUltimaModificacion(LocalDateTime.now())
        .limiteItems(maxItemsPorCarrito)
        .fechaExpiracion(LocalDateTime.now().plusDays(diasExpiracion))
        .build();
  }
  
  private Contenido validarYObtenerContenido(Long contenidoId) {
    return contenidoRepository.findById(contenidoId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Contenido no encontrado: " + contenidoId));
  }
  
  private void validarCantidadSolicitada(Contenido contenido, int cantidad) {
    if (cantidad <= 0) {
      throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
    }
    
    if (cantidad > maxCantidadPorItem) {
      throw new IllegalArgumentException("La cantidad excede el máximo permitido por item: " + maxCantidadPorItem);
    }
    
    if (contenido instanceof ContenidoFisico) {
      ContenidoFisico contenidoFisico = (ContenidoFisico) contenido;
      if (contenidoFisico.getStockDisponible() < cantidad) {
        throw new ContenidoNoDisponibleException("Stock insuficiente para el contenido: " + contenido.getId());
      }
    }
  }
  
  private void actualizarItemExistente(ItemCarrito item, int cantidadAdicional, Contenido contenido) {
    int nuevaCantidad = item.getCantidad() + cantidadAdicional;
    validarCantidadSolicitada(contenido, nuevaCantidad);
    
    item.setCantidad(nuevaCantidad);
    item.setFechaUltimaModificacion(LocalDateTime.now());
  }
  
  private void crearNuevoItem(Carrito carrito, Contenido contenido, ItemCarritoRequestDTO itemDTO) {
    ItemCarrito nuevoItem = ItemCarrito.builder()
        .carrito(carrito)
        .contenido(contenido)
        .cantidad(itemDTO.getCantidad())
        .precio(calcularPrecioItem(contenido))
        .descuento(0)
        .fechaAgregado(LocalDateTime.now())
        .fechaUltimaModificacion(LocalDateTime.now())
        .cantidadMaxima(maxCantidadPorItem)
        .precioOriginal(calcularPrecioItem(contenido))
        .build();
    
    carrito.addItem(nuevoItem);
  }    private Integer calcularPrecioItem(Contenido contenido) {
        // Usar el precio real del contenido
        return contenido.getPrecio() != null ? contenido.getPrecio() : 0;
    }

  // ============ MÉTODOS FALTANTES DE LA INTERFAZ ============

  @Override
  @Transactional
  public boolean aplicarCuponDescuentoPorPerfil(Long perfilId, String codigoCupon) {
    log.info("Aplicando cupón {} al carrito del perfil {}", codigoCupon, perfilId);
    
    Optional<Carrito> carritoOpt = carritoRepository.findByPerfil_Id(perfilId);
    if (carritoOpt.isEmpty()) {
      log.warn("No se encontró carrito para el perfil {}", perfilId);
      return false;
    }
    
    // Lógica de validación y aplicación de cupón
    // Por ahora implementación básica
    Carrito carrito = carritoOpt.get();
    
    // Ejemplo: cupón DESCUENTO10 aplica 10% de descuento
    if ("DESCUENTO10".equals(codigoCupon)) {
      int descuentoTotal = carrito.getItems().stream()
          .mapToInt(item -> (int) (item.getPrecio() * item.getCantidad() * 0.10))
          .sum();
      
      // Aplicar descuento proporcionalmente a cada item
      carrito.getItems().forEach(item -> {
        int descuentoItem = (int) (item.getPrecio() * 0.10);
        item.setDescuento(descuentoItem);
        item.setMotivoDescuento("Cupón: " + codigoCupon);
      });
      
      carritoRepository.save(carrito);
      log.info("Cupón {} aplicado exitosamente. Descuento total: {}", codigoCupon, descuentoTotal);
      return true;
    }
    
    log.warn("Cupón {} no válido o no aplicable", codigoCupon);
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean verificarDisponibilidadItemsPorPerfil(Long perfilId) {
    log.debug("Verificando disponibilidad de items para perfil {}", perfilId);
    
    Optional<Carrito> carritoOpt = carritoRepository.findByPerfil_Id(perfilId);
    if (carritoOpt.isEmpty()) {
      return true; // Carrito vacío, no hay problemas de disponibilidad
    }
    
    Carrito carrito = carritoOpt.get();
    for (ItemCarrito item : carrito.getItems()) {
      if (item.getContenido() instanceof ContenidoFisico) {
        ContenidoFisico contenidoFisico = (ContenidoFisico) item.getContenido();
        if (contenidoFisico.getStockDisponible() < item.getCantidad()) {
          log.warn("Item {} no tiene suficiente stock. Disponible: {}, Solicitado: {}", 
              item.getContenido().getId(), contenidoFisico.getStockDisponible(), item.getCantidad());
          return false;
        }
      }
    }
    
    return true;
  }

  @Override
  @Transactional
  public CarritoResponseDTO fusionarCarritos(Long carritoDestinoId, Long carritoOrigenId) {
    log.info("Fusionando carrito origen {} con destino {}", carritoOrigenId, carritoDestinoId);
    
    Carrito carritoDestino = carritoRepository.findById(carritoDestinoId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Carrito destino no encontrado: " + carritoDestinoId));
    
    Carrito carritoOrigen = carritoRepository.findById(carritoOrigenId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Carrito origen no encontrado: " + carritoOrigenId));
    
    // Fusionar items del carrito origen al destino
    for (ItemCarrito itemOrigen : carritoOrigen.getItems()) {
      // Buscar si ya existe un item con el mismo contenido en el destino
      Optional<ItemCarrito> itemDestinoOpt = carritoDestino.getItems().stream()
          .filter(item -> item.getContenido().getId().equals(itemOrigen.getContenido().getId()))
          .findFirst();
      
      if (itemDestinoOpt.isPresent()) {
        // Sumar cantidades
        ItemCarrito itemDestino = itemDestinoOpt.get();
        int nuevaCantidad = itemDestino.getCantidad() + itemOrigen.getCantidad();
        validarCantidadSolicitada(itemDestino.getContenido(), nuevaCantidad);
        itemDestino.setCantidad(nuevaCantidad);
        itemDestino.setFechaUltimaModificacion(LocalDateTime.now());
      } else {
        // Crear nuevo item en destino
        ItemCarrito nuevoItem = ItemCarrito.builder()
            .carrito(carritoDestino)
            .contenido(itemOrigen.getContenido())
            .cantidad(itemOrigen.getCantidad())
            .precio(itemOrigen.getPrecio())
            .descuento(itemOrigen.getDescuento())
            .fechaAgregado(LocalDateTime.now())
            .fechaUltimaModificacion(LocalDateTime.now())
            .cantidadMaxima(itemOrigen.getCantidadMaxima())
            .precioOriginal(itemOrigen.getPrecioOriginal())
            .motivoDescuento(itemOrigen.getMotivoDescuento())
            .build();
        
        carritoDestino.addItem(nuevoItem);
      }
    }
    
    // Eliminar carrito origen después de la fusión
    carritoRepository.delete(carritoOrigen);
    
    Carrito carritoFusionado = carritoRepository.save(carritoDestino);
    log.info("Carritos fusionados exitosamente. Total items en carrito destino: {}", 
        carritoFusionado.getItems().size());
    
    return carritoMapper.toResponseDTO(carritoFusionado);
  }

  @Override
  @Transactional
  public boolean transferirCarritoEntrePerfiles(Long perfilOrigenId, Long perfilDestinoId) {
    log.info("Transfiriendo carrito de perfil {} a perfil {}", perfilOrigenId, perfilDestinoId);
    
    // Verificar que ambos perfiles existen
    Perfil perfilOrigen = perfilService.obtenerEntidadPerfilPorId(perfilOrigenId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Perfil origen no encontrado: " + perfilOrigenId));
    
    Perfil perfilDestino = perfilService.obtenerEntidadPerfilPorId(perfilDestinoId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Perfil destino no encontrado: " + perfilDestinoId));
    
    // Verificar que ambos perfiles pertenecen al mismo usuario
    if (!perfilOrigen.getUsuario().getId().equals(perfilDestino.getUsuario().getId())) {
      throw new IllegalArgumentException("Los perfiles deben pertenecer al mismo usuario");
    }
    
    Optional<Carrito> carritoOrigenOpt = carritoRepository.findByPerfil_Id(perfilOrigenId);
    if (carritoOrigenOpt.isEmpty()) {
      log.warn("No hay carrito para transferir del perfil {}", perfilOrigenId);
      return false;
    }
    
    Carrito carritoOrigen = carritoOrigenOpt.get();
    
    // Si el perfil destino ya tiene carrito, fusionar
    Optional<Carrito> carritoDestinoOpt = carritoRepository.findByPerfil_Id(perfilDestinoId);
    if (carritoDestinoOpt.isPresent()) {
      fusionarCarritos(carritoDestinoOpt.get().getId(), carritoOrigen.getId());
    } else {
      // Simplemente cambiar el perfil del carrito
      carritoOrigen.setPerfil(perfilDestino);
      carritoOrigen.setFechaUltimaModificacion(LocalDateTime.now());
      carritoRepository.save(carritoOrigen);
    }
    
    log.info("Carrito transferido exitosamente de perfil {} a perfil {}", perfilOrigenId, perfilDestinoId);
    return true;
  }

  @Override
  @Transactional
  public boolean guardarCarritoParaCompra(Long perfilId, String nombreGuardado) {
    log.info("Guardando carrito del perfil {} con nombre '{}'", perfilId, nombreGuardado);
    
    Optional<Carrito> carritoOpt = carritoRepository.findByPerfil_Id(perfilId);
    if (carritoOpt.isEmpty()) {
      log.warn("No se encontró carrito para el perfil {}", perfilId);
      return false;
    }
    
    Carrito carrito = carritoOpt.get();
    if (carrito.getItems().isEmpty()) {
      log.warn("No se puede guardar un carrito vacío");
      return false;
    }
    
    // Por ahora, simplemente marcamos el carrito como guardado cambiando su estado
    // En una implementación completa, se crearía una nueva entidad CarritoGuardado
    carrito.setEstado(EstadoCarrito.CONVERTIDO_A_ORDEN);
    carrito.setUsuarioModificacion(nombreGuardado); // Temporal: usar este campo para el nombre
    carrito.setFechaUltimaModificacion(LocalDateTime.now());
    
    carritoRepository.save(carrito);
    
    log.info("Carrito guardado exitosamente con nombre '{}'", nombreGuardado);
    return true;
  }

  @Override
  @Transactional
  public CarritoResponseDTO restaurarCarritoGuardado(Long perfilId, Long carritoGuardadoId) {
    log.info("Restaurando carrito guardado {} para perfil {}", carritoGuardadoId, perfilId);
    
    // Verificar que el perfil existe
    Perfil perfil = perfilService.obtenerEntidadPerfilPorId(perfilId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Perfil no encontrado: " + perfilId));
    
    // Buscar el carrito guardado
    Carrito carritoGuardado = carritoRepository.findById(carritoGuardadoId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Carrito guardado no encontrado: " + carritoGuardadoId));
    
    // Verificar que el carrito pertenece al mismo usuario
    if (!carritoGuardado.getPerfil().getUsuario().getId().equals(perfil.getUsuario().getId())) {
      throw new IllegalArgumentException("El carrito guardado no pertenece al usuario");
    }
    
    // Crear un nuevo carrito activo basado en el guardado
    Carrito nuevoCarrito = Carrito.builder()
        .perfil(perfil)
        .estado(EstadoCarrito.ACTIVO)
        .fechaCreacion(LocalDateTime.now())
        .fechaUltimaModificacion(LocalDateTime.now())
        .limiteItems(maxItemsPorCarrito)
        .fechaExpiracion(LocalDateTime.now().plusDays(diasExpiracion))
        .build();
    
    // Copiar items del carrito guardado
    for (ItemCarrito itemGuardado : carritoGuardado.getItems()) {
      ItemCarrito nuevoItem = ItemCarrito.builder()
          .carrito(nuevoCarrito)
          .contenido(itemGuardado.getContenido())
          .cantidad(itemGuardado.getCantidad())
          .precio(itemGuardado.getPrecio())
          .descuento(itemGuardado.getDescuento())
          .fechaAgregado(LocalDateTime.now())
          .fechaUltimaModificacion(LocalDateTime.now())
          .cantidadMaxima(itemGuardado.getCantidadMaxima())
          .precioOriginal(itemGuardado.getPrecioOriginal())
          .motivoDescuento(itemGuardado.getMotivoDescuento())
          .build();
      
      nuevoCarrito.addItem(nuevoItem);
    }
    
    // Eliminar carrito actual si existe
    carritoRepository.findByPerfil_Id(perfilId).ifPresent(carritoRepository::delete);
    
    Carrito carritoRestaurado = carritoRepository.save(nuevoCarrito);
    
    log.info("Carrito guardado restaurado exitosamente para perfil {}", perfilId);
    return carritoMapper.toResponseDTO(carritoRestaurado);
  }
}
