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
  // Crear factura a partir de un DTO que referencia una Orden
  FacturaResponseDTO crearFactura(FacturaRequestDTO facturaDTO);

  // Crear factura desde una Orden de compra (usado internamente, por ejemplo, después de un pago exitoso)
  Optional<Factura> generarFacturaDesdeOrden(Orden orden);

  Optional<FacturaResponseDTO> obtenerFacturaPorId(Long id);

  List<FacturaResponseDTO> obtenerFacturasPorPerfil(Long perfilId);

  List<FacturaResponseDTO> obtenerTodasLasFacturas();

  List<FacturaResponseDTO> buscarFacturasPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);

  boolean anularFactura(Long id, String motivo);

  Optional<Factura> obtenerEntidadFacturaPorId(Long id);

  Map<String, Object> generarReporteVentas(LocalDate fechaInicio, LocalDate fechaFin);

  Map<String, Object> obtenerEstadisticasVentas();
}