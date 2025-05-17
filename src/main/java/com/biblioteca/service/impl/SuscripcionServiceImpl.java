package com.biblioteca.service.impl;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.biblioteca.dto.comercial.SuscripcionRequestDTO;
import com.biblioteca.dto.comercial.SuscripcionResponseDTO;
import com.biblioteca.mapper.comercial.SuscripcionMapper;
import com.biblioteca.models.Usuario;
import com.biblioteca.models.comercial.MetodoPago;
import com.biblioteca.models.comercial.Plan;
import com.biblioteca.models.comercial.Suscripcion;
import com.biblioteca.service.MetodoPagoService;
import com.biblioteca.service.PlanService;
import com.biblioteca.service.SuscripcionService;
import com.biblioteca.service.UsuarioService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuscripcionServiceImpl implements SuscripcionService {

  private final List<Suscripcion> suscripciones = new ArrayList<>();
  private final AtomicLong suscripcionIdCounter = new AtomicLong(0);
  private final SuscripcionMapper suscripcionMapper;
  private final PlanService planService;
  private final UsuarioService usuarioService;
  private final MetodoPagoService metodoPagoService;
  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;

  // Estados posibles de una suscripción
  public static final String ESTADO_ACTIVA = "ACTIVA";
  public static final String ESTADO_PENDIENTE = "PENDIENTE";
  public static final String ESTADO_CANCELADA = "CANCELADA";
  public static final String ESTADO_PAUSADA = "PAUSADA";
  public static final String ESTADO_VENCIDA = "VENCIDA";
  public static final String ESTADO_PRUEBA = "PRUEBA";

  @PostConstruct
  public void initSuscripcionesData() {
    try {
      InputStream inputStream = resourceLoader.getResource("classpath:data/suscripciones-data.json").getInputStream();
      List<SuscripcionRequestDTO> suscripcionesDTOs = objectMapper.readValue(inputStream,
          new TypeReference<List<SuscripcionRequestDTO>>() {
          });
      suscripcionesDTOs.forEach(this::crearSuscripcion);
      System.out
          .println("Datos iniciales de Suscripciones cargados desde JSON: " + suscripciones.size() + " suscripciones.");
    } catch (Exception e) {
      System.err.println("Error al cargar datos iniciales de suscripciones desde JSON: " + e.getMessage());
      // No iniciamos con datos por defecto ya que dependemos de usuarios, planes y
      // métodos de pago existentes
    }
  }

  @Override
  public SuscripcionResponseDTO crearSuscripcion(SuscripcionRequestDTO suscripcionDTO) {
    // Obtener Usuario, Plan y Método de Pago
    Usuario usuario = usuarioService.buscarPorId(suscripcionDTO.getUsuarioId())
        .orElseThrow(
            () -> new IllegalArgumentException("Usuario no encontrado con ID: " + suscripcionDTO.getUsuarioId()));

    Plan plan = planService.obtenerEntidadPlanPorId(suscripcionDTO.getPlanId())
        .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado con ID: " + suscripcionDTO.getPlanId()));

    MetodoPago metodoPago = metodoPagoService.obtenerEntidadMetodoPagoPorId(suscripcionDTO.getMetodoPagoId())
        .orElseThrow(() -> new IllegalArgumentException(
            "Método de pago no encontrado con ID: " + suscripcionDTO.getMetodoPagoId()));

    // Verificar si ya existe una suscripción activa para este usuario
    boolean existeSuscripcionActiva = verificarSuscripcionActiva(usuario.getId());
    if (existeSuscripcionActiva) {
      throw new IllegalStateException("El usuario ya tiene una suscripción activa");
    }

    // Crear suscripción
    Suscripcion suscripcion = suscripcionMapper.toEntity(suscripcionDTO);
    suscripcion.setId(suscripcionIdCounter.incrementAndGet());
    suscripcion.setUsuario(usuario);
    suscripcion.setPlan(plan);
    suscripcion.setMetodoPago(metodoPago);

    // Establecer fechas si no vienen en el DTO
    if (suscripcion.getFechaInicio() == null) {
      suscripcion.setFechaInicio(LocalDate.now());
    }

    // Establecer fecha de renovación (un mes después del inicio)
    if (suscripcion.getFechaRenovacion() == null) {
      suscripcion.setFechaRenovacion(suscripcion.getFechaInicio().plusMonths(1));
    }

    // Asignar estado si no viene en el DTO
    if (suscripcion.getEstado() == null || suscripcion.getEstado().isEmpty()) {
      // Si el plan tiene días de prueba, iniciar en estado PRUEBA
      if (plan.getDiasPrueba() > 0) {
        suscripcion.setEstado(ESTADO_PRUEBA);
        // La fecha de renovación en este caso es cuando termina el periodo de prueba
        suscripcion.setFechaRenovacion(suscripcion.getFechaInicio().plusDays(plan.getDiasPrueba()));
      } else {
        suscripcion.setEstado(ESTADO_ACTIVA);
      }
    }

    suscripciones.add(suscripcion);
    return suscripcionMapper.toResponseDTO(suscripcion);
  }

  @Override
  public Optional<SuscripcionResponseDTO> obtenerSuscripcionPorId(Long id) {
    return suscripciones.stream()
        .filter(s -> s.getId().equals(id))
        .findFirst()
        .map(suscripcionMapper::toResponseDTO);
  }

  @Override
  public List<SuscripcionResponseDTO> obtenerSuscripcionesPorUsuario(Long usuarioId) {
    return suscripciones.stream()
        .filter(s -> s.getUsuario().getId().equals(usuarioId))
        .map(suscripcionMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<SuscripcionResponseDTO> obtenerSuscripcionActivaPorUsuario(Long usuarioId) {
    return suscripciones.stream()
        .filter(s -> s.getUsuario().getId().equals(usuarioId) &&
            (ESTADO_ACTIVA.equals(s.getEstado()) || ESTADO_PRUEBA.equals(s.getEstado())))
        .findFirst()
        .map(suscripcionMapper::toResponseDTO);
  }

  @Override
  public List<SuscripcionResponseDTO> obtenerTodasLasSuscripciones() {
    return suscripciones.stream()
        .map(suscripcionMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<SuscripcionResponseDTO> actualizarSuscripcion(Long id, SuscripcionRequestDTO suscripcionDTO) {
    return obtenerEntidadSuscripcionPorId(id)
        .map(suscripcion -> {
          // Actualizar solo campos permitidos
          if (suscripcionDTO.getEstado() != null && !suscripcionDTO.getEstado().isEmpty()) {
            suscripcion.setEstado(suscripcionDTO.getEstado());
          }

          // Actualizar método de pago si se proporciona
          if (suscripcionDTO.getMetodoPagoId() != null) {
            MetodoPago metodoPago = metodoPagoService.obtenerEntidadMetodoPagoPorId(suscripcionDTO.getMetodoPagoId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Método de pago no encontrado con ID: " + suscripcionDTO.getMetodoPagoId()));
            suscripcion.setMetodoPago(metodoPago);
          }

          // Actualizar plan si se proporciona
          if (suscripcionDTO.getPlanId() != null && !suscripcion.getPlan().getId().equals(suscripcionDTO.getPlanId())) {
            Plan nuevoPlan = planService.obtenerEntidadPlanPorId(suscripcionDTO.getPlanId())
                .orElseThrow(
                    () -> new IllegalArgumentException("Plan no encontrado con ID: " + suscripcionDTO.getPlanId()));
            suscripcion.setPlan(nuevoPlan);
          }

          // Actualizar fechas si se proporcionan
          if (suscripcionDTO.getFechaInicio() != null) {
            suscripcion.setFechaInicio(suscripcionDTO.getFechaInicio());
          }

          if (suscripcionDTO.getFechaRenovacion() != null) {
            suscripcion.setFechaRenovacion(suscripcionDTO.getFechaRenovacion());
          }

          return suscripcionMapper.toResponseDTO(suscripcion);
        });
  }

  @Override
  public boolean eliminarSuscripcion(Long id) {
    return suscripciones.removeIf(s -> s.getId().equals(id));
  }

  @Override
  public Optional<Suscripcion> obtenerEntidadSuscripcionPorId(Long id) {
    return suscripciones.stream()
        .filter(s -> s.getId().equals(id))
        .findFirst();
  }

  @Override
  public SuscripcionResponseDTO renovarSuscripcion(Long id) {
    Suscripcion suscripcion = obtenerEntidadSuscripcionPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Suscripción no encontrada con ID: " + id));

    // Verificar que la suscripción esté en un estado que permita renovación
    if (ESTADO_CANCELADA.equals(suscripcion.getEstado())) {
      throw new IllegalStateException("No se puede renovar una suscripción cancelada");
    }

    // Extender la fecha de renovación por un mes más
    suscripcion.setFechaRenovacion(suscripcion.getFechaRenovacion().plusMonths(1));

    // Asegurar que el estado sea activo
    suscripcion.setEstado(ESTADO_ACTIVA);

    return suscripcionMapper.toResponseDTO(suscripcion);
  }

  @Override
  public SuscripcionResponseDTO cambiarPlan(Long id, Long nuevoPlanId) {
    Suscripcion suscripcion = obtenerEntidadSuscripcionPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Suscripción no encontrada con ID: " + id));

    Plan nuevoPlan = planService.obtenerEntidadPlanPorId(nuevoPlanId)
        .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado con ID: " + nuevoPlanId));

    // Cambiar plan
    suscripcion.setPlan(nuevoPlan);

    return suscripcionMapper.toResponseDTO(suscripcion);
  }

  @Override
  public boolean cancelarSuscripcion(Long id) {
    return obtenerEntidadSuscripcionPorId(id)
        .map(suscripcion -> {
          suscripcion.setEstado(ESTADO_CANCELADA);
          return true;
        })
        .orElse(false);
  }

  @Override
  public boolean pausarSuscripcion(Long id) {
    return obtenerEntidadSuscripcionPorId(id)
        .map(suscripcion -> {
          // Solo se pueden pausar suscripciones activas
          if (ESTADO_ACTIVA.equals(suscripcion.getEstado()) || ESTADO_PRUEBA.equals(suscripcion.getEstado())) {
            suscripcion.setEstado(ESTADO_PAUSADA);
            return true;
          }
          return false;
        })
        .orElse(false);
  }

  @Override
  public boolean reactivarSuscripcion(Long id) {
    return obtenerEntidadSuscripcionPorId(id)
        .map(suscripcion -> {
          // Solo se pueden reactivar suscripciones pausadas o vencidas
          if (ESTADO_PAUSADA.equals(suscripcion.getEstado()) || ESTADO_VENCIDA.equals(suscripcion.getEstado())) {
            suscripcion.setEstado(ESTADO_ACTIVA);

            // Si está vencida, actualizar fecha de renovación
            if (ESTADO_VENCIDA.equals(suscripcion.getEstado())) {
              suscripcion.setFechaRenovacion(LocalDate.now().plusMonths(1));
            }

            return true;
          }
          return false;
        })
        .orElse(false);
  }

  @Override
  public List<SuscripcionResponseDTO> obtenerSuscripcionesPorVencer(int diasRestantes) {
    LocalDate fechaLimite = LocalDate.now().plusDays(diasRestantes);
    return suscripciones.stream()
        .filter(s -> s.getFechaRenovacion().isBefore(fechaLimite) || s.getFechaRenovacion().isEqual(fechaLimite))
        .filter(s -> ESTADO_ACTIVA.equals(s.getEstado()) || ESTADO_PRUEBA.equals(s.getEstado()))
        .map(suscripcionMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<SuscripcionResponseDTO> obtenerSuscripcionesVencidas() {
    LocalDate hoy = LocalDate.now();
    return suscripciones.stream()
        .filter(s -> s.getFechaRenovacion().isBefore(hoy))
        .filter(s -> ESTADO_ACTIVA.equals(s.getEstado()) || ESTADO_PRUEBA.equals(s.getEstado()))
        .map(suscripcionMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public boolean verificarSuscripcionActiva(Long usuarioId) {
    return suscripciones.stream()
        .anyMatch(s -> s.getUsuario().getId().equals(usuarioId) &&
            (ESTADO_ACTIVA.equals(s.getEstado()) || ESTADO_PRUEBA.equals(s.getEstado())) &&
            (s.getFechaRenovacion().isAfter(LocalDate.now()) || s.getFechaRenovacion().isEqual(LocalDate.now())));
  }

  @Override
  public SuscripcionResponseDTO cambiarMetodoPago(Long id, Long nuevoMetodoPagoId) {
    Suscripcion suscripcion = obtenerEntidadSuscripcionPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Suscripción no encontrada con ID: " + id));

    MetodoPago nuevoMetodoPago = metodoPagoService.obtenerEntidadMetodoPagoPorId(nuevoMetodoPagoId)
        .orElseThrow(() -> new IllegalArgumentException("Método de pago no encontrado con ID: " + nuevoMetodoPagoId));

    // Cambiar método de pago
    suscripcion.setMetodoPago(nuevoMetodoPago);

    return suscripcionMapper.toResponseDTO(suscripcion);
  }
}