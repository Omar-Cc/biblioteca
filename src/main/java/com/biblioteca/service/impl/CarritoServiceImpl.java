package com.biblioteca.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.biblioteca.dto.ContenidoResponseDTO;
import com.biblioteca.dto.comercial.CarritoResponseDTO;
import com.biblioteca.dto.comercial.ItemCarritoRequestDTO;
import com.biblioteca.dto.comercial.ItemCarritoResponseDTO;
import com.biblioteca.enums.TipoContenido;
import com.biblioteca.mapper.comercial.CarritoMapper;
import com.biblioteca.mapper.comercial.ItemCarritoMapper;
import com.biblioteca.models.Obra;
import com.biblioteca.models.Perfil;
import com.biblioteca.models.comercial.Carrito;
import com.biblioteca.models.comercial.ItemCarrito;
import com.biblioteca.models.contenido.Comic;
import com.biblioteca.models.contenido.Contenido;
import com.biblioteca.models.contenido.LibroFisico;
import com.biblioteca.models.contenido.Manga;
import com.biblioteca.models.contenido.RevistaPeriodica;
import com.biblioteca.service.CarritoService;
import com.biblioteca.service.ContenidoService;
import com.biblioteca.service.PerfilService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CarritoServiceImpl implements CarritoService {

  private final Map<Long, Carrito> carritosPorPerfil = new ConcurrentHashMap<>();
  private final AtomicLong carritoIdCounter = new AtomicLong(0);
  private final AtomicLong itemCarritoIdCounter = new AtomicLong(0);

  private final CarritoMapper carritoMapper;
  private final ItemCarritoMapper itemCarritoMapper;
  private final PerfilService perfilService;
  private final ContenidoService contenidoService;

  @Override
  public Optional<CarritoResponseDTO> obtenerCarritoPorPerfil(Long perfilId) {
    return Optional.ofNullable(carritosPorPerfil.get(perfilId))
        .map(carrito -> {
          CarritoResponseDTO dto = carritoMapper.toResponseDTO(carrito);
          actualizarCalculosCarrito(carrito, dto);

          return dto;
        });
  }

  @Override
  public CarritoResponseDTO agregarItemAlCarritoPorPerfil(Long perfilId, ItemCarritoRequestDTO itemDTO) {
    Carrito carrito = obtenerOCrearCarritoPorPerfil(perfilId);

    ContenidoResponseDTO contenidoDto = contenidoService.obtenerContenidoPorId(itemDTO.getContenidoId())
        .orElseThrow(() -> new IllegalArgumentException("Contenido no encontrado con ID: " + itemDTO.getContenidoId()));

    System.out.println("\n\n[CarritoService] Contenido DTO recuperado: ID=" + contenidoDto.getId() +
        ", enVenta=" + contenidoDto.isEnVenta() +
        ", activo=" + contenidoDto.isActivo() +
        ", precio=" + contenidoDto.getPrecio() +
        ", tipo=" + contenidoDto.getTipo());

    if (!contenidoDto.isActivo() || !contenidoDto.isEnVenta()) {
      String mensajeError = "El contenido (ID: " + contenidoDto.getId() + ") no está disponible para compra.";
      if (!contenidoDto.isActivo()) {
        mensajeError += " No está activo.";
      }
      if (!contenidoDto.isEnVenta()) {
        mensajeError += " No está marcado para venta.";
      }
      throw new IllegalArgumentException(mensajeError.trim());
    }

    Contenido contenidoParaAsociarAlItem;
    TipoContenido tipoDelContenido = contenidoDto.getTipo();

    // Instanciar la subclase concreta basada en el tipo
    switch (tipoDelContenido) {
      case LIBRO_FISICO:
        contenidoParaAsociarAlItem = new LibroFisico();
        break;
      case COMIC_FISICO:
        contenidoParaAsociarAlItem = new Comic();
        break;
      case REVISTA_FISICA:
        contenidoParaAsociarAlItem = new RevistaPeriodica();
        break;
      case MANGA_FISICO:
        contenidoParaAsociarAlItem = new Manga();
        break;
      default:
        throw new IllegalArgumentException("Tipo de contenido no manejado para instanciación: " + tipoDelContenido);
    }

    contenidoParaAsociarAlItem.setId(contenidoDto.getId());
    contenidoParaAsociarAlItem.setPrecio(contenidoDto.getPrecio());
    contenidoParaAsociarAlItem.setTipo(tipoDelContenido);

    if (contenidoDto.getPortadaUrl() != null) {
      contenidoParaAsociarAlItem.setPortadaUrl(contenidoDto.getPortadaUrl());
    }

    if (contenidoDto.getObra() != null && contenidoDto.getObra().getTitulo() != null) {
      Obra obraEntidad = new Obra();
      
      obraEntidad.setTitulo(contenidoDto.getObra().getTitulo());
      contenidoParaAsociarAlItem.setObra(obraEntidad);
    }
    
    else if (contenidoDto.getObra().getTitulo() != null) {
      Obra obraEntidad = new Obra();
      obraEntidad.setTitulo(contenidoDto.getObra().getTitulo());
      contenidoParaAsociarAlItem.setObra(obraEntidad);
    }

    Optional<ItemCarrito> itemExistente = carrito.getItems().stream()
        .filter(item -> item.getContenido() != null && item.getContenido().getId().equals(contenidoDto.getId()))
        .findFirst();

    if (itemExistente.isPresent()) {
      ItemCarrito item = itemExistente.get();
      item.setCantidad(item.getCantidad() + itemDTO.getCantidad());
      
    } else {
      ItemCarrito nuevoItem = itemCarritoMapper.toEntity(itemDTO);
      nuevoItem.setId(itemCarritoIdCounter.incrementAndGet());
      nuevoItem.setContenido(contenidoParaAsociarAlItem); // Asocia la instancia de la subclase concreta
      nuevoItem.setPrecio(contenidoDto.getPrecio()); // Precio del contenido en el momento de agregarlo
      nuevoItem.setDescuento(0);

      carrito.addItem(nuevoItem);
    }

    CarritoResponseDTO dto = carritoMapper.toResponseDTO(carrito);
    actualizarCalculosCarrito(carrito, dto);
    return dto;
  }

  @Override
  public Optional<ItemCarritoResponseDTO> actualizarCantidadItemPorPerfil(Long perfilId, Long itemId, int cantidad) {
    if (cantidad <= 0) {
      throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
    }

    return obtenerEntidadCarritoPorPerfil(perfilId)
        .flatMap(carrito -> carrito.getItems().stream()
            .filter(item -> item.getId().equals(itemId))
            .findFirst()
            .map(item -> {
              item.setCantidad(cantidad);
              ItemCarritoResponseDTO dto = itemCarritoMapper.toResponseDTO(item);

              // Asegurarse de actualizar los cálculos del item
              Integer precio = item.getPrecio() != null ? item.getPrecio() : 0;
              Integer descuento = item.getDescuento() != null ? item.getDescuento() : 0;

              dto.setSubtotal(precio * cantidad);
              dto.setTotal((precio - descuento) * cantidad);

              return dto;
            }));
  }

  @Override
  public boolean eliminarItemDelCarritoPorPerfil(Long perfilId, Long itemId) {
    return obtenerEntidadCarritoPorPerfil(perfilId)
        .map(carrito -> {
          Optional<ItemCarrito> itemOpt = carrito.getItems().stream()
              .filter(item -> item.getId() != null && item.getId().equals(itemId))
              .findFirst();

          if (itemOpt.isPresent()) {
            carrito.removeItem(itemOpt.get());
            return true;
          }
          return false;
        })
        .orElse(false);
  }

  @Override
  public boolean vaciarCarritoPorPerfil(Long perfilId) {
    return obtenerEntidadCarritoPorPerfil(perfilId)
        .map(carrito -> {
          if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
            return false;
          }

          List<ItemCarrito> itemsToRemove = new ArrayList<>(carrito.getItems());
          itemsToRemove.forEach(carrito::removeItem);
          return true;
        })
        .orElse(false);
  }

  @Override
  public double calcularTotalCarritoPorPerfil(Long perfilId) {
    return obtenerEntidadCarritoPorPerfil(perfilId)
        .map(carrito -> {
          int total = 0;
          for (ItemCarrito item : carrito.getItems()) {
            int precio = item.getPrecio() != null ? item.getPrecio() : 0;
            int descuento = item.getDescuento() != null ? item.getDescuento() : 0;
            int cantidad = item.getCantidad() != null ? item.getCantidad() : 1;

            total += ((precio - descuento) * cantidad);
          }
          return total / 100.0; // Convertir centavos a unidades
        })
        .orElse(0.0);
  }

  @Override
  public Optional<Carrito> obtenerEntidadCarritoPorPerfil(Long perfilId) {
    return Optional.ofNullable(carritosPorPerfil.get(perfilId));
  }

  @Override
  public boolean aplicarCuponDescuentoPorPerfil(Long perfilId, String codigoCupon) {
    if (codigoCupon == null || codigoCupon.isEmpty()) {
      return false;
    }

    return obtenerEntidadCarritoPorPerfil(perfilId)
        .map(carrito -> {
          if ("DESCUENTO10".equals(codigoCupon)) {
            for (ItemCarrito item : carrito.getItems()) {
              int precioItem = item.getPrecio() != null ? item.getPrecio() : 0;
              int descuentoCalculado = (int) (precioItem * 0.10); // 10% de descuento
              item.setDescuento(descuentoCalculado);
            }
            return true;
          }
          return false;
        })
        .orElse(false);
  }

  @Override
  public boolean verificarDisponibilidadItemsPorPerfil(Long perfilId) {
    return obtenerEntidadCarritoPorPerfil(perfilId)
        .map(carrito -> {
          // Verificar cada ítem
          for (ItemCarrito item : carrito.getItems()) {
            Long contenidoId = item.getContenido().getId();

            // Verificar si el contenido existe y está disponible
            boolean disponible = contenidoService.obtenerContenidoPorId(contenidoId)
                .map(dto -> {
                  // Para libros físicos, verificar stock
                  if (dto.getTipo().equals(TipoContenido.COMIC_FISICO) ||
                      dto.getTipo().equals(TipoContenido.LIBRO_FISICO) ||
                      dto.getTipo().equals(TipoContenido.REVISTA_FISICA) ||
                      dto.getTipo().equals(TipoContenido.MANGA_FISICO)) {
                    Integer stock = dto.getStockDisponible();
                    return stock != null && stock >= item.getCantidad();
                  }
                  // Para contenido digital, solo verificar que esté activo
                  return dto.isActivo() && dto.isEnVenta();
                })
                .orElse(false);

            if (!disponible) {
              return false;
            }
          }
          return true;
        })
        .orElse(true); // Si no hay carrito, no hay problemas de disponibilidad
  }

  // Método auxiliar para obtener o crear un carrito para un perfil
  private Carrito obtenerOCrearCarritoPorPerfil(Long perfilId) {
    return carritosPorPerfil.computeIfAbsent(perfilId, key -> {
      Perfil perfil = perfilService.obtenerPerfilPorId(key)
          .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado con ID: " + key));

      Carrito nuevoCarrito = new Carrito();
      nuevoCarrito.setId(carritoIdCounter.incrementAndGet());
      nuevoCarrito.setPerfil(perfil);
      nuevoCarrito.setFechaCreacion(LocalDateTime.now());
      return nuevoCarrito;
    });
  }

  private void actualizarCalculosCarrito(Carrito carrito, CarritoResponseDTO dto) {
    if (carrito.getItems() != null) {
      dto.setCantidadItems(carrito.getItems().size());

      int subtotalCalc = 0;
      int descuentosCalc = 0;

      // Actualizar cálculos para cada item
      if (dto.getItems() != null) {
        for (ItemCarritoResponseDTO itemDto : dto.getItems()) {
          Integer precio = itemDto.getPrecio() != null ? itemDto.getPrecio() : 0;
          Integer descuento = itemDto.getDescuento() != null ? itemDto.getDescuento() : 0;
          Integer cantidad = itemDto.getCantidad() != null ? itemDto.getCantidad() : 1;

          // Actualizar subtotal y total de cada item
          itemDto.setSubtotal(precio * cantidad);
          itemDto.setTotal((precio - descuento) * cantidad);

          // Acumular para el carrito
          subtotalCalc += itemDto.getSubtotal();
          descuentosCalc += (descuento * cantidad);
        }
      }

      // Actualizar totales del carrito
      dto.setSubtotal(subtotalCalc);
      dto.setTotalDescuentos(descuentosCalc);
      dto.setTotal(subtotalCalc - descuentosCalc);
    } else {
      // Si no hay items, los totales deben ser 0
      dto.setCantidadItems(0);
      dto.setSubtotal(0);
      dto.setTotalDescuentos(0);
      dto.setTotal(0);
    }
  }
}