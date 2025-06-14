package com.biblioteca.service.impl;

import com.biblioteca.dto.PerfilRequestDTO;
import com.biblioteca.dto.PerfilResponseDTO;
import com.biblioteca.dto.comercial.PlanBeneficioResponseDTO;
import com.biblioteca.dto.comercial.SuscripcionResponseDTO;
import com.biblioteca.exceptions.OperacionNoPermitidaException;
import com.biblioteca.exceptions.RecursoNoEncontradoException;
import com.biblioteca.mapper.PerfilMapper;
import com.biblioteca.models.acceso.InformacionPerfil;
import com.biblioteca.models.acceso.Perfil;
import com.biblioteca.models.acceso.Usuario;
import com.biblioteca.repositories.InformacionPerfilRepository;
import com.biblioteca.repositories.PerfilRepository;
import com.biblioteca.service.PerfilService;
import com.biblioteca.service.PlanBeneficioService;
import com.biblioteca.service.SuscripcionService;
import com.biblioteca.service.UsuarioService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerfilServiceImpl implements PerfilService {

  private final PerfilRepository perfilRepository; // Inyectar Repositorio
  private final PerfilMapper perfilMapper;
  private final UsuarioService usuarioService; // UsuarioService para obtener y actualizar Usuario
  private final SuscripcionService suscripcionService;
  private final PlanBeneficioService planBeneficioService;
  private final InformacionPerfilRepository informacionPerfilRepository;

  @Override
  @Transactional
  public PerfilResponseDTO crearPerfil(PerfilRequestDTO perfilDto, String username) {
    Usuario usuario = usuarioService.buscarPorUsername(username)
        .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));

    // VALIDAR LÍMITE DE PERFILES SEGÚN EL PLAN
    validarLimitePerfiles(usuario);

    // VALIDAR LÍMITE DE PRÉSTAMOS SIMULTÁNEOS SEGÚN EL PLAN
    validarLimitePrestamosPorPlan(usuario, perfilDto.getLimitePrestamosDesignado());

    boolean esPrimerPerfil = perfilRepository.countByUsuarioUsername(username) == 0;

    if (esPrimerPerfil) {
      perfilDto.setEsPerfilPrincipal(true);
    } else if (perfilDto.getEsPerfilPrincipal()) {
      // Si se marca como principal y ya existen otros, desmarcar los demás
      desmarcarOtrosPerfilesPrincipales(usuario, null);
    }

    Perfil perfil = perfilMapper.perfilRequestDTOToPerfil(perfilDto, usuario);
    // El ID será generado por la base de datos
    perfil.setUsuario(usuario); // Asegurar que la relación esté establecida

    LocalDate ahora = LocalDate.now();
    perfil.setFechaCreacion(ahora);
    perfil.setFechaModificacion(ahora);
    perfil.setUltimaActividad(LocalDateTime.now());
    perfil.setActivo(true); // Por defecto activo al crear

    Perfil perfilGuardado = perfilRepository.save(perfil);

    if (perfilGuardado.getEsPerfilPrincipal()) {
      usuario.setPerfilPrincipal(perfilGuardado);
      usuarioService.actualizarUsuario(usuario); // Guardar el cambio en Usuario
    }

    return perfilMapper.perfilToPerfilResponseDTO(perfilGuardado);
  }

  private void desmarcarOtrosPerfilesPrincipales(Usuario usuario, Long perfilIdExcluir) {
    List<Perfil> perfilesPrincipalesAnteriores = perfilRepository.findByUsuarioAndEsPerfilPrincipalTrue(usuario);
    for (Perfil p : perfilesPrincipalesAnteriores) {
      if (perfilIdExcluir == null || !p.getId().equals(perfilIdExcluir)) {
        p.setEsPerfilPrincipal(false);
        perfilRepository.save(p);
      }
    }
    // Si se está estableciendo un nuevo perfil principal (perfilIdExcluir != null),
    // el campo usuario.perfilPrincipal se actualizará después de guardar el nuevo
    // perfil principal.
    // Si se está desmarcando el único perfil principal (perfilIdExcluir == null y
    // no hay nuevo principal),
    // entonces usuario.perfilPrincipal debería ponerse a null.
    if (perfilIdExcluir == null && (perfilesPrincipalesAnteriores.size() > 0
        && perfilesPrincipalesAnteriores.stream().noneMatch(p -> p.getId().equals(perfilIdExcluir)))) {
      usuario.setPerfilPrincipal(null);
      // usuarioService.actualizarUsuario(usuario); // Se actualiza en el flujo
      // principal
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PerfilResponseDTO> obtenerPerfilPorIdYUsuario(Long id, String username) {
    return perfilRepository.findByIdAndUsuarioUsername(id, username)
        .map(perfilMapper::perfilToPerfilResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PerfilResponseDTO> obtenerPerfilesPorUsuario(String username) {
    return perfilRepository.findByUsuarioUsername(username).stream()
        .map(perfilMapper::perfilToPerfilResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Perfil> obtenerEntidadPerfilPorId(Long id) {
    return perfilRepository.findById(id);
  }

  @Override
  @Transactional
  public Optional<PerfilResponseDTO> actualizarPerfil(Long id, PerfilRequestDTO perfilDto, String username) {
    Usuario usuario = usuarioService.buscarPorUsername(username)
        .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));

    return perfilRepository.findByIdAndUsuarioUsername(id, usuario.getUsername())
        .map(perfil -> {
          // VALIDAR LÍMITE DE PRÉSTAMOS SIMULTÁNEOS SEGÚN EL PLAN (excluyendo el perfil
          // actual)
          validarLimitePrestamosPorPlan(usuario, perfilDto.getLimitePrestamosDesignado(), id);

          boolean estabaMarcadoComoPrincipal = perfil.getEsPerfilPrincipal();
          boolean seQuiereMarcarComoPrincipal = perfilDto.getEsPerfilPrincipal();

          perfilMapper.updatePerfilFromDto(perfilDto, perfil); // Actualiza campos básicos
          perfil.setEsPerfilPrincipal(seQuiereMarcarComoPrincipal); // Asegurar que el DTO mande
          perfil.setFechaModificacion(LocalDate.now());
          perfil.setUltimaActividad(LocalDateTime.now());

          if (seQuiereMarcarComoPrincipal && !estabaMarcadoComoPrincipal) {
            desmarcarOtrosPerfilesPrincipales(usuario, perfil.getId());
            usuario.setPerfilPrincipal(perfil);
          } else if (!seQuiereMarcarComoPrincipal && estabaMarcadoComoPrincipal) {
            // Si se desmarca el principal, y no hay otro marcado como principal en el DTO
            // (lo cual no debería pasar si la UI lo maneja bien),
            // se podría intentar asignar otro o dejarlo nulo.
            // Por ahora, si se desmarca, se asume que otro será marcado o no habrá
            // principal.
            if (usuario.getPerfilPrincipal() != null && usuario.getPerfilPrincipal().getId().equals(perfil.getId())) {
              usuario.setPerfilPrincipal(null); // O buscar otro para asignar
            }
          }

          Perfil perfilActualizado = perfilRepository.save(perfil);
          usuarioService.actualizarUsuario(usuario); // Guardar cambios en Usuario (perfilPrincipal)
          return perfilMapper.perfilToPerfilResponseDTO(perfilActualizado);
        });
  }

  @Override
  @Transactional
  public boolean eliminarPerfil(Long id, String username) {
    Usuario usuario = usuarioService.buscarPorUsername(username)
        .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));

    Optional<Perfil> perfilOpt = perfilRepository.findByIdAndUsuarioUsername(id, usuario.getUsername());

    if (perfilOpt.isPresent()) {
      Perfil perfilAEliminar = perfilOpt.get();

      if (perfilAEliminar.getEsPerfilPrincipal() && perfilRepository.countByUsuarioUsername(username) <= 1) {
        throw new OperacionNoPermitidaException("No se puede eliminar el único perfil principal.");
      }

      boolean eraPrincipal = perfilAEliminar.getEsPerfilPrincipal();
      perfilRepository.delete(perfilAEliminar); // Eliminar el perfil

      if (eraPrincipal) {
        // Si se eliminó el perfil principal, asignar otro como principal si existen más
        // perfiles
        Optional<Perfil> nuevoPrincipalOpt = perfilRepository
            .findFirstByUsuarioUsernameOrderByFechaCreacionAsc(username);
        if (nuevoPrincipalOpt.isPresent()) {
          Perfil nuevoPrincipal = nuevoPrincipalOpt.get();
          nuevoPrincipal.setEsPerfilPrincipal(true);
          perfilRepository.save(nuevoPrincipal);
          usuario.setPerfilPrincipal(nuevoPrincipal);
        } else {
          usuario.setPerfilPrincipal(null); // No quedan más perfiles
        }
      }
      usuarioService.actualizarUsuario(usuario);
      return true;
    }
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public long contarPerfilesPorUsuario(String username) {
    return perfilRepository.countByUsuarioUsername(username);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existePerfilPrincipal(String username) {
    return perfilRepository.existsByUsuarioUsernameAndEsPerfilPrincipalTrue(username);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PerfilResponseDTO> obtenerPerfilPrincipal(String username) {
    return perfilRepository.findByUsuarioUsernameAndEsPerfilPrincipalTrue(username)
        .map(perfilMapper::perfilToPerfilResponseDTO);
  }

  @Override
  @Transactional
  public void actualizarFechaUltimoActividad(Long perfilId) {
    try {
      // 1. VERIFICAR QUE EL PERFIL EXISTE
      Optional<Perfil> perfilOpt = perfilRepository.findById(perfilId);
      if (perfilOpt.isEmpty()) {
        log.warn("No se encontró el perfil con ID: {}", perfilId);
        return;
      }

      Perfil perfil = perfilOpt.get();

      // 2. VERIFICAR QUE EL PERFIL ESTÉ ACTIVO
      if (!perfil.esPerfilActivo()) {
        log.warn("Intento de activar perfil inactivo: {}", perfilId);
        return;
      }

      // 3. ACTUALIZAR ACTIVIDAD DEL PERFIL
      perfil.registrarUso();

      // 4. OBTENER O CREAR INFORMACIÓN DEL PERFIL DE FORMA SEGURA
      InformacionPerfil informacionPerfil = perfil.getInformacionPerfil();

      if (informacionPerfil == null) {
        // Crear nueva información de perfil con todos los campos requeridos
        informacionPerfil = InformacionPerfil.builder()
            .perfil(perfil) // Establecer la relación
            .fechaCreacion(LocalDateTime.now())
            .ultimaActividad(LocalDateTime.now())
            .ultimaActualizacion(LocalDateTime.now())
            // Valores por defecto para evitar nulls
            .nivelLectura(com.biblioteca.enums.NivelLector.PRINCIPIANTE)
            .formatoPreferido("AMBOS")
            .idiomaLectura("ES")
            .mostrarProgreso(true)
            .aceptarInvitacionesGrupos(true)
            .algoritmoRecomendacionesActivo(true)
            .tipoRecomendaciones("PERSONALIZADO")
            .totalLibrosLeidos(0)
            .totalPrestamoRealizados(0)
            .totalResenasEscritas(0)
            .totalRecomendacionesHechas(0)
            .puntuacionComunidad(0)
            .tiempoLecturaMinutos(0)
            .librosLeidosMesActual(0)
            .librosLeidosAnioActual(0)
            .metaLibrosMes(0)
            .metaLibrosAnio(0)
            .build();

        // Establecer la relación bidireccional
        perfil.setInformacionPerfil(informacionPerfil);
      } else {
        // Actualizar información existente
        informacionPerfil.actualizarUltimaActividad();
      }

      // 5. GUARDAR EN UNA SOLA TRANSACCIÓN
      // Primero guardar el perfil (asegura que existe)
      Perfil perfilGuardado = perfilRepository.save(perfil);

      // Luego guardar la información del perfil si es nueva
      if (perfilGuardado.getInformacionPerfil() != null &&
          perfilGuardado.getInformacionPerfil().getId() == null) {
        // Es una nueva información de perfil, asegurar que tenga el ID correcto
        perfilGuardado.getInformacionPerfil().setId(perfilGuardado.getId());
      }

      log.debug("Actividad actualizada exitosamente para perfil ID: {} ({})",
          perfilId, perfil.getNombreVisible());

    } catch (Exception e) {
      log.error("Error al actualizar actividad del perfil {}: {}",
          perfilId, e.getMessage(), e);
      throw new RuntimeException("Error al actualizar actividad del perfil", e);
    }
  }

  /**
   * Valida que el usuario no exceda el límite de perfiles permitido por su plan
   * de suscripción
   */
  private void validarLimitePerfiles(Usuario usuario) {
    // Obtener suscripción activa del usuario
    Optional<SuscripcionResponseDTO> suscripcionActivaOpt = suscripcionService
        .obtenerSuscripcionActivaPorUsuario(usuario.getId());

    if (suscripcionActivaOpt.isEmpty()) {
      // Sin suscripción activa, aplicar límite por defecto (1 perfil)
      long perfilesExistentes = perfilRepository.countByUsuarioUsername(usuario.getUsername());
      if (perfilesExistentes >= 1) {
        throw new OperacionNoPermitidaException(
            "Sin suscripción activa, solo se permite 1 perfil. Active una suscripción para crear más perfiles.");
      }
      return;
    }

    SuscripcionResponseDTO suscripcion = suscripcionActivaOpt.get();
    Long planId = suscripcion.getPlanId();

    // Obtener el beneficio "Número de perfiles" (ID 11) para el plan actual
    Optional<PlanBeneficioResponseDTO> beneficioPerfilesOpt = planBeneficioService
        .obtenerAsociacionPorPlanIdYBeneficioId(planId, 11L);

    if (beneficioPerfilesOpt.isEmpty()) {
      // Si no hay beneficio definido, aplicar límite por defecto
      long perfilesExistentes = perfilRepository.countByUsuarioUsername(usuario.getUsername());
      if (perfilesExistentes >= 1) {
        throw new OperacionNoPermitidaException(
            "Su plan actual no tiene configurado el límite de perfiles. Solo se permite 1 perfil.");
      }
      return;
    }

    PlanBeneficioResponseDTO beneficioPerfiles = beneficioPerfilesOpt.get();
    int limitePerfiles;
    try {
      limitePerfiles = Integer.parseInt(beneficioPerfiles.getValor());
    } catch (NumberFormatException e) {
      limitePerfiles = 1; // Valor por defecto si no se puede parsear
    }

    // Verificar si el usuario ya alcanzó el límite
    long perfilesExistentes = perfilRepository.countByUsuarioUsername(usuario.getUsername());
    if (perfilesExistentes >= limitePerfiles) {
      throw new OperacionNoPermitidaException(
          String.format(
              "Ha alcanzado el límite máximo de %d perfil(es) para su plan actual. Actualice su plan para crear más perfiles.",
              limitePerfiles));
    }
  }

  /**
   * Valida que el límite de préstamos simultáneos no exceda lo permitido por el
   * plan
   * y que no sobrepase los préstamos disponibles restantes
   */
  private void validarLimitePrestamosPorPlan(Usuario usuario, Integer limitePrestamosDesignado) {
    validarLimitePrestamosPorPlan(usuario, limitePrestamosDesignado, null);
  }

  /**
   * Valida que el límite de préstamos simultáneos no exceda lo permitido por el
   * plan
   * y que no sobrepase los préstamos disponibles restantes
   */
  private void validarLimitePrestamosPorPlan(Usuario usuario, Integer limitePrestamosDesignado, Long perfilIdExcluir) {
    if (limitePrestamosDesignado == null || limitePrestamosDesignado <= 0) {
      return; // Si no se especifica límite o es 0, no validar
    }

    // Calcular préstamos disponibles
    int prestamosDisponibles = calcularPrestamosDisponibles(usuario, perfilIdExcluir);

    // Verificar si el límite designado excede los préstamos disponibles
    if (limitePrestamosDesignado > prestamosDisponibles) {
      int limiteTotalPlan = obtenerLimitePrestamosDelPlan(usuario);
      int prestamosAsignados = limiteTotalPlan - prestamosDisponibles;

      throw new OperacionNoPermitidaException(
          String.format(
              "El límite de préstamos (%d) excede los disponibles (%d). Su plan permite %d préstamos total, ya tiene %d asignados.",
              limitePrestamosDesignado, prestamosDisponibles, limiteTotalPlan, prestamosAsignados));
    }
  }

  /**
   * Calcula los préstamos disponibles para asignar a un nuevo perfil o al editar
   * uno existente
   */
  private int calcularPrestamosDisponibles(Usuario usuario, Long perfilIdExcluir) {
    // Obtener límite total del plan
    int limiteTotalPlan = obtenerLimitePrestamosDelPlan(usuario);

    // Calcular préstamos ya asignados (excluyendo el perfil actual si se está
    // editando)
    int prestamosAsignados = perfilRepository.findByUsuarioUsername(usuario.getUsername())
        .stream()
        .filter(perfil -> perfilIdExcluir == null || !perfil.getId().equals(perfilIdExcluir))
        .mapToInt(Perfil::getLimitePrestamosDesignado)
        .sum();

    return limiteTotalPlan - prestamosAsignados;
  }

  /**
   * Obtiene el límite de préstamos simultáneos del plan del usuario
   */
  private int obtenerLimitePrestamosDelPlan(Usuario usuario) {
    // Obtener suscripción activa del usuario
    Optional<SuscripcionResponseDTO> suscripcionActivaOpt = suscripcionService
        .obtenerSuscripcionActivaPorUsuario(usuario.getId());

    if (suscripcionActivaOpt.isEmpty()) {
      return 2; // Sin suscripción activa, límite por defecto
    }

    SuscripcionResponseDTO suscripcion = suscripcionActivaOpt.get();
    Long planId = suscripcion.getPlanId();

    // Obtener el beneficio "Préstamos simultáneos" (ID 2) para el plan actual
    Optional<PlanBeneficioResponseDTO> beneficioPrestamosOpt = planBeneficioService
        .obtenerAsociacionPorPlanIdYBeneficioId(planId, 2L);

    if (beneficioPrestamosOpt.isEmpty()) {
      return 2; // Si no hay beneficio definido, límite por defecto
    }

    try {
      return Integer.parseInt(beneficioPrestamosOpt.get().getValor());
    } catch (NumberFormatException e) {
      return 2; // Valor por defecto si no se puede parsear
    }
  }
}