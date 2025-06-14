package com.biblioteca.service.impl;

import com.biblioteca.dto.ActividadRecienteDTO;
import com.biblioteca.dto.UsuarioAdminDTO;
import com.biblioteca.dto.UsuarioDataDTO;
import com.biblioteca.dto.UsuarioRegistroDTO;
import com.biblioteca.dto.comercial.SuscripcionRequestDTO;
import com.biblioteca.exceptions.OperacionNoPermitidaException;
import com.biblioteca.exceptions.RecursoNoEncontradoException;
import com.biblioteca.mapper.UsuarioMapper;
import com.biblioteca.models.acceso.Rol;
import com.biblioteca.models.acceso.Usuario;
import com.biblioteca.repositories.UsuarioRepository;
import com.biblioteca.service.RolService;
import com.biblioteca.service.UsuarioService;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UsuarioServiceImpl implements UsuarioService {

  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;
  private final UsuarioMapper usuarioMapper;
  private final RolService rolService;
  private final SuscripcionServiceImpl suscripcionService;
  private final PlanServiceImpl planService;
  private final ZoneId limaZone = ZoneId.of("America/Lima");

  public UsuarioServiceImpl(
      UsuarioRepository usuarioRepository,
      PasswordEncoder passwordEncoder,
      UsuarioMapper usuarioMapper,
      RolService rolService,
      @Lazy SuscripcionServiceImpl suscripcionService,
      PlanServiceImpl planService) {
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
    this.usuarioMapper = usuarioMapper;
    this.rolService = rolService;
    this.suscripcionService = suscripcionService;
    this.planService = planService;
  }

  @PostConstruct
  @Transactional
  public void inicializarUsuarios() {
    rolService.inicializarRoles();
    inicializarAdmin();
  }

  @Transactional
  private void inicializarAdmin() {
    if (usuarioRepository.findByUsername("admin").isEmpty()) {
      Usuario admin = new Usuario();
      admin.setUsername("admin");
      admin.setPassword(passwordEncoder.encode("admin"));
      admin.setEmail("admin@biblioteca.com");
      admin.setActivo(true);
      admin.setFechaRegistro(LocalDateTime.now(limaZone));
      admin.setUltimaActividad(LocalDateTime.now(limaZone));

      Set<Rol> rolesAdmin = new HashSet<>();
      rolService.buscarPorNombre(RolServiceImpl.ROLE_ADMIN).ifPresent(rolesAdmin::add);
      // Opcionalmente añadir otros roles si es necesario
      rolService.buscarPorNombre(RolServiceImpl.ROLE_LECTOR).ifPresent(rolesAdmin::add);
      admin.setRoles(rolesAdmin);

      usuarioRepository.save(admin);
      System.out.println("Usuario admin inicializado.");
    } else {
      System.out.println("Usuario admin ya existe.");
    }
  }

  @Override
  @Transactional
  public Usuario registrarUsuario(UsuarioRegistroDTO registroDTO) {
    try {
      if (usuarioRepository.existsByUsername(registroDTO.getUsername())) {
        throw new OperacionNoPermitidaException("El nombre de usuario ya está en uso: " + registroDTO.getUsername());
      }
      if (usuarioRepository.existsByEmail(registroDTO.getEmail())) {
        throw new OperacionNoPermitidaException("El email ya está en uso: " + registroDTO.getEmail());
      }

      Usuario usuario = usuarioMapper.usuarioRegistroDTOToUsuario(registroDTO);
      usuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
      usuario.setFechaRegistro(LocalDateTime.now(limaZone));
      usuario.setUltimaActividad(LocalDateTime.now(limaZone));
      usuario.setActivo(true);

      Optional<Rol> userRolOpt = rolService.buscarPorNombre(RolServiceImpl.ROLE_LECTOR);
      if (userRolOpt.isPresent()) {
        usuario.getRoles().add(userRolOpt.get());
      } else {
        throw new IllegalStateException(
            "El rol por defecto LECTOR no fue encontrado en la base de datos. No se puede registrar el usuario.");
      }

      // CAMBIO IMPORTANTE: Guardar y forzar flush para generar el ID
      usuario = usuarioRepository.saveAndFlush(usuario);

      // Verificar que el usuario tenga ID antes de asignar plan
      if (usuario.getId() != null) {
        log.info("Usuario guardado con ID: {} - Asignando plan básico", usuario.getId());
        asignarPlanBasicoPorDefecto(usuario);
      } else {
        throw new RuntimeException("Error: Usuario guardado sin ID");
      }

      return usuario;
    } catch (Exception e) {
      log.error("Error en registro de usuario {}: ", registroDTO.getUsername(), e);
      throw e; // Re-lanzar para que la transacción haga rollback
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Usuario> buscarPorUsername(String username) {
    return usuarioRepository.findByUsername(username);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Usuario> buscarPorEmail(String email) {
    return usuarioRepository.findByEmail(email);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Usuario> buscarPorId(Long id) {
    return usuarioRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Usuario usuario = usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

    Set<GrantedAuthority> authorities = usuario.getRoles().stream()
        .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
        .collect(Collectors.toSet());

    return new User(usuario.getUsername(), usuario.getPassword(), usuario.getActivo(),
        true, true, true, authorities);
  }

  @Override
  @Transactional
  public Usuario actualizarDatosUsuario(String username, UsuarioDataDTO datosActualizados) {
    Usuario usuario = usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));

    // Actualizar solo campos permitidos
    if (datosActualizados.getEmail() != null && !datosActualizados.getEmail().equals(usuario.getEmail())) {
      if (usuarioRepository.existsByEmail(datosActualizados.getEmail())) {
        throw new OperacionNoPermitidaException("El nuevo email ya está en uso: " + datosActualizados.getEmail());
      }
      usuario.setEmail(datosActualizados.getEmail());
    }
    // Aquí se podrían actualizar otros campos del UsuarioDataDTO si los tuviera
    // ej. nombre, apellido si estuvieran en Usuario y no en Lector/Perfil.

    usuario.setUltimaActividad(LocalDateTime.now(limaZone));
    return usuarioRepository.save(usuario);
  }

  @Override
  @Transactional
  public Usuario actualizarUsuario(Usuario usuario) {
    if (usuario == null || usuario.getId() == null) {
      throw new IllegalArgumentException("El usuario o su ID no pueden ser nulos para actualizar.");
    }
    // Asegurar que el usuario exista antes de intentar guardarlo (opcional, save
    // puede hacer upsert)
    // if(!usuarioRepository.existsById(usuario.getId())){
    // throw new RecursoNoEncontradoException("Usuario no encontrado con ID: " +
    // usuario.getId());
    // }
    usuario.setUltimaActividad(LocalDateTime.now(limaZone));
    return usuarioRepository.save(usuario);
  }

  @Override
  @Transactional
  public boolean cambiarPassword(String username, String passwordActual, String nuevaPassword) {
    Usuario usuario = usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));

    if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
      return false; // Contraseña actual incorrecta
    }

    usuario.setPassword(passwordEncoder.encode(nuevaPassword));
    usuario.setUltimaActualizacionPassword(LocalDateTime.now(limaZone));
    usuario.setUltimaActividad(LocalDateTime.now(limaZone));
    usuarioRepository.save(usuario);
    return true;
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsuarioAdminDTO> listarTodosLosUsuariosAdmin() {
    return usuarioRepository.findAll().stream()
        .map(usuarioMapper::usuarioToUsuarioAdminDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public boolean toggleEstadoUsuario(Long id) {
    return usuarioRepository.findById(id)
        .map(usuario -> {
          usuario.setActivo(!usuario.getActivo());
          usuario.setUltimaActividad(LocalDateTime.now(limaZone));
          usuarioRepository.save(usuario);
          return true;
        }).orElse(false);
  }

  @Override
  @Transactional
  public UsuarioAdminDTO crearUsuarioConRoles(UsuarioAdminDTO usuarioDTO) {
    if (usuarioRepository.existsByUsername(usuarioDTO.getUsername())) {
      throw new OperacionNoPermitidaException("El nombre de usuario ya está en uso: " + usuarioDTO.getUsername());
    }
    if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
      throw new OperacionNoPermitidaException("El email ya está en uso: " + usuarioDTO.getEmail());
    }

    Usuario usuario = new Usuario();
    usuario.setUsername(usuarioDTO.getUsername());
    usuario.setEmail(usuarioDTO.getEmail());
    usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
    usuario.setActivo(usuarioDTO.getActivo());
    usuario.setFechaRegistro(LocalDateTime.now(limaZone));
    usuario.setUltimaActividad(LocalDateTime.now(limaZone));

    Set<Rol> roles = new HashSet<>();
    if (usuarioDTO.getRoles() != null) {
      for (String nombreRol : usuarioDTO.getRoles()) {
        rolService.buscarPorNombre(nombreRol).ifPresent(roles::add);
      }
    }
    usuario.setRoles(roles);

    Usuario guardado = usuarioRepository.save(usuario);
    return usuarioMapper.usuarioToUsuarioAdminDTO(guardado);
  }

  @Override
  @Transactional
  public boolean eliminarCuenta(String username, String password) {
    Usuario usuario = usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));

    if (!passwordEncoder.matches(password, usuario.getPassword())) {
      throw new OperacionNoPermitidaException("Contraseña incorrecta.");
    }
    // Considerar la lógica de eliminación en cascada para perfiles, sesiones, etc.
    // Si Usuario tiene CascadeType.ALL y orphanRemoval=true para sus colecciones,
    // se eliminarán.
    // También se deben considerar las suscripciones, órdenes, etc., asociadas.
    // Podría ser mejor desactivar la cuenta en lugar de eliminarla físicamente.
    usuarioRepository.delete(usuario);
    return true;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean tieneUsuarioSuscripcionActiva(Long usuarioId) {
    return suscripcionService.verificarSuscripcionActiva(usuarioId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<String> obtenerPlanActualUsuario(Long usuarioId) {
    return suscripcionService.obtenerSuscripcionActivaPorUsuario(usuarioId)
        .map(suscripcion -> {
          try {
            var plan = planService.obtenerPlanPorId(suscripcion.getPlanId());
            return plan.map(planResponse -> planResponse.getNombre())
                .orElse("Sin plan");
          } catch (Exception e) {
            log.warn("Error obteniendo plan para usuario {}: {}", usuarioId, e.getMessage());
            return "Error obteniendo plan";
          }
        });
  }

  @Override
  @Transactional(readOnly = true)
  public long contarSuscripcionesActivas() {
    // Delegamos a MetricasSuscripcionService que tiene la lógica específica
    try {
      return suscripcionService.obtenerTodasLasSuscripciones().stream()
          .filter(s -> "ACTIVA".equals(s.getEstado()) || "PRUEBA".equals(s.getEstado()))
          .count();
    } catch (Exception e) {
      log.warn("Error contando suscripciones activas: {}", e.getMessage());
      return 0;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public long contarUsuarios() {
    return usuarioRepository.count();
  }

  @Override
  @Transactional(readOnly = true)
  public long contarUsuariosNuevosMes() {
    YearMonth mesActual = YearMonth.now(limaZone);
    LocalDateTime inicioMes = mesActual.atDay(1).atStartOfDay();
    LocalDateTime finMes = mesActual.atEndOfMonth().atTime(23, 59, 59, 999999999);
    return usuarioRepository.countByFechaRegistroBetween(inicioMes, finMes);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ActividadRecienteDTO> obtenerActividadesRecientes(int limit) {
    Pageable pageable = PageRequest.of(0, limit);
    // La consulta actual en UsuarioRepository ya obtiene actividades basadas en
    // u.ultimaActividad
    // La descripción es "Actualización de perfil/actividad", lo cual es adecuado
    // para un dashboard general.
    return usuarioRepository.findActividadesRecientesUsuarios(pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public long contarUsuariosConSuscripcionActiva() {
    return usuarioRepository.count() - usuarioRepository.contarUsuariosSinSuscripcionActiva();
  }

  @Override
  @Transactional
  public UsuarioAdminDTO actualizarUsuarioAdmin(UsuarioAdminDTO usuarioDTO) {
    Usuario usuario = usuarioRepository.findById(usuarioDTO.getId())
        .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + usuarioDTO.getId()));

    // Verificar si el username ha cambiado y no está en uso
    if (!usuario.getUsername().equals(usuarioDTO.getUsername())) {
      if (usuarioRepository.existsByUsername(usuarioDTO.getUsername())) {
        throw new OperacionNoPermitidaException("El nombre de usuario ya está en uso: " + usuarioDTO.getUsername());
      }
      usuario.setUsername(usuarioDTO.getUsername());
    }

    // Verificar si el email ha cambiado y no está en uso
    if (!usuario.getEmail().equals(usuarioDTO.getEmail())) {
      if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
        throw new OperacionNoPermitidaException("El email ya está en uso: " + usuarioDTO.getEmail());
      }
      usuario.setEmail(usuarioDTO.getEmail());
    }

    // Actualizar contraseña solo si se proporciona una nueva
    if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().trim().isEmpty()) {
      usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
      usuario.setUltimaActualizacionPassword(LocalDateTime.now(limaZone));
    }

    // Actualizar estado
    usuario.setActivo(usuarioDTO.getActivo());

    // Actualizar roles
    if (usuarioDTO.getRoles() != null) {
      actualizarRolesUsuario(usuario.getId(), usuarioDTO.getRoles());
    }

    usuario.setUltimaActividad(LocalDateTime.now(limaZone));
    Usuario usuarioActualizado = usuarioRepository.save(usuario);

    return usuarioMapper.usuarioToUsuarioAdminDTO(usuarioActualizado);
  }

  @Override
  @Transactional
  public void actualizarRolesUsuario(Long usuarioId, Set<String> nuevosRoles) {
    Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + usuarioId));

    // Limpiar roles actuales
    usuario.getRoles().clear();

    // Agregar nuevos roles
    Set<Rol> roles = new HashSet<>();
    if (nuevosRoles != null && !nuevosRoles.isEmpty()) {
      for (String nombreRol : nuevosRoles) {
        rolService.buscarPorNombre(nombreRol)
            .ifPresent(roles::add);
      }
    }

    // Si no se especifican roles, asignar ROLE_LECTOR por defecto
    if (roles.isEmpty()) {
      rolService.buscarPorNombre(RolServiceImpl.ROLE_LECTOR)
          .ifPresent(roles::add);
    }

    usuario.setRoles(roles);
    usuario.setUltimaActividad(LocalDateTime.now(limaZone));
    usuarioRepository.save(usuario);

    log.info("Roles actualizados para usuario ID {}: {}", usuarioId, nuevosRoles);
  }

  private void asignarPlanBasicoPorDefecto(Usuario usuario) {
    try {
      // Verificar que el usuario tenga ID
      if (usuario.getId() == null) {
        throw new IllegalArgumentException("El usuario debe tener ID antes de asignar plan");
      }

      // Obtener el plan básico
      var planBasicoOpt = planService.obtenerEntidadPlanPorId(1L);
      if (planBasicoOpt.isEmpty()) {
        log.warn("Plan básico con ID 1 no encontrado. Usuario: {}", usuario.getUsername());
        return;
      }

      // Crear suscripción directamente para plan gratuito
      suscripcionService.crearSuscripcionBasicaGratuita(usuario, planBasicoOpt.get());

      log.info("Plan básico asignado exitosamente al usuario: {}", usuario.getUsername());

    } catch (Exception e) {
      log.warn("No se pudo asignar plan básico al usuario {}: {}",
          usuario.getUsername(), e.getMessage());
    }
  }
}