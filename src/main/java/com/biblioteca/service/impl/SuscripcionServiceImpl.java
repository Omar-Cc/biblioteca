package com.biblioteca.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.comercial.SuscripcionRequestDTO;
import com.biblioteca.dto.comercial.SuscripcionResponseDTO;
import com.biblioteca.exceptions.OperacionNoPermitidaException;
import com.biblioteca.exceptions.RecursoNoEncontradoException;
import com.biblioteca.mapper.comercial.SuscripcionMapper;
import com.biblioteca.models.acceso.Usuario;
import com.biblioteca.models.comercial.MetodoPago;
import com.biblioteca.models.comercial.Plan;
import com.biblioteca.models.comercial.Suscripcion;
import com.biblioteca.repositories.comercial.PlanRepository;
import com.biblioteca.repositories.comercial.SuscripcionRepository;
import com.biblioteca.service.MetodoPagoService;
import com.biblioteca.service.PeriodoPruebaService;
import com.biblioteca.service.PlanService;
import com.biblioteca.service.SuscripcionService;
import com.biblioteca.service.UsuarioService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SuscripcionServiceImpl implements SuscripcionService {

  private final SuscripcionRepository suscripcionRepository;
  private final SuscripcionMapper suscripcionMapper;
  private final PlanService planService;
  private final UsuarioService usuarioService;
  private final MetodoPagoService metodoPagoService;
  private final PlanRepository planRepository;
  private final PeriodoPruebaService periodoPruebaService;

  public SuscripcionServiceImpl(
      SuscripcionRepository suscripcionRepository,
      SuscripcionMapper suscripcionMapper,
      PlanService planService,
      @Lazy UsuarioService usuarioService,
      MetodoPagoService metodoPagoService,
      PlanRepository planRepository,
      PeriodoPruebaService periodoPruebaService) {
    this.suscripcionRepository = suscripcionRepository;
    this.suscripcionMapper = suscripcionMapper;
    this.planService = planService;
    this.usuarioService = usuarioService;
    this.metodoPagoService = metodoPagoService;
    this.planRepository = planRepository;
    this.periodoPruebaService = periodoPruebaService;
  }

  // Estados posibles de una suscripción
  public static final String ESTADO_ACTIVA = "ACTIVA";
  public static final String ESTADO_PENDIENTE = "PENDIENTE";
  public static final String ESTADO_CANCELADA = "CANCELADA";
  public static final String ESTADO_PAUSADA = "PAUSADA";
  public static final String ESTADO_VENCIDA = "VENCIDA";
  public static final String ESTADO_PRUEBA = "PRUEBA";

  @Override
  @Transactional
  public SuscripcionResponseDTO crearSuscripcion(SuscripcionRequestDTO suscripcionDTO) {
    validarReglasSuscripcion(suscripcionDTO);
    Usuario usuario = usuarioService.buscarPorId(suscripcionDTO.getUsuarioId())
        .orElseThrow(
            () -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + suscripcionDTO.getUsuarioId()));

    Plan plan = planService.obtenerEntidadPlanPorId(suscripcionDTO.getPlanId())
        .orElseThrow(
            () -> new RecursoNoEncontradoException("Plan no encontrado con ID: " + suscripcionDTO.getPlanId()));

    MetodoPago metodoPago = metodoPagoService.obtenerEntidadMetodoPagoPorId(suscripcionDTO.getMetodoPagoId())
        .orElseThrow(() -> new RecursoNoEncontradoException(
            "Método de pago no encontrado con ID: " + suscripcionDTO.getMetodoPagoId()));

    if (verificarSuscripcionActiva(usuario.getId())) {
      throw new OperacionNoPermitidaException("El usuario ya tiene una suscripción activa o en prueba.");
    }

    Suscripcion suscripcion = suscripcionMapper.toEntity(suscripcionDTO);
    // El ID será generado por la base de datos
    suscripcion.setUsuario(usuario);
    suscripcion.setPlan(plan);
    suscripcion.setMetodoPago(metodoPago);

    if (suscripcion.getFechaInicio() == null) {
      suscripcion.setFechaInicio(LocalDate.now());
    }

    // Calcular fecha de renovación basada en el plan (mensual/anual/días de prueba)
    if (suscripcion.getFechaRenovacion() == null) {
      if (plan.getDiasPrueba() != null && plan.getDiasPrueba() > 0
          && (suscripcionDTO.getEstado() == null || ESTADO_PRUEBA.equals(suscripcionDTO.getEstado()))) {
        suscripcion.setFechaRenovacion(suscripcion.getFechaInicio().plusDays(plan.getDiasPrueba()));
        suscripcion.setEstado(ESTADO_PRUEBA);
      } else if ("ANUAL".equalsIgnoreCase(plan.getPeriodoFacturacion())) {
        suscripcion.setFechaRenovacion(suscripcion.getFechaInicio().plusYears(1));
      } else { // Por defecto MENSUAL o si no está especificado
        suscripcion.setFechaRenovacion(suscripcion.getFechaInicio().plusMonths(1));
      }
    }

    if (suscripcion.getEstado() == null || suscripcion.getEstado().isEmpty()) {
      suscripcion
          .setEstado(plan.getDiasPrueba() != null && plan.getDiasPrueba() > 0 ? ESTADO_PRUEBA : ESTADO_PENDIENTE);
    }

    // La relación bidireccional Plan <-> Suscripcion se maneja si Plan tiene
    // CascadeType.PERSIST o ALL
    // plan.addSuscripcion(suscripcion); // No es estrictamente necesario si la
    // relación está bien mapeada

    Suscripcion suscripcionGuardada = suscripcionRepository.save(suscripcion);
    return convertirConCalculoDias(suscripcionGuardada);
  }

  @Transactional
  public void crearSuscripcionBasicaGratuita(Usuario usuario, Plan planBasico) {
    // Crear suscripción directamente sin método de pago
    Suscripcion suscripcion = new Suscripcion();
    suscripcion.setUsuario(usuario);
    suscripcion.setPlan(planBasico);
    suscripcion.setFechaInicio(LocalDate.now());
    suscripcion.setEstado("ACTIVA");
    suscripcion.setMetodoPago(null); // Sin método de pago para plan gratuito
    suscripcion.setFechaRenovacion(null); // Sin límite para plan gratuito

    suscripcionRepository.save(suscripcion);
    log.info("Suscripción gratuita creada para usuario ID: {}", usuario.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<SuscripcionResponseDTO> obtenerSuscripcionPorId(Long id) {
    return suscripcionRepository.findById(id)
        .map(this::convertirConCalculoDias);
  }

  @Override
  @Transactional(readOnly = true)
  public List<SuscripcionResponseDTO> obtenerSuscripcionesPorUsuario(Long usuarioId) {
    return suscripcionRepository.findByUsuarioId(usuarioId).stream()
        .map(this::convertirConCalculoDias)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<SuscripcionResponseDTO> obtenerSuscripcionActivaPorUsuario(Long usuarioId) {
    // Busca primero por estado ACTIVA, luego por PRUEBA si no se encuentra activa
    Optional<Suscripcion> activaOpt = suscripcionRepository.findByUsuarioIdAndEstado(usuarioId, ESTADO_ACTIVA);
    if (activaOpt.isPresent()) {
      return activaOpt.map(this::convertirConCalculoDias);
    }
    return suscripcionRepository.findByUsuarioIdAndEstado(usuarioId, ESTADO_PRUEBA)
        .map(this::convertirConCalculoDias);
  }

  @Override
  @Transactional(readOnly = true)
  public List<SuscripcionResponseDTO> obtenerTodasLasSuscripciones() {
    return suscripcionRepository.findAll().stream()
        .map(this::convertirConCalculoDias)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Optional<SuscripcionResponseDTO> actualizarSuscripcion(Long id, SuscripcionRequestDTO suscripcionDTO) {
    return suscripcionRepository.findById(id)
        .map(suscripcion -> {
          // Actualizar campos básicos. No se permite cambiar usuario o plan directamente
          // aquí.
          // Para cambiar plan, usar el método cambiarPlan.
          if (suscripcionDTO.getFechaInicio() != null) {
            suscripcion.setFechaInicio(suscripcionDTO.getFechaInicio());
          }
          if (suscripcionDTO.getFechaRenovacion() != null) {
            suscripcion.setFechaRenovacion(suscripcionDTO.getFechaRenovacion());
          }
          if (suscripcionDTO.getEstado() != null && !suscripcionDTO.getEstado().isEmpty()) {
            suscripcion.setEstado(suscripcionDTO.getEstado());
          }
          if (suscripcionDTO.getMetodoPagoId() != null &&
              (suscripcion.getMetodoPago() == null
                  || !suscripcion.getMetodoPago().getId().equals(suscripcionDTO.getMetodoPagoId()))) {
            MetodoPago nuevoMetodoPago = metodoPagoService
                .obtenerEntidadMetodoPagoPorId(suscripcionDTO.getMetodoPagoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                    "Nuevo método de pago no encontrado con ID: " + suscripcionDTO.getMetodoPagoId()));
            suscripcion.setMetodoPago(nuevoMetodoPago);
          }
          Suscripcion suscripcionActualizada = suscripcionRepository.save(suscripcion);
          return convertirConCalculoDias(suscripcionActualizada);
        });
  }

  @Override
  @Transactional
  public boolean eliminarSuscripcion(Long id) {
    if (suscripcionRepository.existsById(id)) {
      // Considerar la lógica de negocio: ¿Se pueden eliminar suscripciones activas?
      // ¿Qué pasa con el historial de pagos?
      // Con CascadeType.ALL y orphanRemoval=true en Suscripcion.historialPagos, se
      // eliminarán.
      suscripcionRepository.deleteById(id);
      return true;
    }
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Suscripcion> obtenerEntidadSuscripcionPorId(Long id) {
    return suscripcionRepository.findById(id);
  }

  @Override
  @Transactional
  public SuscripcionResponseDTO renovarSuscripcion(Long id) {
    Suscripcion suscripcion = suscripcionRepository.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Suscripción no encontrada con ID: " + id));

    if (!ESTADO_ACTIVA.equals(suscripcion.getEstado()) && !ESTADO_PRUEBA.equals(suscripcion.getEstado())) {
      throw new OperacionNoPermitidaException("Solo se pueden renovar suscripciones activas o en prueba.");
    }

    Plan plan = suscripcion.getPlan();
    if ("ANUAL".equalsIgnoreCase(plan.getPeriodoFacturacion())) {
      suscripcion.setFechaRenovacion(suscripcion.getFechaRenovacion().plusYears(1));
    } else { // MENSUAL o por defecto
      suscripcion.setFechaRenovacion(suscripcion.getFechaRenovacion().plusMonths(1));
    }
    suscripcion.setEstado(ESTADO_ACTIVA); // Asegurar que esté activa después de renovar
    // Aquí se podría generar un nuevo HistorialPagoSuscripcion con estado PENDIENTE

    Suscripcion suscripcionRenovada = suscripcionRepository.save(suscripcion);
    return convertirConCalculoDias(suscripcionRenovada);
  }

  @Override
  @Transactional
  public SuscripcionResponseDTO cambiarPlan(Long id, Long nuevoPlanId) {
    return cambiarPlan(id, nuevoPlanId, null);
  }

  @Override
  @Transactional
  public SuscripcionResponseDTO cambiarPlan(Long id, Long nuevoPlanId, String modalidadPago) {
    Suscripcion suscripcion = obtenerEntidadSuscripcionPorId(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Suscripción no encontrada"));

    Plan planActual = suscripcion.getPlan();
    Plan nuevoPlan = planRepository.findById(nuevoPlanId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Plan no encontrado"));

    String modalidadAnterior = suscripcion.getModalidadPago();
    boolean hayCambioModalidad = modalidadPago != null && !modalidadPago.equals(modalidadAnterior);
    boolean hayCambioPlan = !planActual.getId().equals(nuevoPlan.getId());

    log.info("Procesando cambio: Plan {} -> {}, Modalidad {} -> {}",
        planActual.getNombre(), nuevoPlan.getNombre(), modalidadAnterior, modalidadPago);

    // Si solo hay cambio de modalidad (mismo plan)
    if (!hayCambioPlan && hayCambioModalidad) {
      log.info("Procesando solo cambio de modalidad de {} a {}", modalidadAnterior, modalidadPago);

      // Calcular prorrateado para cambio de modalidad
      calcularYProcesarProrrateado(suscripcion, planActual, modalidadPago);

      // Actualizar modalidad y recalcular fechas
      suscripcion.setModalidadPago(modalidadPago);
      recalcularFechasRenovacion(suscripcion, modalidadPago);

      return convertirConCalculoDias(suscripcionRepository.save(suscripcion));
    }

    // Actualizar modalidad de pago si se proporciona
    if (modalidadPago != null) {
      suscripcion.setModalidadPago(modalidadPago);
      recalcularFechasRenovacion(suscripcion, modalidadPago);
    }

    // Lógica de cambio de plan
    if (esUpgrade(planActual, nuevoPlan)) {
      procesarUpgrade(suscripcion, nuevoPlan, modalidadPago);
    } else if (esDowngrade(planActual, nuevoPlan)) {
      procesarDowngrade(suscripcion, nuevoPlan);
    } else {
      // Mismo precio, solo cambiar plan
      suscripcion.setPlan(nuevoPlan);

      // Si hay cambio de modalidad, calcular prorrateado
      if (hayCambioModalidad) {
        calcularYProcesarProrrateado(suscripcion, nuevoPlan, modalidadPago);
      }
    }

    return convertirConCalculoDias(suscripcionRepository.save(suscripcion));
  }

  /**
   * Recalcula las fechas de renovación según la modalidad de pago
   */
  private void recalcularFechasRenovacion(Suscripcion suscripcion, String modalidadPago) {
    LocalDate hoy = LocalDate.now();

    if ("anual".equals(modalidadPago)) {
      // Para cambio a anual, extender desde hoy
      suscripcion.setFechaRenovacion(hoy.plusYears(1));
    } else {
      // Para cambio a mensual, próximo mes
      suscripcion.setFechaRenovacion(hoy.plusMonths(1));
    }

    log.info("Fechas recalculadas para modalidad {}: renovación {}",
        modalidadPago, suscripcion.getFechaRenovacion());
  }

  private boolean esUpgrade(Plan planActual, Plan nuevoPlan) {
    return nuevoPlan.getPrecioMensual() > planActual.getPrecioMensual();
  }

  private void procesarUpgrade(Suscripcion suscripcion, Plan nuevoPlan, String nuevaModalidad) {
    // Upgrade inmediato
    suscripcion.setPlan(nuevoPlan);
    suscripcion.setEstado("ACTIVA");

    // Calcular prorrateado considerando modalidades
    if (nuevoPlan.getPrecioMensual() > 0) {
      calcularYProcesarProrrateado(suscripcion, nuevoPlan, nuevaModalidad);
    }
  }

  private void procesarDowngrade(Suscripcion suscripcion, Plan nuevoPlan) {
    // Downgrade al final del período actual
    // Crear una suscripción pendiente
    crearSuscripcionPendiente(suscripcion.getUsuario(), nuevoPlan,
        suscripcion.getFechaRenovacion());
  }

  /**
   * Crea una suscripción pendiente que se activará en una fecha futura
   * Utilizado principalmente para downgrades diferidos
   */
  private void crearSuscripcionPendiente(Usuario usuario, Plan nuevoPlan, LocalDate fechaActivacion) {
    log.info("Creando suscripción pendiente para usuario {} con plan {} - Activación: {}",
        usuario.getId(), nuevoPlan.getId(), fechaActivacion);

    try {
      // Buscar método de pago activo del usuario de la suscripción actual
      MetodoPago metodoPagoActivo = obtenerMetodoPagoActivoUsuario(usuario.getId())
          .orElseThrow(() -> new OperacionNoPermitidaException(
              "No se encontró método de pago activo para el usuario"));

      // Crear nueva suscripción pendiente
      Suscripcion suscripcionPendiente = Suscripcion.builder()
          .usuario(usuario)
          .plan(nuevoPlan)
          .metodoPago(metodoPagoActivo)
          .fechaInicio(fechaActivacion) // Se activará cuando expire la actual
          .fechaRenovacion(calcularProximaRenovacion(fechaActivacion, nuevoPlan))
          .estado(ESTADO_PENDIENTE)
          .estadoAnterior(null)
          .fechaCambioEstado(LocalDateTime.now())
          .motivoCambio("Downgrade programado desde plan superior")
          .modalidadPago("mensual") // Por defecto mensual para downgrades
          .build();

      // Guardar suscripción pendiente
      Suscripcion suscripcionGuardada = suscripcionRepository.save(suscripcionPendiente);

      log.info("Suscripción pendiente creada exitosamente con ID: {} - Se activará el: {}",
          suscripcionGuardada.getId(), fechaActivacion);

    } catch (Exception e) {
      log.error("Error al crear suscripción pendiente para usuario {}: {}",
          usuario.getId(), e.getMessage(), e);
      throw new OperacionNoPermitidaException(
          "No se pudo programar el cambio de plan: " + e.getMessage());
    }
  }

  /**
   * Busca el método de pago activo del usuario de su suscripción actual
   */
  private Optional<MetodoPago> obtenerMetodoPagoActivoUsuario(Long usuarioId) {
    return obtenerSuscripcionActivaPorUsuario(usuarioId)
        .flatMap(suscripcionDTO -> metodoPagoService.obtenerEntidadMetodoPagoPorId(suscripcionDTO.getMetodoPagoId()));
  }

  /**
   * Calcula la próxima fecha de renovación basada en el plan y modalidad
   */
  private LocalDate calcularProximaRenovacion(LocalDate fechaInicio, Plan plan) {
    // Para planes en downgrade, usar periodo mensual por defecto
    return fechaInicio.plusMonths(1);
  }

  private Suscripcion cambiarEstadoSuscripcion(Long id, String nuevoEstado) {
    Suscripcion suscripcion = suscripcionRepository.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Suscripción no encontrada con ID: " + id));
    // Validar transiciones de estado si es necesario
    suscripcion.setEstado(nuevoEstado);
    return suscripcionRepository.save(suscripcion);
  }

  @Override
  @Transactional
  public boolean cancelarSuscripcion(Long id) {
    cambiarEstadoSuscripcion(id, ESTADO_CANCELADA);
    return true;
  }

  @Override
  @Transactional
  public boolean pausarSuscripcion(Long id) {
    // Lógica adicional: ¿Por cuánto tiempo? ¿Afecta la fecha de renovación?
    cambiarEstadoSuscripcion(id, ESTADO_PAUSADA);
    return true;
  }

  @Override
  @Transactional
  public boolean reactivarSuscripcion(Long id) {
    Suscripcion suscripcion = suscripcionRepository.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Suscripción no encontrada con ID: " + id));
    if (!ESTADO_PAUSADA.equals(suscripcion.getEstado()) && !ESTADO_CANCELADA.equals(suscripcion.getEstado())
        && !ESTADO_VENCIDA.equals(suscripcion.getEstado())) {
      throw new OperacionNoPermitidaException(
          "Solo se pueden reactivar suscripciones pausadas, canceladas o vencidas.");
    }
    // Lógica de reactivación: ajustar fecha de renovación, etc.
    suscripcion.setEstado(ESTADO_ACTIVA);
    // Podría ser necesario ajustar la fecha de renovación
    // suscripcion.setFechaRenovacion(LocalDate.now().plusMonths(1)); // Ejemplo
    suscripcionRepository.save(suscripcion);
    return true;
  }

  @Override
  @Transactional(readOnly = true)
  public List<SuscripcionResponseDTO> obtenerSuscripcionesPorVencer(int diasRestantes) {
    LocalDate hoy = LocalDate.now();
    LocalDate fechaLimite = hoy.plusDays(diasRestantes);
    return suscripcionRepository.findByEstadoAndFechaRenovacionBetween(ESTADO_ACTIVA, hoy, fechaLimite)
        .stream()
        .map(this::convertirConCalculoDias)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<SuscripcionResponseDTO> obtenerSuscripcionesVencidas() {
    return suscripcionRepository.findSuscripcionesVencidas(LocalDate.now())
        .stream()
        .map(this::convertirConCalculoDias)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public boolean verificarSuscripcionActiva(Long usuarioId) {
    return suscripcionRepository.existsByUsuarioIdAndEstado(usuarioId, ESTADO_ACTIVA) ||
        suscripcionRepository.existsByUsuarioIdAndEstado(usuarioId, ESTADO_PRUEBA);
  }

  @Override
  @Transactional
  public SuscripcionResponseDTO cambiarMetodoPago(Long id, Long nuevoMetodoPagoId) {
    Suscripcion suscripcion = suscripcionRepository.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Suscripción no encontrada con ID: " + id));
    MetodoPago nuevoMetodoPago = metodoPagoService.obtenerEntidadMetodoPagoPorId(nuevoMetodoPagoId)
        .orElseThrow(
            () -> new RecursoNoEncontradoException("Nuevo método de pago no encontrado con ID: " + nuevoMetodoPagoId));

    suscripcion.setMetodoPago(nuevoMetodoPago);
    Suscripcion actualizada = suscripcionRepository.save(suscripcion);
    return convertirConCalculoDias(actualizada);
  }

  private void validarReglasSuscripcion(SuscripcionRequestDTO suscripcionDTO) {
    Plan plan = planRepository.findById(suscripcionDTO.getPlanId())
        .orElseThrow(() -> new RecursoNoEncontradoException("Plan no encontrado"));

    // Si no es plan básico, requiere método de pago
    if (plan.getPrecioMensual() > 0 && suscripcionDTO.getMetodoPagoId() == null) {
      throw new OperacionNoPermitidaException("Los planes de pago requieren un método de pago válido");
    }

    // Verificar período de prueba
    if (plan.getPrecioMensual() > 0 && plan.getDiasPrueba() > 0) {
      if (periodoPruebaService.puedeUsarPeriodoPrueba(suscripcionDTO.getUsuarioId(), plan.getId())) {
        // Aplicar período de prueba
        suscripcionDTO.setEstado("PRUEBA");
        if (suscripcionDTO.getFechaInicio() == null) {
          suscripcionDTO.setFechaInicio(LocalDate.now());
        }
        suscripcionDTO.setFechaRenovacion(
            suscripcionDTO.getFechaInicio().plusDays(plan.getDiasPrueba()));

        // Marcar período de prueba como usado
        periodoPruebaService.marcarPeriodoPruebaUsado(suscripcionDTO.getUsuarioId());
      } else {
        // Sin período de prueba, cobro inmediato
        suscripcionDTO.setEstado("ACTIVA");
      }
    }
  }

  private boolean esDowngrade(Plan planActual, Plan nuevoPlan) {
    return nuevoPlan.getPrecioMensual() < planActual.getPrecioMensual();
  }

  private void calcularYProcesarProrrateado(Suscripcion suscripcion, Plan nuevoPlan) {
    calcularYProcesarProrrateado(suscripcion, nuevoPlan, null);
  }

  private void calcularYProcesarProrrateado(Suscripcion suscripcion, Plan nuevoPlan, String nuevaModalidad) {
    LocalDate hoy = LocalDate.now();
    LocalDate fechaRenovacion = suscripcion.getFechaRenovacion();

    if (!fechaRenovacion.isAfter(hoy)) {
      log.debug("No hay días restantes para calcular prorrateado");
      return;
    }

    long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(hoy, fechaRenovacion);

    // Calcular días totales del período actual basado en la modalidad actual
    long diasTotalPeriodo = calcularDiasTotalPeriodo(suscripcion);

    if (diasTotalPeriodo <= 0) {
      log.warn("No se pudo calcular el período total para prorrateado");
      return;
    }

    double factorProrrateado = (double) diasRestantes / diasTotalPeriodo;

    // Calcular diferencia de precios considerando modalidades
    double diferenciaPrecio = calcularDiferenciaPrecio(suscripcion, nuevoPlan, nuevaModalidad);

    if (diferenciaPrecio == 0.0) {
      log.debug("No hay diferencia de precio entre planes/modalidades");
      return;
    }

    double montoProrrateado = diferenciaPrecio * factorProrrateado;

    log.info("Prorrateado calculado: {} días restantes de {}, factor: {:.2f}, diferencia: ${:.2f}, monto: ${:.2f}",
        diasRestantes, diasTotalPeriodo, factorProrrateado, diferenciaPrecio, montoProrrateado);

    // Crear cargo o crédito según el signo del monto
    if (montoProrrateado > 0) {
      log.info("Creando cargo por upgrade prorrateado: ${:.2f}", montoProrrateado);
      crearCargoProrrateado(suscripcion, montoProrrateado);
    } else if (montoProrrateado < 0) {
      log.info("Creando crédito por cambio de modalidad: ${:.2f}", Math.abs(montoProrrateado));
      crearCreditoProrrateado(suscripcion, Math.abs(montoProrrateado));
    }
  }

  /**
   * Calcula los días totales del período actual basado en la modalidad de pago
   */
  private long calcularDiasTotalPeriodo(Suscripcion suscripcion) {
    String modalidad = suscripcion.getModalidadPago();
    LocalDate fechaInicio = suscripcion.getFechaInicio();
    LocalDate fechaRenovacion = suscripcion.getFechaRenovacion();

    if (fechaInicio != null && fechaRenovacion != null) {
      return java.time.temporal.ChronoUnit.DAYS.between(fechaInicio, fechaRenovacion);
    }

    // Si no hay fechas exactas, calcular basado en modalidad
    if ("ANUAL".equals(modalidad)) {
      return 365; // Año
    } else {
      return 30; // Mes promedio
    }
  }

  /**
   * Calcula la diferencia de precio considerando modalidades actuales y nuevas
   */
  private double calcularDiferenciaPrecio(Suscripcion suscripcion, Plan nuevoPlan, String nuevaModalidad) {
    Plan planActual = suscripcion.getPlan();
    String modalidadActual = suscripcion.getModalidadPago();

    // Si no se especifica nueva modalidad, mantener la actual
    if (nuevaModalidad == null) {
      nuevaModalidad = modalidadActual;
    }

    // Calcular precio actual por día
    double precioActualDiario = calcularPrecioDiario(planActual, modalidadActual);

    // Calcular precio nuevo por día
    double precioNuevoDiario = calcularPrecioDiario(nuevoPlan, nuevaModalidad);

    // Calcular días restantes para determinar el monto total
    LocalDate hoy = LocalDate.now();
    LocalDate fechaRenovacion = suscripcion.getFechaRenovacion();
    long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(hoy, fechaRenovacion);

    double diferenciaDiaria = precioNuevoDiario - precioActualDiario;

    log.debug("Precio actual diario: ${:.4f}, precio nuevo diario: ${:.4f}, diferencia: ${:.4f}",
        precioActualDiario, precioNuevoDiario, diferenciaDiaria);

    return diferenciaDiaria * diasRestantes;
  }

  /**
   * Calcula el precio diario de un plan según su modalidad
   */
  private double calcularPrecioDiario(Plan plan, String modalidad) {
    if ("ANUAL".equals(modalidad)) {
      // Precio anual tiene descuento, típicamente precio mensual * 10 (20% descuento)
      double precioAnual = plan.getPrecioMensual() * 10.0; // 10 meses en lugar de 12
      return precioAnual / 365.0; // Dividir entre días del año
    } else {
      // Modalidad mensual
      return plan.getPrecioMensual() / 30.0; // Dividir entre días promedio del mes
    }
  }

  /**
   * Simula la creación de un cargo prorrateado (a implementar con sistema de
   * pagos)
   */
  private void crearCargoProrrateado(Suscripcion suscripcion, double monto) {
    log.info("CARGO PRORRATEADO: Usuario {} - Monto: ${:.2f} - Concepto: Upgrade/cambio de plan",
        suscripcion.getUsuario().getUsername(), monto);

    // TODO: Integrar con el sistema de pagos real
    // Crear registro de cargo pendiente
    // notificarCargoProrrateado(suscripcion, monto);
  }

  /**
   * Simula la creación de un crédito prorrateado (a implementar con sistema de
   * pagos)
   */
  private void crearCreditoProrrateado(Suscripcion suscripcion, double monto) {
    log.info("CRÉDITO PRORRATEADO: Usuario {} - Monto: ${:.2f} - Concepto: Cambio de modalidad/downgrade",
        suscripcion.getUsuario().getUsername(), monto);

    // TODO: Integrar con el sistema de pagos real
    // Crear registro de crédito para próxima facturación
    // notificarCreditoProrrateado(suscripcion, monto);
  }

  /**
   * Método de prueba para simular diferentes escenarios de prorrateado
   */
  public void simularEscenariosProrrateado() {
    log.info("=== SIMULACIÓN DE ESCENARIOS DE PRORRATEADO ===");

    // Crear plan ficticio para pruebas
    Plan planBasico = new Plan();
    planBasico.setId(1L);
    planBasico.setNombre("Plan Básico");
    planBasico.setPrecioMensual(2999); // $29.99 en centavos

    Plan planPremium = new Plan();
    planPremium.setId(2L);
    planPremium.setNombre("Plan Premium");
    planPremium.setPrecioMensual(4999); // $49.99 en centavos

    // Escenario 1: Cambio de mensual a anual (mismo plan)
    log.info("\n--- ESCENARIO 1: Cambio Mensual → Anual (mismo plan) ---");
    double precioMensualDiario = calcularPrecioDiario(planBasico, "MENSUAL");
    double precioAnualDiario = calcularPrecioDiario(planBasico, "ANUAL");
    double diferenciaDiaria = precioAnualDiario - precioMensualDiario;
    int diasRestantes = 15; // 15 días restantes del período actual

    log.info("Precio mensual diario: ${:.4f}", precioMensualDiario / 100.0);
    log.info("Precio anual diario: ${:.4f}", precioAnualDiario / 100.0);
    log.info("Diferencia diaria: ${:.4f}", diferenciaDiaria / 100.0);
    log.info("Monto prorrateado (15 días): ${:.2f}", (diferenciaDiaria * diasRestantes) / 100.0);

    // Escenario 2: Cambio de anual a mensual (mismo plan)
    log.info("\n--- ESCENARIO 2: Cambio Anual → Mensual (mismo plan) ---");
    diferenciaDiaria = precioMensualDiario - precioAnualDiario;
    log.info("Diferencia diaria: ${:.4f}", diferenciaDiaria / 100.0);
    log.info("Reembolso prorrateado (15 días): ${:.2f}", Math.abs(diferenciaDiaria * diasRestantes) / 100.0);

    // Escenario 3: Upgrade de plan con cambio de modalidad
    log.info("\n--- ESCENARIO 3: Upgrade Básico Mensual → Premium Anual ---");
    double precioBasicoMensualDiario = calcularPrecioDiario(planBasico, "MENSUAL");
    double precioPremiumAnualDiario = calcularPrecioDiario(planPremium, "ANUAL");
    diferenciaDiaria = precioPremiumAnualDiario - precioBasicoMensualDiario;

    log.info("Precio Básico mensual diario: ${:.4f}", precioBasicoMensualDiario / 100.0);
    log.info("Precio Premium anual diario: ${:.4f}", precioPremiumAnualDiario / 100.0);
    log.info("Diferencia diaria: ${:.4f}", diferenciaDiaria / 100.0);
    log.info("Monto prorrateado (15 días): ${:.2f}", (diferenciaDiaria * diasRestantes) / 100.0);

    log.info("\n=== FIN SIMULACIÓN ===");
  }

  /**
   * Convierte una entidad Suscripcion a SuscripcionResponseDTO con cálculos de
   * días.
   * Calcula los días transcurridos, totales y restantes del período actual.
   */
  private SuscripcionResponseDTO convertirConCalculoDias(Suscripcion suscripcion) {
    SuscripcionResponseDTO dto = suscripcionMapper.toResponseDTO(suscripcion);

    if (suscripcion.getFechaInicio() != null && suscripcion.getFechaRenovacion() != null) {
      LocalDate fechaInicio = suscripcion.getFechaInicio();
      LocalDate fechaRenovacion = suscripcion.getFechaRenovacion();
      LocalDate fechaActual = LocalDate.now();

      // Calcular días totales del período
      int diasTotales = (int) fechaInicio.until(fechaRenovacion).getDays();

      // Verificar que el período sea válido (evita valores negativos o cero)
      if (diasTotales <= 0) {
        diasTotales = 1; // Valor mínimo seguro para evitar división por cero
      }

      // Calcular días transcurridos desde el inicio
      int diasTranscurridos = (int) fechaInicio.until(fechaActual).getDays();

      // Asegurar que los días transcurridos no excedan el total
      if (diasTranscurridos > diasTotales) {
        diasTranscurridos = diasTotales;
      }

      // Si la fecha actual es anterior al inicio, los días transcurridos son 0
      if (diasTranscurridos < 0) {
        diasTranscurridos = 0;
      }

      // Calcular días restantes
      int diasRestantes = diasTotales - diasTranscurridos;
      if (diasRestantes < 0) {
        diasRestantes = 0;
      }

      dto.setDiasTranscurridos(diasTranscurridos);
      dto.setDiasTotales(diasTotales);
      dto.setDiasRestantes(diasRestantes);
    } else {
      // Si no hay fechas, establecer valores por defecto seguros
      // Para evitar división por cero en el frontend, establecemos un valor mínimo
      dto.setDiasTranscurridos(0);
      dto.setDiasTotales(1); // Evita división por cero
      dto.setDiasRestantes(0);
    }

    return dto;
  }
}