package com.biblioteca.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.biblioteca.dto.comercial.ItemOrdenRequestDTO;
import com.biblioteca.dto.comercial.ItemOrdenResponseDTO;
import com.biblioteca.dto.comercial.OrdenRequestDTO;
import com.biblioteca.dto.comercial.OrdenResponseDTO;
import com.biblioteca.models.comercial.Orden;

public interface OrdenService {
  // Operaciones CRUD básicas
  OrdenResponseDTO crearOrden(Long perfilId, OrdenRequestDTO ordenDTO);

  OrdenResponseDTO crearOrdenDesdeCarrito(Long perfilId);

  Optional<OrdenResponseDTO> obtenerOrdenPorId(Long id);

  List<OrdenResponseDTO> obtenerOrdenesPorPerfilConFiltros(Long perfilId, String estado,
      LocalDate fechaDesde, LocalDate fechaHasta);

  List<OrdenResponseDTO> obtenerOrdenesPorPerfil(Long perfilId);

  List<OrdenResponseDTO> obtenerTodasLasOrdenes();

  Optional<OrdenResponseDTO> actualizarOrden(Long id, OrdenRequestDTO ordenDTO);

  boolean eliminarOrden(Long id);

  // Para uso interno principalmente
  Optional<Orden> obtenerEntidadOrdenPorId(Long id);

  // Operaciones específicas por estado
  List<OrdenResponseDTO> obtenerOrdenesPorEstado(String estado);

  // Operaciones para cambiar estado
  OrdenResponseDTO procesarOrden(Long id);

  OrdenResponseDTO completarOrden(Long id);

  OrdenResponseDTO cancelarOrden(Long id, String motivo);

  // Operaciones para ítems de orden
  List<ItemOrdenResponseDTO> obtenerItemsDeOrden(Long ordenId);

  ItemOrdenResponseDTO agregarItemAOrden(Long ordenId, ItemOrdenRequestDTO itemDTO);

  boolean eliminarItemDeOrden(Long ordenId, Long itemOrdenId);

  Optional<ItemOrdenResponseDTO> actualizarCantidadItem(Long ordenId, Long itemOrdenId, int nuevaCantidad);

  // Operaciones por fecha
  List<OrdenResponseDTO> obtenerOrdenesPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);

  // Operaciones para cálculos
  double calcularTotalOrdenes(List<Long> ordenIds);

  double calcularTotalOrdenesPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);

  // Generación de factura
  boolean generarFactura(Long ordenId);
}