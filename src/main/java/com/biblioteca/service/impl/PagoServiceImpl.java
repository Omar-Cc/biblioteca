package com.biblioteca.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.comercial.PagoRequestDTO;
import com.biblioteca.dto.comercial.PagoResponseDTO;
import com.biblioteca.enums.EstadoPago;
import com.biblioteca.exceptions.OperacionNoPermitidaException;
import com.biblioteca.exceptions.RecursoNoEncontradoException;
import com.biblioteca.mapper.comercial.PagoMapper;
import com.biblioteca.models.comercial.MetodoPago;
import com.biblioteca.models.comercial.Orden;
import com.biblioteca.models.comercial.Pago;
import com.biblioteca.models.comercial.Suscripcion;
import com.biblioteca.repositories.comercial.PagoRepository;
import com.biblioteca.service.MetodoPagoService;
import com.biblioteca.service.NotificacionPagoService;
import com.biblioteca.service.OrdenService;
import com.biblioteca.service.PagoService;
import com.biblioteca.service.SuscripcionService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PagoServiceImpl implements PagoService {

  private final PagoRepository pagoRepository;
  private final PagoMapper pagoMapper;
  private final OrdenService ordenService;
  private final MetodoPagoService metodoPagoService;
  private final SuscripcionService suscripcionService;
  private final NotificacionPagoService notificacionPagoService;

  // Constructor con @Lazy para romper la dependencia circular
  public PagoServiceImpl(
      PagoRepository pagoRepository,
      PagoMapper pagoMapper,
      OrdenService ordenService,
      MetodoPagoService metodoPagoService,
      SuscripcionService suscripcionService,
      @Lazy NotificacionPagoService notificacionPagoService) {
    this.pagoRepository = pagoRepository;
    this.pagoMapper = pagoMapper;
    this.ordenService = ordenService;
    this.metodoPagoService = metodoPagoService;
    this.suscripcionService = suscripcionService;
    this.notificacionPagoService = notificacionPagoService;
  }

  @Override
  @Transactional
  public PagoResponseDTO registrarPago(PagoRequestDTO pagoDTO) {
    log.info("Registrando nuevo pago para orden ID: {}", pagoDTO.getOrdenId());

    Pago pago = pagoMapper.toEntity(pagoDTO);

    // Obtener y asignar la orden
    if (pagoDTO.getOrdenId() != null) {
      Orden orden = ordenService.obtenerEntidadOrdenPorId(pagoDTO.getOrdenId())
          .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con ID: " + pagoDTO.getOrdenId()));
      pago.setOrden(orden);
    }

    // Obtener y asignar la suscripción SOLO si se proporciona
    if (pagoDTO.getSuscripcionId() != null) {
      // Aquí necesitarías inyectar SuscripcionService y obtener la suscripción
      Suscripcion suscripcion = suscripcionService.obtenerEntidadSuscripcionPorId(pagoDTO.getSuscripcionId())
          .orElse(null);
      pago.setSuscripcion(suscripcion);
    } else {
      pago.setSuscripcion(null); // ← Asegurar que sea null para pagos de órdenes
    }

    // Obtener y asignar el método de pago
    MetodoPago metodoPago = metodoPagoService.obtenerEntidadMetodoPagoPorId(pagoDTO.getMetodoPagoId())
        .orElseThrow(() -> new RecursoNoEncontradoException(
            "Método de pago no encontrado con ID: " + pagoDTO.getMetodoPagoId()));
    pago.setMetodoPago(metodoPago);

    // Establecer el estado inicial
    if (pagoDTO.getEstado() != null && !pagoDTO.getEstado().isEmpty()) {
      try {
        pago.setEstado(EstadoPago.valueOf(pagoDTO.getEstado()));
      } catch (IllegalArgumentException e) {
        log.warn("Estado de pago inválido: {}, usando PENDIENTE por defecto", pagoDTO.getEstado());
        pago.setEstado(EstadoPago.PENDIENTE);
      }
    } else {
      pago.setEstado(EstadoPago.PENDIENTE);
    }

    // Establecer fecha de creación si no está presente
    if (pago.getFechaCreacion() == null) {
      pago.setFechaCreacion(LocalDateTime.now());
    }

    // Guardar el pago
    Pago pagoGuardado = pagoRepository.save(pago);
    log.info("Pago registrado con ID: {}", pagoGuardado.getId());

    return pagoMapper.toResponseDTO(pagoGuardado);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PagoResponseDTO> obtenerPagoPorId(Long id) {
    return pagoRepository.findById(id)
        .map(pagoMapper::toResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PagoResponseDTO> obtenerPagosPorOrden(Long ordenId) {
    return pagoRepository.findByOrdenId(ordenId).stream() // Asume que este método existe en el repositorio
        .map(pagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<PagoResponseDTO> obtenerTodosLosPagos() {
    return pagoRepository.findAll().stream()
        .map(pagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Optional<PagoResponseDTO> actualizarPago(Long id, PagoRequestDTO pagoDTO) {
    return pagoRepository.findById(id)
        .map(pago -> {
          // Actualizar campos básicos
          if (pagoDTO.getMonto() != null) {
            pago.setMonto(pagoDTO.getMonto());
          }
          if (pagoDTO.getEstado() != null) {
            pago.setEstado(EstadoPago.valueOf(pagoDTO.getEstado()));
          }
          if (pagoDTO.getReferenciaExterna() != null) {
            pago.setReferenciaExterna(pagoDTO.getReferenciaExterna());
          }

          // Si el método de pago cambia:
          if (pagoDTO.getMetodoPagoId() != null &&
              (pago.getMetodoPago() == null || !pago.getMetodoPago().getId().equals(pagoDTO.getMetodoPagoId()))) {
            MetodoPago nuevoMetodoPago = metodoPagoService.obtenerEntidadMetodoPagoPorId(pagoDTO.getMetodoPagoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                    "Nuevo método de pago no encontrado con ID: " + pagoDTO.getMetodoPagoId()));
            pago.setMetodoPago(nuevoMetodoPago);
          }

          Pago pagoActualizado = pagoRepository.save(pago);
          return pagoMapper.toResponseDTO(pagoActualizado);
        });
  }

  @Override
  @Transactional
  public boolean eliminarPago(Long id) {
    if (pagoRepository.existsById(id)) {
      // Considerar lógica de negocio: ¿Se pueden eliminar pagos completados?
      // ¿Qué pasa con la orden asociada?
      // Por ahora, simplemente eliminamos el pago.
      pagoRepository.deleteById(id);
      return true;
    }
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Pago> obtenerEntidadPagoPorId(Long id) {
    return pagoRepository.findWithAllRelationsById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PagoResponseDTO> obtenerPagosPorEstado(EstadoPago estado) {
    return pagoRepository.findByEstado(estado).stream()
        .map(pagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<PagoResponseDTO> obtenerPagosPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay().minusNanos(1); // Para incluir todo el día de fechaFin
    return pagoRepository.findByFechaPagoBetween(inicio, fin).stream() // Asume que este método existe
        .map(pagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<PagoResponseDTO> obtenerPagosPorMetodoPago(Long metodoPagoId) {
    return pagoRepository.findByMetodoPagoId(metodoPagoId).stream() // Asume que este método existe
        .map(pagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  private Pago cambiarEstadoPago(Long id, EstadoPago nuevoEstado, String referenciaExternaSiAprobado,
      String motivoSiRechazado) {
    Pago pago = pagoRepository.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado con ID: " + id));

    // Lógica de transición de estados (simplificada)
    // TODO: Implementar una máquina de estados más robusta si es necesario
    if (EstadoPago.EXITOSO.equals(pago.getEstado()) && !EstadoPago.REEMBOLSADO.equals(nuevoEstado)) {
      throw new OperacionNoPermitidaException(
          "El pago ya está completado y no puede cambiar a " + nuevoEstado + " (excepto Reembolsado).");
    }
    if (EstadoPago.FALLIDO.equals(pago.getEstado()) && !EstadoPago.PENDIENTE.equals(nuevoEstado)) {
      throw new OperacionNoPermitidaException(
          "El pago ya está rechazado y solo puede volver a Pendiente para reintentar.");
    }

    pago.setEstado(nuevoEstado);
    if (EstadoPago.EXITOSO.equals(nuevoEstado) && referenciaExternaSiAprobado != null) {
      pago.setReferenciaExterna(referenciaExternaSiAprobado);
      pago.setFechaProcesamiento(LocalDateTime.now());
      // Actualizar estado de la orden si el pago se completa
      if (pago.getOrden() != null) {
        ordenService.completarOrden(pago.getOrden().getId());
      }
    }
    if (EstadoPago.FALLIDO.equals(nuevoEstado) && motivoSiRechazado != null) {
      pago.setMotivoRechazo(motivoSiRechazado);
      pago.setFechaProcesamiento(LocalDateTime.now());
      // Actualizar estado de la orden si el pago es rechazado
      if (pago.getOrden() != null) {
        // ordenService.marcarOrdenComoFallida(pago.getOrden().getId());
      }
    }

    return pagoRepository.save(pago);
  }

  @Override
  @Transactional
  public PagoResponseDTO procesarPago(Long id) {
    Pago pago = cambiarEstadoPago(id, EstadoPago.PENDIENTE, null, null);

    try {
      notificacionPagoService.enviarNotificacionPagoEnProceso(pago);
    } catch (Exception e) {
      log.warn("Error enviando notificación de pago en proceso para pago {}: {}", id, e.getMessage());
    }

    return pagoMapper.toResponseDTO(pago);
  }

  @Override
  @Transactional
  public PagoResponseDTO aprobarPago(Long id, String referenciaPago) {
    Pago pago = cambiarEstadoPago(id, EstadoPago.EXITOSO, referenciaPago, null);

    try {
      notificacionPagoService.enviarNotificacionPagoExitoso(pago);
    } catch (Exception e) {
      log.warn("Error enviando notificación de pago exitoso para pago {}: {}", id, e.getMessage());
    }

    return pagoMapper.toResponseDTO(pago);
  }

  @Override
  @Transactional
  public PagoResponseDTO rechazarPago(Long id, String motivo) {
    Pago pago = cambiarEstadoPago(id, EstadoPago.FALLIDO, null, motivo);
    try {
      notificacionPagoService.enviarNotificacionPagoFallido(pago);
    } catch (Exception e) {
      log.warn("Error enviando notificación de pago fallido para pago {}: {}", id, e.getMessage());
    }
    return pagoMapper.toResponseDTO(pago);
  }

  @Override
  @Transactional
  public PagoResponseDTO verificarEstadoConPasarela(Long id) {
    // Esta lógica dependerá de la integración con la pasarela de pago.
    // Por ahora, simulamos obteniendo el pago y devolviéndolo.
    Pago pago = pagoRepository.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado con ID: " + id));
    // Aquí iría la llamada a la pasarela y la actualización del estado del pago.
    // Ejemplo:
    // String estadoPasarela =
    // pasarelaService.consultarEstado(pago.getReferenciaExterna());
    // if ("APROBADO".equals(estadoPasarela) &&
    // !EstadoPago.EXITOSO.equals(pago.getEstado())) {
    // return aprobarPago(id, pago.getReferenciaExterna());
    // } else if (("RECHAZADO".equals(estadoPasarela) ||
    // "FALLIDO".equals(estadoPasarela)) &&
    // !EstadoPago.FALLIDO.equals(pago.getEstado())) {
    // return rechazarPago(id, "Rechazado por pasarela");
    // }
    return pagoMapper.toResponseDTO(pago);
  }

  @Override
  @Transactional(readOnly = true)
  public double calcularTotalPagosPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay().minusNanos(1);
    List<Pago> pagosEnRango = pagoRepository.findByFechaPagoBetweenAndEstado(inicio, fin, EstadoPago.EXITOSO);
    return pagosEnRango.stream()
        .mapToDouble(p -> p.getMonto() / 100.0) // Asumiendo que monto está en centavos
        .sum();
  }

  @Override
  @Transactional(readOnly = true)
  public double calcularTotalPagosPorMetodoPago(Long metodoPagoId, LocalDate fechaInicio, LocalDate fechaFin) {
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay().minusNanos(1);
    List<Pago> pagosFiltrados = pagoRepository.findByMetodoPagoIdAndFechaPagoBetweenAndEstado(metodoPagoId, inicio, fin,
        EstadoPago.EXITOSO);
    return pagosFiltrados.stream()
        .mapToDouble(p -> p.getMonto() / 100.0) // Asumiendo que monto está en centavos
        .sum();
  }

  // ⭐ NUEVOS MÉTODOS PARA SUSCRIPCIONES

  @Override
  @Transactional
  public PagoResponseDTO procesarPagoSuscripcion(Long suscripcionId, Long metodoPagoId, Double monto, String periodo) {
    // Crear pago para suscripción
    PagoRequestDTO pagoRequest = PagoRequestDTO.builder()
        .suscripcionId(suscripcionId)
        .metodoPagoId(metodoPagoId)
        .monto((int) (monto * 100)) // Convertir a centavos
        .periodo(periodo)
        .esSimulado(true) // Por ahora simulado
        .simularFallo(false)
        .build();

    PagoResponseDTO pago = registrarPago(pagoRequest);
    return procesarPago(pago.getId());
  }

  @Override
  @Transactional
  public PagoResponseDTO simularRenovacionAutomatica(Long suscripcionId) {
    // Aquí deberías obtener la suscripción para calcular el monto
    // Por simplicidad, asumo un monto fijo para el ejemplo
    return procesarPagoSuscripcion(suscripcionId, 1L, 29.99, "Renovación Automática");
  }

  @Override
  @Transactional(readOnly = true)
  public List<PagoResponseDTO> obtenerPagosPorSuscripcion(Long suscripcionId) {
    return pagoRepository.findBySuscripcionId(suscripcionId)
        .stream()
        .map(pagoMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<PagoResponseDTO> obtenerPagosUnificadosPorUsuario(Long usuarioId) {
    // Combinar pagos de órdenes (a través de perfil) y suscripciones
    List<PagoResponseDTO> pagosSuscripciones = pagoRepository.findBySuscripcionUsuarioId(usuarioId)
        .stream()
        .map(pagoMapper::toResponseDTO)
        .collect(Collectors.toList());

    // Aquí podrías agregar pagos de órdenes si es necesario
    // List<PagoResponseDTO> pagosOrdenes = ...

    return pagosSuscripciones.stream()
        .sorted((p1, p2) -> {
          LocalDateTime fecha1 = p1.getFechaPago() != null ? p1.getFechaPago() : p1.getFechaCreacion();
          LocalDateTime fecha2 = p2.getFechaPago() != null ? p2.getFechaPago() : p2.getFechaCreacion();
          return fecha2.compareTo(fecha1); // Más recientes primero
        })
        .collect(Collectors.toList());
  }
}