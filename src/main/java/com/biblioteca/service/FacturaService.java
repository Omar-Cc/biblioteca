package com.biblioteca.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.biblioteca.dto.comercial.FacturaRequestDTO;
import com.biblioteca.dto.comercial.FacturaResponseDTO;
import com.biblioteca.models.comercial.Factura;
import com.biblioteca.models.comercial.Orden;

public interface FacturaService {
  // Crear factura a partir de un carrito
  FacturaResponseDTO crearFactura(String username, FacturaRequestDTO facturaDTO);

  // Crear factura desde una Orden de compra
  Optional<Factura> generarFacturaDesdeOrden(Orden orden);

  // Obtener una factura específica
  Optional<FacturaResponseDTO> obtenerFacturaPorId(Long id);

  // Obtener facturas de un perfil
  List<FacturaResponseDTO> obtenerFacturasPorPerfil(Long perfilId);

  // Obtener todas las facturas (admin)
  List<FacturaResponseDTO> obtenerTodasLasFacturas();

  // Buscar facturas por rango de fechas
  List<FacturaResponseDTO> buscarFacturasPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);

  // Anular una factura
  boolean anularFactura(Long id, String motivo);

  // Para uso interno principalmente
  Optional<Factura> obtenerEntidadFacturaPorId(Long id);

  // Generar reporte de ventas
  Map<String, Object> generarReporteVentas(LocalDate fechaInicio, LocalDate fechaFin);

  // Obtener estadísticas de ventas para dashboard
  Map<String, Object> obtenerEstadisticasVentas();
}