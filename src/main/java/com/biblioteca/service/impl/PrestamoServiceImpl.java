package com.biblioteca.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.dto.actividades.PrestamoRequestDTO;
import com.biblioteca.dto.actividades.PrestamoResponseDTO;
import com.biblioteca.dto.comercial.PlanBeneficioResponseDTO;
import com.biblioteca.dto.comercial.SuscripcionResponseDTO;
import com.biblioteca.dto.validacion.prestamos.ValidacionLimitesResult;
import com.biblioteca.dto.validacion.prestamos.ValidacionRenovacionResult;
import com.biblioteca.enums.EstadoPrestamo;
import com.biblioteca.events.prestamos.PrestamoVencidoEvent;
import com.biblioteca.events.prestamos.PrestamoRealizadoEvent;
import com.biblioteca.events.prestamos.PrestamoRenovadoEvent;
import com.biblioteca.events.prestamos.PrestamoDevueltoEvent;
import com.biblioteca.exceptions.OperacionNoPermitidaException;
import com.biblioteca.exceptions.RecursoNoEncontradoException;
import com.biblioteca.mapper.actividades.PrestamosMapper;
import com.biblioteca.models.actividades.Prestamo;
import com.biblioteca.models.acceso.Perfil;
import com.biblioteca.models.contenido.Contenido;
import com.biblioteca.repositories.actividades.PrestamoRepository;
import com.biblioteca.service.ContenidoService;
import com.biblioteca.service.PerfilService;
import com.biblioteca.service.PlanBeneficioService;
import com.biblioteca.service.PrestamoService;
import com.biblioteca.service.SuscripcionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PrestamoServiceImpl implements PrestamoService {

  private final PrestamoRepository prestamoRepository;
  private final PrestamosMapper prestamosMapper;
  private final PerfilService perfilService;
  private final ContenidoService contenidoService;
  private final SuscripcionService suscripcionService;
  private final PlanBeneficioService planBeneficioService;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public PrestamoResponseDTO crearPrestamo(PrestamoRequestDTO prestamoRequest) {
    log.info("Creando préstamo para perfil: {} y contenido: {}",
        prestamoRequest.getPerfilId(), prestamoRequest.getContenidoId());

    // 1. VALIDACIONES BÁSICAS
    validarDatosBasicos(prestamoRequest);

    // 2. OBTENER ENTIDADES
    Perfil perfil = obtenerPerfilValidado(prestamoRequest.getPerfilId());
    Contenido contenido = obtenerContenidoValidado(prestamoRequest.getContenidoId());

    // 3. VALIDACIONES DE NEGOCIO
    validarDisponibilidadContenido(contenido);
    validarPerfilActivo(perfil);
    validarContenidoNoDuplicado(perfil.getId(), contenido.getId());

    // 4. VALIDACIONES DE LÍMITES DE PLAN
    ValidacionLimitesResult validacion = validarLimitesPrestamos(perfil);

    if (!validacion.getPuedePrestar()) {
      throw new OperacionNoPermitidaException(validacion.getMensaje());
    }

    // 5. CREAR PRÉSTAMO
    Prestamo prestamo = prestamosMapper.toEntity(prestamoRequest);
    prestamo.setContenido(contenido);
    prestamo.setPerfil(perfil);
    prestamo.setEstado(EstadoPrestamo.ACTIVO);

    // Establecer fechas por defecto si no se proporcionan
    if (prestamo.getFechaPrestamo() == null) {
      prestamo.setFechaPrestamo(LocalDate.now());
    }
    if (prestamo.getFechaDevolucionPrevista() == null) {
      prestamo.setFechaDevolucionPrevista(calcularFechaDevolucion(perfil, contenido));
    }

    Prestamo prestamoGuardado = prestamoRepository.save(prestamo);

    // 6. PUBLICAR EVENTO
    eventPublisher.publishEvent(new PrestamoRealizadoEvent(this, prestamoGuardado, validacion));

    log.info("Préstamo creado exitosamente con ID: {}", prestamoGuardado.getId());

    return prestamosMapper.toResponseDTO(prestamoGuardado);
  }

  @Override
  @Transactional(readOnly = true)
  public PrestamoResponseDTO obtenerPrestamoPorId(Long id) {
    Prestamo prestamo = prestamoRepository.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Préstamo no encontrado con ID: " + id));

    return prestamosMapper.toResponseDTO(prestamo);
  }

  @Override
  public PrestamoResponseDTO actualizarPrestamo(Long id, PrestamoRequestDTO prestamoRequest) {
    Prestamo prestamo = prestamoRepository.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Préstamo no encontrado con ID: " + id));

    // Validar que el préstamo se puede actualizar
    if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO ||
        prestamo.getEstado() == EstadoPrestamo.CANCELADO) {
      throw new OperacionNoPermitidaException(
          "No se puede actualizar un préstamo " + prestamo.getEstado().name().toLowerCase());
    }

    prestamosMapper.updateEntityFromDTO(prestamoRequest, prestamo);
    Prestamo prestamoActualizado = prestamoRepository.save(prestamo);

    return prestamosMapper.toResponseDTO(prestamoActualizado);
  }

  @Override
  public void eliminarPrestamo(Long id) {
    Prestamo prestamo = prestamoRepository.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Préstamo no encontrado con ID: " + id));

    if (prestamo.getEstado() == EstadoPrestamo.ACTIVO) {
      throw new OperacionNoPermitidaException("No se puede eliminar un préstamo activo. Debe devolverlo primero.");
    }

    prestamoRepository.delete(prestamo);
    log.info("Préstamo eliminado: {}", id);
  }

  // ========== CONSULTAS BÁSICAS ==========

  @Override
  @Transactional(readOnly = true)
  public Page<PrestamoResponseDTO> obtenerTodosPrestamos(Pageable pageable) {
    return prestamoRepository.findAll(pageable)
        .map(prestamosMapper::toResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PrestamoResponseDTO> obtenerPrestamosPorEstado(EstadoPrestamo estado) {
    return prestamoRepository.findByEstado(estado)
        .stream()
        .map(prestamosMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Page<PrestamoResponseDTO> obtenerPrestamosPorEstado(EstadoPrestamo estado, Pageable pageable) {
    return prestamoRepository.findByEstado(estado, pageable)
        .map(prestamosMapper::toResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PrestamoResponseDTO> obtenerPrestamosPorEstados(List<EstadoPrestamo> estados) {
    return prestamoRepository.findByEstadoIn(estados)
        .stream()
        .map(prestamosMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<PrestamoResponseDTO> obtenerPrestamosPorPerfil(Long perfilId) {
    return prestamoRepository.findByPerfilId(perfilId)
        .stream()
        .map(prestamosMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Page<PrestamoResponseDTO> obtenerPrestamosPorPerfil(Long perfilId, Pageable pageable) {
    return prestamoRepository.findByPerfilId(perfilId, pageable)
        .map(prestamosMapper::toResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PrestamoResponseDTO> obtenerPrestamosActivosPorPerfil(Long perfilId) {
    return prestamoRepository.findPrestamosActivosByPerfil(perfilId)
        .stream()
        .map(prestamosMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PrestamoResponseDTO> obtenerPrestamosPorPerfilYEstado(Long perfilId, EstadoPrestamo estadoEnum,
      Pageable pageable) {
    return prestamoRepository.findByPerfilIdAndEstado(perfilId, estadoEnum, pageable)
        .map(prestamosMapper::toResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PrestamoResponseDTO> obtenerHistorialPrestamosPerfil(Long perfilId, Pageable pageable) {
    return prestamoRepository.findHistorialPrestamosByPerfil(perfilId, pageable)
        .map(prestamosMapper::toResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public Long contarPrestamosActivosPorPerfil(Long perfilId) {
    return prestamoRepository.countPrestamosActivosByPerfil(perfilId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PrestamoResponseDTO> obtenerPrestamosPorContenido(Long contenidoId) {
    return prestamoRepository.findByContenidoId(contenidoId)
        .stream()
        .map(prestamosMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public boolean verificarContenidoPrestado(Long contenidoId) {
    return prestamoRepository.isContenidoPrestado(contenidoId);
  }

  @Override
  @Transactional(readOnly = true)
  public PrestamoResponseDTO obtenerUltimoPrestamoContenido(Long contenidoId) {
    List<Prestamo> prestamos = prestamoRepository.findUltimosPrestamosByContenido(
        contenidoId, PageRequest.of(0, 1));

    if (prestamos.isEmpty()) {
      throw new RecursoNoEncontradoException("No se encontraron préstamos para el contenido: " + contenidoId);
    }

    return prestamosMapper.toResponseDTO(prestamos.get(0));
  }

  // ========== OPERACIONES DE GESTIÓN ==========

  @Override
  public PrestamoResponseDTO devolverPrestamo(Long prestamoId) {
    Prestamo prestamo = prestamoRepository.findById(prestamoId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Préstamo no encontrado con ID: " + prestamoId));

    if (prestamo.getEstado() != EstadoPrestamo.ACTIVO && prestamo.getEstado() != EstadoPrestamo.VENCIDO) {
      throw new OperacionNoPermitidaException("Solo se pueden devolver préstamos activos o vencidos");
    }

    prestamo.setEstado(EstadoPrestamo.DEVUELTO);
    prestamo.setFechaDevolucionReal(LocalDate.now());

    Prestamo prestamoDevuelto = prestamoRepository.save(prestamo);

    // Publicar evento de devolución
    eventPublisher.publishEvent(new PrestamoDevueltoEvent(this, prestamoDevuelto));

    log.info("Préstamo devuelto: {}", prestamoId);
    return prestamosMapper.toResponseDTO(prestamoDevuelto);
  }

  @Override
  public PrestamoResponseDTO renovarPrestamo(Long prestamoId, Integer diasExtension) {
    Prestamo prestamo = prestamoRepository.findById(prestamoId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Préstamo no encontrado con ID: " + prestamoId));

    if (prestamo.getEstado() != EstadoPrestamo.ACTIVO) {
      throw new OperacionNoPermitidaException("Solo se pueden renovar préstamos activos");
    }

    // Validar renovación según el plan
    ValidacionRenovacionResult validacion = validarRenovacion(prestamo, diasExtension);
    if (!validacion.getPuedeRenovar()) {
      throw new OperacionNoPermitidaException(validacion.getMensaje());
    }

    // Aplicar la renovación
    prestamo.setEstado(EstadoPrestamo.RENOVADO);
    prestamo
        .setFechaDevolucionPrevista(
            prestamo.getFechaDevolucionPrevista().plusDays(validacion.getDiasExtensionPermitidosSafe()));

    String observacion = String.format("Renovado por %d días. %s",
        validacion.getDiasExtensionPermitidosSafe(),
        validacion.getObservaciones() != null ? validacion.getObservaciones() : "");
    prestamo.setObservaciones(observacion);

    Prestamo prestamoRenovado = prestamoRepository.save(prestamo);

    // Publicar evento de renovación
    eventPublisher.publishEvent(new PrestamoRenovadoEvent(this, prestamoRenovado, validacion));

    log.info("Préstamo renovado: {} por {} días", prestamoId, validacion.getDiasExtensionPermitidosSafe());
    return prestamosMapper.toResponseDTO(prestamoRenovado);
  }

  @Override
  public PrestamoResponseDTO marcarComoPerdido(Long prestamoId, String observaciones) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'marcarComoPerdido'");
  }

  @Override
  public PrestamoResponseDTO cancelarPrestamo(Long prestamoId, String motivo) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'cancelarPrestamo'");
  }

  @Override
  public PrestamoResponseDTO cambiarEstadoPrestamo(Long prestamoId, EstadoPrestamo nuevoEstado, String observaciones) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'cambiarEstadoPrestamo'");
  }

  @Override
  @Transactional(readOnly = true)
  public List<PrestamoResponseDTO> obtenerPrestamosVencidos() {
    return prestamoRepository.findPrestamosVencidos(EstadoPrestamo.ACTIVO, LocalDate.now())
        .stream()
        .map(prestamosMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<PrestamoResponseDTO> obtenerPrestamosProximosAVencer(Integer diasAnticipacion) {
    LocalDate fechaLimite = LocalDate.now().plusDays(diasAnticipacion);
    return prestamoRepository.findPrestamosProximosAVencer(
        EstadoPrestamo.ACTIVO, LocalDate.now(), fechaLimite)
        .stream()
        .map(prestamosMapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  // ========== PROCESAMIENTO AUTOMÁTICO ==========

  @Override
  public void procesarVencimientosAutomaticos() {
    log.info("Iniciando procesamiento de vencimientos automáticos");

    List<Prestamo> prestamosVencidos = prestamoRepository.findPrestamosVencidos(
        EstadoPrestamo.ACTIVO, LocalDate.now());

    for (Prestamo prestamo : prestamosVencidos) {
      try {
        prestamo.setEstado(EstadoPrestamo.VENCIDO);
        prestamoRepository.save(prestamo);

        // Publicar evento de vencimiento
        eventPublisher.publishEvent(new PrestamoVencidoEvent(this, prestamo));

        log.debug("Préstamo {} marcado como vencido", prestamo.getId());
      } catch (Exception e) {
        log.error("Error procesando vencimiento del préstamo {}: {}", prestamo.getId(), e.getMessage());
      }
    }

    log.info("Procesamiento de vencimientos completado. {} préstamos procesados", prestamosVencidos.size());
  }

  @Override
  public List<PrestamoResponseDTO> obtenerPrestamosParaNotificacionVencimiento(LocalDate fechaVencimiento) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'obtenerPrestamosParaNotificacionVencimiento'");
  }

  @Override
  @Transactional(readOnly = true)
  public boolean puedeRealizarPrestamo(Long perfilId) {
    try {
      Perfil perfil = perfilService.obtenerEntidadPerfilPorId(perfilId)
          .orElseThrow(() -> new RecursoNoEncontradoException("Perfil no encontrado: " + perfilId));

      ValidacionLimitesResult validacion = validarLimitesPrestamos(perfil);
      return validacion.getPuedePrestar();

    } catch (Exception e) {
      log.error("Error verificando si perfil {} puede prestar: {}", perfilId, e.getMessage());
      return false;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PrestamoResponseDTO> obtenerPrestamoActivoPorPerfilYContenido(Long perfilId, Long contenidoId) {
    Optional<Prestamo> prestamoOpt = prestamoRepository.findByPerfilIdAndContenidoIdAndEstado(
        perfilId, contenidoId, EstadoPrestamo.ACTIVO);

    return prestamoOpt.map(prestamosMapper::toResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean perfilTieneContenidoPrestado(Long perfilId, Long contenidoId) {
    return prestamoRepository.existePrestamoActivoPerfilContenido(perfilId, contenidoId);
  }

  @Override
  @Transactional(readOnly = true)
  public Integer obtenerLimitePrestamos(Long perfilId) {
    try {
      Perfil perfil = perfilService.obtenerEntidadPerfilPorId(perfilId)
          .orElseThrow(() -> new RecursoNoEncontradoException("Perfil no encontrado: " + perfilId));

      return perfil.getLimitePrestamosDesignado();

    } catch (Exception e) {
      log.error("Error obteniendo límite de préstamos para perfil {}: {}", perfilId, e.getMessage());
      return 0;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public boolean verificarDisponibilidadContenido(Long contenidoId) {
    try {
      Contenido contenido = contenidoService.obtenerEntidadContenidoPorId(contenidoId)
          .orElse(null);

      if (contenido == null) {
        return false;
      }

      return contenido.isActivo() && contenido.isPuedeSerPrestado();

    } catch (Exception e) {
      log.error("Error verificando disponibilidad del contenido {}: {}", contenidoId, e.getMessage());
      return false;
    }
  }

  @Override
  public List<PrestamoResponseDTO> obtenerPrestamosPorFecha(LocalDate fecha) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'obtenerPrestamosPorFecha'");
  }

  @Override
  public List<PrestamoResponseDTO> obtenerPrestamosEnRango(LocalDate fechaInicio, LocalDate fechaFin) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'obtenerPrestamosEnRango'");
  }

  @Override
  public List<PrestamoResponseDTO> obtenerPrestamosConDevolucionEn(LocalDate fecha) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'obtenerPrestamosConDevolucionEn'");
  }

  @Override
  public Page<PrestamoResponseDTO> buscarPrestamosConFiltros(Long perfilId, Long contenidoId, EstadoPrestamo estado,
      LocalDate fechaInicio, LocalDate fechaFin, Boolean soloVencidos, Pageable pageable) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'buscarPrestamosConFiltros'");
  }

  @Override
  public List<PrestamoResponseDTO> buscarPrestamosPorTituloObra(String titulo) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'buscarPrestamosPorTituloObra'");
  }

  @Override
  public List<PrestamoResponseDTO> buscarPrestamosPorNombreUsuario(String nombreUsuario) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'buscarPrestamosPorNombreUsuario'");
  }

  @Override
  public List<PrestamoResponseDTO> obtenerPrestamosParaRecordatorio() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'obtenerPrestamosParaRecordatorio'");
  }

  @Override
  public void marcarNotificacionEnviada(Long prestamoId, String tipoNotificacion) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'marcarNotificacionEnviada'");
  }

  @Override
  public List<PrestamoResponseDTO> obtenerPrestamosParaArchivado(LocalDate fechaLimite) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'obtenerPrestamosParaArchivado'");
  }

  @Override
  public void limpiarPrestamosAntiguos(LocalDate fechaLimite) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'limpiarPrestamosAntiguos'");
  }

  @Override
  public void sincronizarEstadosPrestamos() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'sincronizarEstadosPrestamos'");
  }

  @Override
  public Long obtenerTotalPrestamosActivos() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'obtenerTotalPrestamosActivos'");
  }

  @Override
  public Long obtenerTotalPrestamosVencidos() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'obtenerTotalPrestamosVencidos'");
  }

  @Override
  public Long obtenerTotalPrestamosDelDia() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'obtenerTotalPrestamosDelDia'");
  }

  @Override
  public Object obtenerEstadisticasBasicas() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'obtenerEstadisticasBasicas'");
  }

  @Override
  public void procesarRenovacionesAutomaticas() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'procesarRenovacionesAutomaticas'");
  }

  @Override
  public List<PrestamoResponseDTO> procesarDevolucionesMasivas(List<Long> prestamosIds) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'procesarDevolucionesMasivas'");
  }

  @Override
  public void actualizarEstadosMasivamente(List<Long> prestamosIds, EstadoPrestamo nuevoEstado) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'actualizarEstadosMasivamente'");
  }

  // ========== VALIDACIONES DE LÍMITES DE PLAN ==========
  /**
   * Valida si un perfil puede realizar más préstamos según su plan y
   * configuración
   */
  private ValidacionLimitesResult validarLimitesPrestamos(Perfil perfil) {
    try {
      // 1. Obtener límite designado al perfil
      int limitePerfil = perfil.getLimitePrestamosDesignado();

      // 2. Contar préstamos activos actuales
      long prestamosActivos = prestamoRepository.countPrestamosActivosByPerfil(perfil.getId());

      // 3. Verificar si ya alcanzó su límite personal
      if (prestamosActivos >= limitePerfil) {
        // 4. Obtener límite máximo del plan del usuario
        int limiteMaximoPlan = obtenerLimitePrestamosDelPlan(perfil.getUsuario().getId());

        // 5. Determinar si puede aumentar el límite del perfil o necesita upgrade de
        // plan
        if (limitePerfil < limiteMaximoPlan) {
          // Puede aumentar el límite del perfil
          return ValidacionLimitesResult.builder()
              .puedePrestar(false)
              .puedeAumentarLimitePerfil(true)
              .necesitaUpgradePlan(false)
              .limiteActual(limitePerfil)
              .limiteMaximo(limiteMaximoPlan)
              .prestamosActivos((int) prestamosActivos)
              .mensaje(String.format(
                  "Ha alcanzado su límite de préstamos (%d/%d). " +
                      "Puede aumentar el límite de este perfil hasta %d o devolver algún contenido.",
                  prestamosActivos, limitePerfil, limiteMaximoPlan))
              .build();
        } else {
          // Necesita upgrade de plan
          return ValidacionLimitesResult.builder()
              .puedePrestar(false)
              .puedeAumentarLimitePerfil(false)
              .necesitaUpgradePlan(true)
              .limiteActual(limitePerfil)
              .limiteMaximo(limiteMaximoPlan)
              .prestamosActivos((int) prestamosActivos)
              .mensaje(String.format(
                  "Ha alcanzado el límite máximo de préstamos de su plan (%d/%d). " +
                      "Actualice su plan para realizar más préstamos simultáneos o devuelva algún contenido.",
                  prestamosActivos, limitePerfil))
              .build();
        }
      }

      // 6. Puede prestar normalmente
      return ValidacionLimitesResult.builder()
          .puedePrestar(true)
          .puedeAumentarLimitePerfil(false)
          .necesitaUpgradePlan(false)
          .limiteActual(limitePerfil)
          .prestamosActivos((int) prestamosActivos)
          .mensaje("Puede realizar el préstamo")
          .build();

    } catch (Exception e) {
      log.error("Error validando límites de préstamos para perfil {}: {}", perfil.getId(), e.getMessage());
      throw new RuntimeException("Error al validar límites de préstamos", e);
    }
  }

  /**
   * Obtiene el límite máximo de préstamos simultáneos del plan del usuario
   */
  private int obtenerLimitePrestamosDelPlan(Long usuarioId) {
    try {
      // Obtener suscripción activa del usuario
      Optional<SuscripcionResponseDTO> suscripcionOpt = suscripcionService
          .obtenerSuscripcionActivaPorUsuario(usuarioId);

      if (suscripcionOpt.isEmpty()) {
        return 2; // Límite por defecto sin suscripción
      }

      SuscripcionResponseDTO suscripcion = suscripcionOpt.get();

      // Obtener beneficio de préstamos simultáneos (ID 2)
      Optional<PlanBeneficioResponseDTO> beneficioOpt = planBeneficioService
          .obtenerAsociacionPorPlanIdYBeneficioId(suscripcion.getPlanId(), 2L);

      if (beneficioOpt.isEmpty()) {
        return 2; // Límite por defecto si no hay beneficio definido
      }

      return Integer.parseInt(beneficioOpt.get().getValor());

    } catch (NumberFormatException e) {
      log.warn("Error parseando límite de préstamos del plan para usuario {}: {}", usuarioId, e.getMessage());
      return 2;
    } catch (Exception e) {
      log.error("Error obteniendo límite de préstamos del plan para usuario {}: {}", usuarioId, e.getMessage());
      return 2;
    }
  }

  // ========== MÉTODOS AUXILIARES ==========

  private void validarDatosBasicos(PrestamoRequestDTO request) {
    if (request.getPerfilId() == null) {
      throw new IllegalArgumentException("El ID del perfil es obligatorio");
    }
    if (request.getContenidoId() == null) {
      throw new IllegalArgumentException("El ID del contenido es obligatorio");
    }
  }

  private Perfil obtenerPerfilValidado(Long perfilId) {
    return perfilService.obtenerEntidadPerfilPorId(perfilId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Perfil no encontrado con ID: " + perfilId));
  }

  private Contenido obtenerContenidoValidado(Long contenidoId) {
    return contenidoService.obtenerEntidadContenidoPorId(contenidoId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Contenido no encontrado con ID: " + contenidoId));
  }

  private void validarDisponibilidadContenido(Contenido contenido) {
    if (!contenido.isPuedeSerPrestado()) {
      throw new OperacionNoPermitidaException("Este contenido no está disponible para préstamo");
    }
    if (!contenido.isActivo()) {
      throw new OperacionNoPermitidaException("Este contenido no está activo");
    }
  }

  private void validarPerfilActivo(Perfil perfil) {
    if (!perfil.getActivo()) {
      throw new OperacionNoPermitidaException("El perfil no está activo");
    }
  }

  private void validarContenidoNoDuplicado(Long perfilId, Long contenidoId) {
    if (prestamoRepository.existePrestamoActivoPerfilContenido(perfilId, contenidoId)) {
      throw new OperacionNoPermitidaException("Ya tiene este contenido en préstamo activo");
    }
  }

  private LocalDate calcularFechaDevolucion(Perfil perfil, Contenido contenido) {
    // Obtener días de préstamo según el plan (beneficio ID 3)
    try {
      Optional<SuscripcionResponseDTO> suscripcionOpt = suscripcionService
          .obtenerSuscripcionActivaPorUsuario(perfil.getUsuario().getId());

      if (suscripcionOpt.isPresent()) {
        Optional<PlanBeneficioResponseDTO> beneficioOpt = planBeneficioService
            .obtenerAsociacionPorPlanIdYBeneficioId(suscripcionOpt.get().getPlanId(), 3L);

        if (beneficioOpt.isPresent()) {
          int diasPrestamo = Integer.parseInt(beneficioOpt.get().getValor());
          return LocalDate.now().plusDays(diasPrestamo);
        }
      }
    } catch (Exception e) {
      log.warn("Error calculando fecha de devolución, usando valor por defecto: {}", e.getMessage());
    }

    return LocalDate.now().plusDays(14); // Por defecto 14 días
  }

  /**
   * Valida si un préstamo puede ser renovado según los beneficios del plan
   */
  private ValidacionRenovacionResult validarRenovacion(Prestamo prestamo, Integer diasExtensionSolicitados) {
    try {
      Long usuarioId = prestamo.getPerfil().getUsuario().getId();

      // 1. Obtener beneficio de renovación de préstamos (ID 4)
      String tipoRenovacion = obtenerBeneficioPlan(usuarioId, 4L, "1"); // Default: 1 renovación

      // 2. Verificar si ya se renovó antes
      boolean yaRenovado = prestamo.getEstado() == EstadoPrestamo.RENOVADO ||
          (prestamo.getObservaciones() != null && prestamo.getObservaciones().contains("Renovado"));

      int renovacionesUsadas = contarRenovacionesPrevias(prestamo);
      // 3. Validar según el tipo de renovación del plan
      switch (tipoRenovacion.toLowerCase()) {
        case "0":
          return ValidacionRenovacionResult.builder()
              .puedeRenovar(false)
              .mensaje("Su plan no permite renovaciones de préstamos")
              .necesitaUpgradePlan(true)
              .build();

        case "1":
          if (yaRenovado) {
            return ValidacionRenovacionResult.builder()
                .puedeRenovar(false)
                .mensaje("Ya ha utilizado su renovación permitida para este préstamo")
                .necesitaUpgradePlan(true)
                .planRecomendado("Premium")
                .build();
          }
          break;

        case "2":
          if (renovacionesUsadas >= 2) {
            return ValidacionRenovacionResult.builder()
                .puedeRenovar(false)
                .mensaje("Ha alcanzado el máximo de 2 renovaciones para este préstamo")
                .necesitaUpgradePlan(true)
                .build();
          }
          break;

        case "automática":
        case "automatica":
        case "ilimitada":
          // Permitir renovación automática
          break;

        default:
          // Intentar parsear como número
          try {
            int maxRenovaciones = Integer.parseInt(tipoRenovacion);
            if (renovacionesUsadas >= maxRenovaciones) {
              return ValidacionRenovacionResult.builder()
                  .puedeRenovar(false)
                  .mensaje(
                      String.format("Ha alcanzado el máximo de %d renovaciones para este préstamo", maxRenovaciones))
                  .necesitaUpgradePlan(true)
                  .build();
            }
          } catch (NumberFormatException e) {
            log.warn("Valor de renovación no válido para usuario {}: {}", usuarioId, tipoRenovacion);
          }
      }

      // 4. Calcular días de extensión permitidos
      int diasPermitidos = calcularDiasExtensionPermitidos(usuarioId, diasExtensionSolicitados);

      return ValidacionRenovacionResult.builder()
          .puedeRenovar(true)
          .diasExtensionPermitidos(diasPermitidos)
          .tipoRenovacion(tipoRenovacion)
          .mensaje(String.format("Renovación aprobada por %d días", diasPermitidos))
          .observaciones(diasPermitidos < diasExtensionSolicitados
              ? String.format("Días solicitados: %d, días otorgados según plan: %d",
                  diasExtensionSolicitados, diasPermitidos)
              : null)
          .build();

    } catch (Exception e) {
      log.error("Error validando renovación para préstamo {}: {}", prestamo.getId(), e.getMessage());
      return ValidacionRenovacionResult.builder()
          .puedeRenovar(false)
          .mensaje("Error validando renovación. Contacte soporte.")
          .build();
    }
  }

  /**
   * Calcula los días de extensión permitidos según el plan
   */
  private int calcularDiasExtensionPermitidos(Long usuarioId, Integer diasSolicitados) {
    try {
      // Obtener período de préstamo base del plan (ID 3)
      String periodoBase = obtenerBeneficioPlan(usuarioId, 3L, "14");
      int diasBase = Integer.parseInt(periodoBase);

      // Para renovaciones, típicamente se otorga el mismo período base
      // o un porcentaje del mismo
      int diasMaximoRenovacion = diasBase;

      // Si el plan es Premium o superior, permitir hasta el doble
      String catalogo = obtenerBeneficioPlan(usuarioId, 1L, "100");
      if (catalogo.contains("5000+") || catalogo.contains("100000+")) {
        diasMaximoRenovacion = diasBase; // Mantener el período base
      }

      return Math.min(diasSolicitados != null ? diasSolicitados : diasMaximoRenovacion,
          diasMaximoRenovacion);

    } catch (Exception e) {
      log.warn("Error calculando días de extensión para usuario {}: {}", usuarioId, e.getMessage());
      return Math.min(diasSolicitados != null ? diasSolicitados : 14, 14); // Default 14 días
    }
  }

  /**
   * Cuenta las renovaciones previas de un préstamo
   */
  private int contarRenovacionesPrevias(Prestamo prestamo) {
    // Implementación simple: buscar en las observaciones
    if (prestamo.getObservaciones() == null) {
      return 0;
    }

    String observaciones = prestamo.getObservaciones().toLowerCase();
    int count = 0;
    int index = 0;
    while ((index = observaciones.indexOf("renovado", index)) != -1) {
      count++;
      index += "renovado".length();
    }

    return count;
  }

  /**
   * Obtiene un beneficio específico del plan del usuario
   */
  private String obtenerBeneficioPlan(Long usuarioId, Long beneficioId, String valorDefault) {
    try {
      Optional<SuscripcionResponseDTO> suscripcionOpt = suscripcionService
          .obtenerSuscripcionActivaPorUsuario(usuarioId);

      if (suscripcionOpt.isPresent()) {
        Optional<PlanBeneficioResponseDTO> beneficioOpt = planBeneficioService
            .obtenerAsociacionPorPlanIdYBeneficioId(suscripcionOpt.get().getPlanId(), beneficioId);

        if (beneficioOpt.isPresent()) {
          return beneficioOpt.get().getValor();
        }
      }

      return valorDefault;

    } catch (Exception e) {
      log.warn("Error obteniendo beneficio {} para usuario {}: {}", beneficioId, usuarioId, e.getMessage());
      return valorDefault;
    }
  }

}
