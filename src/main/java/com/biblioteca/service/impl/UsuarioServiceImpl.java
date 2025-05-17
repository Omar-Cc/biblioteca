package com.biblioteca.service.impl;

import com.biblioteca.dto.ActividadRecienteDTO;
import com.biblioteca.dto.UsuarioAdminDTO;
import com.biblioteca.dto.UsuarioDataDTO;
import com.biblioteca.dto.UsuarioRegistroDTO;
import com.biblioteca.mapper.UsuarioMapper;
import com.biblioteca.models.Lector;
import com.biblioteca.models.Rol;
import com.biblioteca.models.Usuario;
import com.biblioteca.service.RolService;
import com.biblioteca.service.UsuarioService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

  private final Map<Long, Usuario> usuarios = new ConcurrentHashMap<>();
  private final Map<String, Usuario> usuariosPorUsername = new ConcurrentHashMap<>();
  private final Map<String, Usuario> usuariosPorEmail = new ConcurrentHashMap<>();
  private final AtomicLong usuarioIdCounter = new AtomicLong(0); // Iniciar en 0

  private final PasswordEncoder passwordEncoder;
  private final UsuarioMapper usuarioMapper;
  private final RolService rolService;
  private final ZoneId limaZone = ZoneId.of("America/Lima");
  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;

  @PostConstruct
  public void inicializarUsuarios() {
    rolService.inicializarRoles(); // Asegurar que los roles existan
    inicializarAdmin(); // Primero inicializar admin
    cargarUsuariosDesdeJson(); // Luego cargar usuarios desde JSON
  }

  private void inicializarAdmin() {
    if (buscarPorUsername("admin").isEmpty()) {
      Usuario admin = new Usuario();
      admin.setId(usuarioIdCounter.incrementAndGet());
      admin.setUsername("admin");
      admin.setPassword(passwordEncoder.encode("admin"));
      admin.setUltimaActualizacionPassword(LocalDateTime.now(limaZone));
      admin.setEmail("admin@biblioteca.com");
      admin.setActivo(true);
      admin.setFechaRegistro(LocalDateTime.now(limaZone));
      admin.setUltimaActividad(LocalDateTime.now(limaZone));

      Set<Rol> adminRoles = new java.util.HashSet<>();
      rolService.buscarPorNombre(RolServiceImpl.ROLE_ADMIN).ifPresent(adminRoles::add);
      rolService.buscarPorNombre(RolServiceImpl.ROLE_USER).ifPresent(adminRoles::add);
      admin.setRoles(adminRoles);

      usuarios.put(admin.getId(), admin);
      usuariosPorUsername.put(admin.getUsername(), admin);
      usuariosPorEmail.put(admin.getEmail(), admin);

      System.out.println("Usuario admin creado: " + admin.getFechaRegistro());
      System.out.println("Usuario admin inicializado.");
    } else {
      System.out.println("Usuario admin ya existe.");
    }
  }

  private void cargarUsuariosDesdeJson() {
    try {
      InputStream inputStream = resourceLoader.getResource("classpath:data/usuarios-data.json").getInputStream();
      List<Map<String, Object>> usuariosData = objectMapper.readValue(inputStream,
          new TypeReference<List<Map<String, Object>>>() {
          });

      for (Map<String, Object> userData : usuariosData) {
        String username = (String) userData.get("username");

        // Verificar si el usuario ya existe
        if (buscarPorUsername(username).isPresent()) {
          System.out.println("Usuario ya existe, omitiendo: " + username);
          continue;
        }

        Usuario usuario = new Usuario();
        usuario.setId(usuarioIdCounter.incrementAndGet());
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode((String) userData.get("password")));
        usuario.setEmail((String) userData.get("email"));
        usuario.setActivo((Boolean) userData.get("activo"));

        // Convertir fechas de string a LocalDateTime
        usuario.setFechaRegistro(LocalDateTime.parse((String) userData.get("fechaRegistro")));
        usuario.setUltimaActividad(LocalDateTime.parse((String) userData.get("ultimaActividad")));
        usuario
            .setUltimaActualizacionPassword(LocalDateTime.parse((String) userData.get("ultimaActualizacionPassword")));

        // Manejar roles
        Set<Rol> roles = new HashSet<>();
        List<String> rolesList = (List<String>) userData.get("roles");
        for (String rolNombre : rolesList) {
          rolService.buscarPorNombre(rolNombre).ifPresent(roles::add);
        }
        usuario.setRoles(roles);

        // Guardar usuario en los mapas
        usuarios.put(usuario.getId(), usuario);
        usuariosPorUsername.put(usuario.getUsername(), usuario);
        usuariosPorEmail.put(usuario.getEmail(), usuario);

        System.out.println("Usuario cargado desde JSON: " + username);
      }

      System.out.println("Datos iniciales de Usuarios cargados: " + usuariosData.size() + " usuarios procesados.");
    } catch (Exception e) {
      System.err.println("Error al cargar datos iniciales de usuarios desde JSON: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public Usuario registrarUsuario(UsuarioRegistroDTO registroDTO) {
    if (buscarPorUsername(registroDTO.getUsername()).isPresent()) {
      throw new IllegalArgumentException("El nombre de usuario ya existe: " + registroDTO.getUsername());
    }
    if (buscarPorEmail(registroDTO.getEmail()).isPresent()) {
      throw new IllegalArgumentException("El correo electrónico ya está registrado: " + registroDTO.getEmail());
    }

    Usuario usuario = usuarioMapper.usuarioRegistroDTOToUsuario(registroDTO);
    usuario.setId(usuarioIdCounter.incrementAndGet());
    usuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));

    Optional<Rol> userRolOpt = rolService.buscarPorNombre(RolServiceImpl.ROLE_USER);
    if (userRolOpt.isPresent()) {
      usuario.getRoles().add(userRolOpt.get());
    } else {
      System.err.println("Error crítico: Rol USER no encontrado durante el registro.");
      throw new RuntimeException("Error interno del servidor: configuración de roles incompleta.");
    }

    usuarios.put(usuario.getId(), usuario);
    usuariosPorUsername.put(usuario.getUsername(), usuario);
    usuariosPorEmail.put(usuario.getEmail(), usuario);

    System.out.println("Usuario registrado: " + usuario.getUsername());
    return usuario;
  }

  @Override
  public Optional<Usuario> buscarPorUsername(String username) {
    return Optional.ofNullable(usuariosPorUsername.get(username));
  }

  @Override
  public Optional<Usuario> buscarPorEmail(String email) {
    return Optional.ofNullable(usuariosPorEmail.get(email));
  }

  @Override
  public Optional<Usuario> buscarPorId(Long id) {
    return Optional.ofNullable(usuarios.get(id));
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Usuario usuario = buscarPorUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con nombre de usuario: " + username));

    Set<GrantedAuthority> authorities = usuario.getRoles().stream()
        .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
        .collect(Collectors.toSet());

    return new User(usuario.getUsername(), usuario.getPassword(), usuario.isActivo(),
        true, true, true, authorities);
  }

  @Override
  public Usuario actualizarUsuario(String username, UsuarioDataDTO datosActualizados) {
    Usuario usuario = buscarPorUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

    // Si el email cambia, verificar que no exista otro usuario con ese email
    if (!usuario.getEmail().equals(datosActualizados.getEmail())) {
      Optional<Usuario> existingEmailUser = buscarPorEmail(datosActualizados.getEmail());
      if (existingEmailUser.isPresent() && !existingEmailUser.get().getId().equals(usuario.getId())) {
        throw new IllegalArgumentException("El correo electrónico ya está registrado: " + datosActualizados.getEmail());
      }

      // Actualizar el email en el mapa de emails
      usuariosPorEmail.remove(usuario.getEmail());
      usuariosPorEmail.put(datosActualizados.getEmail(), usuario);
    }

    // Actualizar los datos
    usuario.setEmail(datosActualizados.getEmail());
    usuario.setUltimaActividad(LocalDateTime.now(limaZone));

    return usuario;
  }

  @Override
  public boolean cambiarPassword(String username, String passwordActual, String nuevaPassword) {
    Usuario usuario = buscarPorUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

    // Verificar que la contraseña actual sea correcta
    if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
      return false; // Contraseña incorrecta
    }

    // Actualizar la contraseña
    usuario.setPassword(passwordEncoder.encode(nuevaPassword));
    usuario.setUltimaActualizacionPassword(LocalDateTime.now(limaZone));
    usuario.setUltimaActividad(LocalDateTime.now(limaZone));

    return true;
  }

  @Override
  public List<Usuario> listarTodosLosUsuarios() {
    return new ArrayList<>(usuarios.values());
  }

  @Override
  public boolean toggleEstadoUsuario(Long id) {
    Usuario usuario = usuarios.get(id);
    if (usuario == null) {
      throw new IllegalArgumentException("Usuario no encontrado con ID: " + id);
    }

    // No permitir desactivar el usuario admin
    if (usuario.getUsername().equals("admin")) {
      throw new IllegalArgumentException("No se puede desactivar el usuario administrador.");
    }

    usuario.setActivo(!usuario.isActivo());
    return usuario.isActivo();
  }

  @Override
  public Usuario crearUsuarioConRoles(UsuarioAdminDTO usuarioDTO) {
    if (buscarPorUsername(usuarioDTO.getUsername()).isPresent()) {
      throw new IllegalArgumentException("El nombre de usuario ya existe: " + usuarioDTO.getUsername());
    }
    if (buscarPorEmail(usuarioDTO.getEmail()).isPresent()) {
      throw new IllegalArgumentException("El correo electrónico ya está registrado: " + usuarioDTO.getEmail());
    }

    Usuario usuario = Usuario.builder()
        .id(usuarioIdCounter.incrementAndGet())
        .username(usuarioDTO.getUsername())
        .password(passwordEncoder.encode(usuarioDTO.getPassword()))
        .email(usuarioDTO.getEmail())
        .activo(true)
        .fechaRegistro(LocalDateTime.now(limaZone))
        .ultimaActividad(LocalDateTime.now(limaZone))
        .build();

    Set<Rol> roles = new HashSet<>();
    for (String rolNombre : usuarioDTO.getRoles()) {
      rolService.buscarPorNombre(rolNombre)
          .ifPresent(roles::add);
    }

    if (roles.isEmpty()) {
      throw new IllegalArgumentException(
          "No se pudieron asignar roles válidos. Debe seleccionar al menos un rol existente.");
    }

    usuario.setRoles(roles);

    usuarios.put(usuario.getId(), usuario);
    usuariosPorUsername.put(usuario.getUsername(), usuario);
    usuariosPorEmail.put(usuario.getEmail(), usuario);

    return usuario;
  }

  @Override
  public void actualizarLector(Lector lector) {
    // Asegurarse de que el lector ya existe en el sistema
    if (usuarios.containsKey(lector.getId())) {
      // Actualizar el usuario en todos los mapas
      usuarios.put(lector.getId(), lector);
      usuariosPorUsername.put(lector.getUsername(), lector);
      usuariosPorEmail.put(lector.getEmail(), lector);
    } else {
      throw new IllegalArgumentException("No existe un usuario con el ID proporcionado: " + lector.getId());
    }
  }

  @Override
  public boolean eliminarCuenta(String username, String password) {
    Usuario usuario = buscarPorUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

    // Verificar que la contraseña sea correcta
    if (!passwordEncoder.matches(password, usuario.getPassword())) {
      return false; // Contraseña incorrecta
    }

    // Eliminar el usuario de todos los mapas
    usuarios.remove(usuario.getId());
    usuariosPorUsername.remove(usuario.getUsername());
    usuariosPorEmail.remove(usuario.getEmail());

    return true; // Cuenta eliminada con éxito
  }

  @Override
  public long contarUsuarios() {
    return usuarios.size();
  }

  @Override
  public long contarUsuariosNuevosMes() {
    LocalDateTime inicioMes = YearMonth.now().atDay(1).atStartOfDay();
    LocalDateTime finMes = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);

    return usuarios.values().stream()
        .filter(u -> u.getFechaRegistro() != null)
        .filter(u -> !u.getFechaRegistro().isBefore(inicioMes) && !u.getFechaRegistro().isAfter(finMes))
        .count();
  }

  @Override
  public List<ActividadRecienteDTO> obtenerActividadesRecientes(int limit) {
    List<ActividadRecienteDTO> actividades = new ArrayList<>();

    // Añadir actividades de préstamos
    /*
     * prestamos.stream()
     * .sorted((p1, p2) -> p2.getFechaPrestamo().compareTo(p1.getFechaPrestamo()))
     * .limit(limit)
     * .forEach(prestamo -> {
     * ActividadRecienteDTO actividad = new ActividadRecienteDTO();
     * actividad.setTitulo("Préstamo de libro");
     * actividad.setDescripcion("Préstamo del libro: " +
     * prestamo.getObra().getTitulo());
     * actividad.setUsuario(prestamo.getUsuario().getUsername());
     * actividad.setFecha(prestamo.getFechaPrestamo());
     * actividad.setTipo("PRÉSTAMO");
     * 
     * actividades.add(actividad);
     * });
     * 
     * // Añadir actividades de registro de usuarios
     * usuarios.values().stream()
     * .sorted((u1, u2) -> u2.getFechaRegistro().compareTo(u1.getFechaRegistro()))
     * .limit(limit)
     * .forEach(usuario -> {
     * ActividadRecienteDTO actividad = new ActividadRecienteDTO();
     * actividad.setTitulo("Nuevo usuario registrado");
     * actividad.setDescripcion("Se ha registrado un nuevo usuario: " +
     * usuario.getUsername());
     * actividad.setUsuario(usuario.getUsername());
     * actividad.setFecha(usuario.getFechaRegistro());
     * actividad.setTipo("REGISTRO");
     * 
     * actividades.add(actividad);
     * });
     */

    // Ordenar todas las actividades por fecha y limitar
    return actividades.stream()
        .sorted((a1, a2) -> a2.getFecha().compareTo(a1.getFecha()))
        .limit(limit)
        .collect(Collectors.toList());
  }

}