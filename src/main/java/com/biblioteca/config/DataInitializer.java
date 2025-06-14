package com.biblioteca.config;

import com.biblioteca.dto.AutorRequestDTO;
import com.biblioteca.dto.comercial.BeneficioRequestDTO;
import com.biblioteca.dto.comercial.MetodoPagoRequestDTO;
import com.biblioteca.dto.comercial.PlanBeneficioRequestDTO;
import com.biblioteca.dto.comercial.PlanRequestDTO;
import com.biblioteca.dto.comercial.SuscripcionResponseDTO;
import com.biblioteca.dto.ContenidoRequestDTO;
import com.biblioteca.dto.EditorialRequestDTO;
import com.biblioteca.dto.GeneroRequestDTO;
import com.biblioteca.dto.ObraRequestDTO;
import com.biblioteca.dto.SerieRequestDTO;
import com.biblioteca.dto.UsuarioAdminDTO;
import com.biblioteca.enums.FormatoFisico;
import com.biblioteca.enums.TipoContenido;
import com.biblioteca.enums.TipoLicenciaDigital;
import com.biblioteca.service.AutorService;
import com.biblioteca.service.BeneficioService;
import com.biblioteca.service.ContenidoService;
import com.biblioteca.service.EditorialService;
import com.biblioteca.service.GeneroService;
import com.biblioteca.service.MetodoPagoService;
import com.biblioteca.service.ObraService;
import com.biblioteca.service.PlanBeneficioService;
import com.biblioteca.service.PlanService;
import com.biblioteca.service.SerieService;
import com.biblioteca.service.SuscripcionService;
import com.biblioteca.service.UsuarioService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
  private final AutorService autorService;
  private final BeneficioService beneficioService;
  private final EditorialService editorialService;
  private final GeneroService generoService;
  private final MetodoPagoService metodoPagoService;
  private final ObraService obraService;
  private final SerieService serieService;
  private final PlanService planService;
  private final PlanBeneficioService planBeneficioService;
  private final SuscripcionService suscripcionService;
  private final UsuarioService usuarioService;
  private final ContenidoService contenidoService;

  @Override
  public void run(String... args) throws Exception {
    log.info("Iniciando carga de datos iniciales...");
    try {
      // Orden de carga: dependencias primero
      cargarEditoriales(); // Las editoriales son requeridas por obras y contenidos
      cargarAutores(); // Los autores son requeridos por obras
      cargarGeneros(); // Los géneros son requeridos por obras
      cargarObras(); // Las obras son requeridas por contenidos
      cargarSeries(); // Las series son requeridas por algunos contenidos
      cargarBeneficios(); // Los beneficios son requeridos por plan-beneficios
      cargarMetodosPago(); // Los métodos de pago son requeridos por el checkout
      cargarPlanes(); // Los planes son requeridos por plan-beneficios y usuarios
      cargarPlanBeneficios(); // Las asociaciones plan-beneficio
      cargarUsuarios(); // Los usuarios dependen de roles y planes
      cargarContenidos(); // Los contenidos dependen de obras, editoriales y series

      // Actualizar suscripciones existentes que no tengan modalidadPago
      actualizarSuscripcionesExistentes();

      log.info("Todos los datos iniciales se cargaron exitosamente.");
    } catch (Exception e) {
      log.error("Error al cargar datos iniciales: {}", e.getMessage(), e);
    }
  }

  private void cargarEditoriales() throws IOException {
    log.info("Cargando datos de editoriales...");

    if (!editorialService.obtenerTodasLasEditoriales().isEmpty()) {
      log.info("Los datos de editoriales ya están cargados. Omitiendo inicialización.");
      return;
    }

    ObjectMapper objectMapper = createObjectMapper();
    ClassPathResource resource = new ClassPathResource("data/editoriales-data.json");

    try (InputStream inputStream = resource.getInputStream()) {
      List<Map<String, Object>> editorialesData = objectMapper.readValue(
          inputStream,
          new TypeReference<List<Map<String, Object>>>() {
          });

      int editorialesCreadas = 0;
      for (Map<String, Object> editorialData : editorialesData) {
        try {
          EditorialRequestDTO editorialRequest = mapearDatosEditorial(editorialData);
          editorialService.crearEditorial(editorialRequest);
          editorialesCreadas++;
          log.debug("Editorial creada: {}", editorialRequest.getNombre());
        } catch (Exception e) {
          log.warn("Error al crear editorial {}: {}",
              editorialData.get("nombre"), e.getMessage());
        }
      }
      log.info("Se crearon {} editoriales exitosamente.", editorialesCreadas);
    }
  }

  private void cargarAutores() throws IOException {
    log.info("Cargando datos de autores...");

    if (!autorService.obtenerTodosLosAutores().isEmpty()) {
      log.info("Los datos de autores ya están cargados. Omitiendo inicialización.");
      return;
    }

    ObjectMapper objectMapper = createObjectMapper();
    ClassPathResource resource = new ClassPathResource("data/autores-data.json");

    try (InputStream inputStream = resource.getInputStream()) {
      List<Map<String, Object>> autoresData = objectMapper.readValue(
          inputStream,
          new TypeReference<List<Map<String, Object>>>() {
          });

      int autoresCreados = 0;
      for (Map<String, Object> autorData : autoresData) {
        try {
          AutorRequestDTO autorRequest = mapearDatosAutor(autorData);
          autorService.crearAutor(autorRequest);
          autoresCreados++;
          log.debug("Autor creado: {}", autorRequest.getNombre());
        } catch (Exception e) {
          log.warn("Error al crear autor {}: {}",
              autorData.get("nombre"), e.getMessage());
        }
      }
      log.info("Se crearon {} autores exitosamente.", autoresCreados);
    }
  }

  private void cargarGeneros() throws IOException {
    log.info("Cargando datos de géneros...");

    if (!generoService.obtenerTodosLosGeneros().isEmpty()) {
      log.info("Los datos de géneros ya están cargados. Omitiendo inicialización.");
      return;
    }

    ObjectMapper objectMapper = createObjectMapper();
    ClassPathResource resource = new ClassPathResource("data/generos-data.json");

    try (InputStream inputStream = resource.getInputStream()) {
      List<Map<String, Object>> generosData = objectMapper.readValue(
          inputStream,
          new TypeReference<List<Map<String, Object>>>() {
          });

      // Separar géneros principales (nivel 1) y subgéneros (nivel 2)
      List<Map<String, Object>> generosPrincipales = generosData.stream()
          .filter(g -> {
            Object parentId = g.get("parentId");
            Integer nivel = parsearInteger(g.get("nivel"));
            return (parentId == null) && (nivel == null || nivel == 1);
          })
          .collect(Collectors.toList());

      List<Map<String, Object>> subgeneros = generosData.stream()
          .filter(g -> {
            Object parentId = g.get("parentId");
            Integer nivel = parsearInteger(g.get("nivel"));
            return (parentId != null) || (nivel != null && nivel == 2);
          })
          .collect(Collectors.toList());

      // Primera pasada: crear géneros principales
      int generosPrincipalesCreados = 0;
      for (Map<String, Object> generoData : generosPrincipales) {
        try {
          GeneroRequestDTO generoRequest = mapearDatosGenero(generoData);
          generoService.crearGenero(generoRequest);
          generosPrincipalesCreados++;
          log.debug("Género principal creado: {}", generoRequest.getNombre());
        } catch (Exception e) {
          log.warn("Error al crear género principal {}: {}",
              generoData.get("nombre"), e.getMessage());
        }
      }
      log.info("Se crearon {} géneros principales exitosamente.", generosPrincipalesCreados);

      // Segunda pasada: crear subgéneros (que tienen parentId)
      int subgenerosCreados = 0;
      for (Map<String, Object> generoData : subgeneros) {
        try {
          GeneroRequestDTO generoRequest = mapearDatosGenero(generoData);
          generoService.crearGenero(generoRequest);
          subgenerosCreados++;
          log.debug("Subgénero creado: {} (padre: {})",
              generoRequest.getNombre(), generoRequest.getParentId());
        } catch (Exception e) {
          log.warn("Error al crear subgénero {}: {}",
              generoData.get("nombre"), e.getMessage());
        }
      }
      log.info("Se crearon {} subgéneros exitosamente.", subgenerosCreados);
      log.info("Total de géneros creados: {}", generosPrincipalesCreados + subgenerosCreados);
    }
  }

  private void cargarObras() throws IOException {
    log.info("Cargando datos de obras...");

    if (!obraService.obtenerTodasLasObras().isEmpty()) {
      log.info("Los datos de obras ya están cargados. Omitiendo inicialización.");
      return;
    }

    ObjectMapper objectMapper = createObjectMapper();
    ClassPathResource resource = new ClassPathResource("data/obras-data.json");

    try (InputStream inputStream = resource.getInputStream()) {
      List<Map<String, Object>> obrasData = objectMapper.readValue(
          inputStream,
          new TypeReference<List<Map<String, Object>>>() {
          });

      int obrasCreadas = 0;
      int obrasOmitidas = 0;

      for (Map<String, Object> obraData : obrasData) {
        try {
          // Validar campos básicos
          String titulo = (String) obraData.get("titulo");
          if (titulo == null || titulo.trim().isEmpty()) {
            log.debug("Omitiendo obra sin título válido");
            obrasOmitidas++;
            continue;
          }

          ObraRequestDTO obraRequest = mapearDatosObra(obraData);

          // Si mapearDatosObra retorna null, omitir obra
          if (obraRequest == null) {
            obrasOmitidas++;
            continue;
          }

          log.debug("Intentando crear obra: {} con {} autores",
              titulo, obraRequest.getAutorIds().size());

          obraService.crearObra(obraRequest);
          obrasCreadas++;
          log.debug("✅ Obra creada exitosamente: {}", titulo);

        } catch (Exception e) {
          String titulo = (String) obraData.get("titulo");
          log.warn("❌ Error al crear obra '{}': {}", titulo, e.getMessage());
          obrasOmitidas++;
        }
      }

      log.info("📊 Resumen de carga de obras:");
      log.info("   ✅ Obras creadas: {}", obrasCreadas);
      if (obrasOmitidas > 0) {
        log.warn("   ⚠️  Obras omitidas: {}", obrasOmitidas);
      }
      log.info("Se crearon {} obras exitosamente.", obrasCreadas);
    }
  }

  private void cargarSeries() throws IOException {
    log.info("Cargando datos de series...");

    if (!serieService.obtenerTodasLasSeries().isEmpty()) {
      log.info("Los datos de series ya están cargados. Omitiendo inicialización.");
      return;
    }

    ObjectMapper objectMapper = createObjectMapper();
    ClassPathResource resource = new ClassPathResource("data/series-data.json");

    try (InputStream inputStream = resource.getInputStream()) {
      List<Map<String, Object>> seriesData = objectMapper.readValue(
          inputStream,
          new TypeReference<List<Map<String, Object>>>() {
          });

      int seriesCreadas = 0;
      for (Map<String, Object> serieData : seriesData) {
        try {
          // Validar que la serie tenga los campos mínimos requeridos
          if (serieData.get("nombre") == null ||
              ((String) serieData.get("nombre")).trim().isEmpty()) {
            log.debug("Omitiendo serie sin nombre válido");
            continue;
          }

          SerieRequestDTO serieRequest = mapearDatosSerie(serieData);
          serieService.crearSerie(serieRequest);
          seriesCreadas++;

          log.debug("Serie creada: {} - {} volúmenes - {}",
              serieRequest.getNombre(),
              serieRequest.getNumeroVolumenes(),
              serieRequest.isCompleta() ? "Completa" : "En progreso");
        } catch (Exception e) {
          log.warn("Error al crear serie {}: {}",
              serieData.get("nombre"), e.getMessage());
        }
      }
      log.info("Se crearon {} series exitosamente.", seriesCreadas);

      // Log resumen de series
      logResumenSeries(seriesData);
    }
  }

  private void logResumenSeries(List<Map<String, Object>> seriesData) {
    log.info("=== RESUMEN DE SERIES CARGADAS ===");

    // Contar series por estado
    long seriesCompletas = seriesData.stream()
        .filter(s -> parsearBoolean(s.get("completa")))
        .count();

    long seriesEnProgreso = seriesData.stream()
        .filter(s -> !parsearBoolean(s.get("completa")))
        .count();

    // Calcular volúmenes totales
    int totalVolumenes = seriesData.stream()
        .mapToInt(s -> parsearInteger(s.get("numeroVolumenes")))
        .sum();

    log.info("Series completas: {}", seriesCompletas);
    log.info("Series en progreso: {}", seriesEnProgreso);
    log.info("Total de volúmenes: {}", totalVolumenes);

    // Detalles por serie
    seriesData.forEach(serieData -> {
      String nombre = (String) serieData.get("nombre");
      Integer volúmenes = parsearInteger(serieData.get("numeroVolumenes"));
      boolean completa = parsearBoolean(serieData.get("completa"));

      if (nombre != null) {
        log.info("  • {}: {} volúmenes - {}",
            nombre,
            volúmenes != null ? volúmenes : 0,
            completa ? "Completa" : "En progreso");
      }
    });

    log.info("===================================");
  }

  private void cargarBeneficios() throws IOException {
    log.info("Cargando datos de beneficios...");

    if (!beneficioService.obtenerTodosLosBeneficios().isEmpty()) {
      log.info("Los datos de beneficios ya están cargados. Omitiendo inicialización.");
      return;
    }

    ObjectMapper objectMapper = createObjectMapper();
    ClassPathResource resource = new ClassPathResource("data/beneficios-data.json");

    try (InputStream inputStream = resource.getInputStream()) {
      List<Map<String, Object>> beneficiosData = objectMapper.readValue(
          inputStream,
          new TypeReference<List<Map<String, Object>>>() {
          });

      int beneficiosCreados = 0;
      for (Map<String, Object> beneficioData : beneficiosData) {
        try {
          // Validar que el beneficio tenga los campos mínimos requeridos
          if (beneficioData.get("nombre") == null ||
              ((String) beneficioData.get("nombre")).trim().isEmpty()) {
            log.debug("Omitiendo beneficio sin nombre válido");
            continue;
          }

          BeneficioRequestDTO beneficioRequest = mapearDatosBeneficio(beneficioData);
          beneficioService.crearBeneficio(beneficioRequest);
          beneficiosCreados++;
          log.debug("Beneficio creado: {}", beneficioRequest.getNombre());
        } catch (Exception e) {
          log.warn("Error al crear beneficio {}: {}",
              beneficioData.get("nombre"), e.getMessage());
        }
      }
      log.info("Se crearon {} beneficios exitosamente.", beneficiosCreados);
    }
  }

  private void cargarPlanes() throws IOException {
    log.info("Cargando datos de planes...");

    if (!planService.obtenerTodosLosPlanes().isEmpty()) {
      log.info("Los datos de planes ya están cargados. Omitiendo inicialización.");
      return;
    }

    ObjectMapper objectMapper = createObjectMapper();
    ClassPathResource resource = new ClassPathResource("data/planes-data.json");

    try (InputStream inputStream = resource.getInputStream()) {
      List<Map<String, Object>> planesData = objectMapper.readValue(
          inputStream,
          new TypeReference<List<Map<String, Object>>>() {
          });

      int planesCreados = 0;
      for (Map<String, Object> planData : planesData) {
        try {
          // Validar que el plan tenga los campos mínimos requeridos
          if (planData.get("nombre") == null ||
              ((String) planData.get("nombre")).trim().isEmpty()) {
            log.debug("Omitiendo plan sin nombre válido");
            continue;
          }

          PlanRequestDTO planRequest = mapearDatosPlan(planData);
          planService.crearPlan(planRequest);
          planesCreados++;

          log.debug("Plan creado: {} - Precio mensual: S/.{} - Precio anual: S/.{}",
              planRequest.getNombre(),
              formatearPrecio(planRequest.getPrecioMensual()),
              formatearPrecio(planRequest.getPrecioAnual()));
        } catch (Exception e) {
          log.warn("Error al crear plan {}: {}",
              planData.get("nombre"), e.getMessage());
        }
      }
      log.info("Se crearon {} planes exitosamente.", planesCreados);

      // Log resumen de planes
      logResumenPlanes(planesData);
    }
  }

  private void logResumenPlanes(List<Map<String, Object>> planesData) {
    log.info("=== RESUMEN DE PLANES CARGADOS ===");

    planesData.forEach(planData -> {
      String nombre = (String) planData.get("nombre");
      Integer precioMensual = parsearInteger(planData.get("precioMensual"));
      Integer precioAnual = parsearInteger(planData.get("precioAnual"));
      Integer diasPrueba = parsearInteger(planData.get("diasPrueba"));
      String periodo = (String) planData.get("periodoFacturacion");

      if (nombre != null) {
        log.info("{}: S/.{}/mes, S/.{}/año, {} días prueba, Facturación: {}",
            nombre,
            formatearPrecio(precioMensual),
            formatearPrecio(precioAnual),
            diasPrueba != null ? diasPrueba : 0,
            periodo != null ? periodo : "N/A");
      }
    });

    log.info("====================================");
  }

  private String formatearPrecio(Integer precioEnCentavos) {
    if (precioEnCentavos == null || precioEnCentavos == 0) {
      return "0.00";
    }
    return String.format("%.2f", precioEnCentavos / 100.0);
  }

  private void cargarPlanBeneficios() throws IOException {
    log.info("Cargando datos de plan-beneficios...");

    // Verificar si ya existen asociaciones plan-beneficio
    try {
      if (!planBeneficioService.obtenerBeneficiosPorPlanId(1L).isEmpty()) {
        log.info("Los datos de plan-beneficios ya están cargados. Omitiendo inicialización.");
        return;
      }
    } catch (Exception e) {
      log.debug("Verificación de plan-beneficios existentes falló, continuando con la carga...");
    }

    ObjectMapper objectMapper = createObjectMapper();
    ClassPathResource resource = new ClassPathResource("data/plan-beneficios-data.json");

    try (InputStream inputStream = resource.getInputStream()) {
      List<Map<String, Object>> planBeneficiosData = objectMapper.readValue(
          inputStream,
          new TypeReference<List<Map<String, Object>>>() {
          });

      int planBeneficiosCreados = 0;
      for (Map<String, Object> planBeneficioData : planBeneficiosData) {
        try {
          // Validar que tenga los campos mínimos requeridos
          if (planBeneficioData.get("planId") == null ||
              planBeneficioData.get("beneficioId") == null) {
            log.debug("Omitiendo plan-beneficio sin planId o beneficioId válidos");
            continue;
          }

          PlanBeneficioRequestDTO planBeneficioRequest = mapearDatosPlanBeneficio(planBeneficioData);
          planBeneficioService.asociarBeneficioAPlan(planBeneficioRequest);
          planBeneficiosCreados++;

          log.debug("Plan-beneficio creado: Plan {} - Beneficio {} - Valor: {}",
              planBeneficioRequest.getPlanId(),
              planBeneficioRequest.getBeneficioId(),
              planBeneficioRequest.getValor());
        } catch (Exception e) {
          log.warn("Error al crear plan-beneficio Plan {} - Beneficio {}: {}",
              planBeneficioData.get("planId"),
              planBeneficioData.get("beneficioId"),
              e.getMessage());
        }
      }
      log.info("Se crearon {} asociaciones plan-beneficio exitosamente.", planBeneficiosCreados);

      // Log resumen por plan
      logResumenPorPlan(planBeneficiosData);
    }
  }

  private void logResumenPorPlan(List<Map<String, Object>> planBeneficiosData) {
    Map<Object, Long> beneficiosPorPlan = planBeneficiosData.stream()
        .filter(pb -> pb.get("planId") != null)
        .collect(Collectors.groupingBy(
            pb -> pb.get("planId"),
            Collectors.counting()));

    beneficiosPorPlan.forEach((planId, cantidad) -> log.info("Plan {}: {} beneficios configurados", planId, cantidad));
  }

  private void cargarUsuarios() throws IOException {
    log.info("Cargando datos de usuarios...");

    if (!usuarioService.listarTodosLosUsuariosAdmin().isEmpty()) {
      log.info("Los datos de usuarios ya están cargados. Omitiendo inicialización.");
      return;
    }

    ObjectMapper objectMapper = createObjectMapper();
    ClassPathResource resource = new ClassPathResource("data/usuarios-data.json");

    try (InputStream inputStream = resource.getInputStream()) {
      List<Map<String, Object>> usuariosData = objectMapper.readValue(
          inputStream,
          new TypeReference<List<Map<String, Object>>>() {
          });

      int usuariosCreados = 0;
      for (Map<String, Object> usuarioData : usuariosData) {
        try {
          // Validar que el usuario tenga los campos mínimos requeridos
          if (usuarioData.get("username") == null ||
              usuarioData.get("email") == null ||
              usuarioData.get("password") == null) {
            log.debug("Omitiendo usuario sin campos básicos válidos");
            continue;
          }

          UsuarioAdminDTO usuarioRequest = mapearDatosUsuario(usuarioData);
          usuarioService.crearUsuarioConRoles(usuarioRequest);
          usuariosCreados++;

          log.debug("Usuario creado: {} - {} - Roles: {}",
              usuarioRequest.getUsername(),
              usuarioRequest.getEmail(),
              String.join(", ", usuarioRequest.getRoles()));
        } catch (Exception e) {
          log.warn("Error al crear usuario {}: {}",
              usuarioData.get("username"), e.getMessage());
        }
      }
      log.info("Se crearon {} usuarios exitosamente.", usuariosCreados);

      // Log resumen de usuarios
      logResumenUsuarios(usuariosData);
    }
  }

  @SuppressWarnings("unchecked")
  private void logResumenUsuarios(List<Map<String, Object>> usuariosData) {
    log.info("=== RESUMEN DE USUARIOS CARGADOS ===");

    // Contar usuarios por rol
    Map<String, Long> usuariosPorRol = usuariosData.stream()
        .flatMap(u -> {
          Object rolesObj = u.get("roles");
          if (rolesObj instanceof List) {
            return ((List<String>) rolesObj).stream();
          }
          return java.util.stream.Stream.empty();
        })
        .collect(Collectors.groupingBy(
            rol -> rol,
            Collectors.counting()));

    // Contar usuarios activos vs inactivos
    long usuariosActivos = usuariosData.stream()
        .filter(u -> parsearBoolean(u.get("activo")))
        .count();

    long usuariosInactivos = usuariosData.size() - usuariosActivos;

    log.info("Total de usuarios: {}", usuariosData.size());
    log.info("Usuarios activos: {}", usuariosActivos);
    log.info("Usuarios inactivos: {}", usuariosInactivos);
    log.info("");
    log.info("Distribución por roles:");
    usuariosPorRol.forEach((rol, cantidad) -> log.info("  • {}: {}", rol, cantidad));

    log.info("");
    log.info("Detalles por usuario:");
    usuariosData.forEach(usuarioData -> {
      String username = (String) usuarioData.get("username");
      String email = (String) usuarioData.get("email");
      boolean activo = parsearBoolean(usuarioData.get("activo"));
      Object rolesObj = usuarioData.get("roles");
      String roles = "";

      if (rolesObj instanceof List) {
        roles = String.join(", ", (List<String>) rolesObj);
      }

      log.info("  • {}: {} - {} - [{}]",
          username,
          email,
          activo ? "Activo" : "Inactivo",
          roles);
    });

    log.info("======================================");
  }

  private void cargarContenidos() throws IOException {
    log.info("Cargando datos de contenidos...");

    // Verificar si ya existen contenidos usando método alternativo si el original
    // no existe
    try {
      if (!contenidoService.obtenerCatalogoPublico(
          java.util.Optional.empty(),
          java.util.Optional.empty(),
          java.util.Optional.empty(),
          java.util.Optional.empty(),
          java.util.Optional.empty()).isEmpty()) {
        log.info("Los datos de contenidos ya están cargados. Omitiendo inicialización.");
        return;
      }
    } catch (Exception e) {
      log.debug("Método obtenerCatalogoPublico no disponible, continuando con la carga...");
    }

    ObjectMapper objectMapper = createObjectMapper();
    ClassPathResource resource = new ClassPathResource("data/contenidos-data.json");

    try (InputStream inputStream = resource.getInputStream()) {
      List<Map<String, Object>> contenidosData = objectMapper.readValue(
          inputStream,
          new TypeReference<List<Map<String, Object>>>() {
          });

      int contenidosCreados = 0;
      for (Map<String, Object> contenidoData : contenidosData) {
        try {
          // Validar que el contenido tenga los campos mínimos requeridos
          if (contenidoData.get("tipo") == null || contenidoData.get("obraId") == null) {
            log.debug("Omitiendo contenido sin tipo o obraId válidos");
            continue;
          }

          ContenidoRequestDTO contenidoRequest = mapearDatosContenido(contenidoData);
          contenidoService.agregarContenido(contenidoRequest);
          contenidosCreados++;
          log.debug("Contenido creado: {} - {}",
              contenidoRequest.getTipo(), contenidoRequest.getIsbn());
        } catch (Exception e) {
          log.warn("Error al crear contenido {}: {}",
              contenidoData.get("isbn"), e.getMessage());
        }
      }
      log.info("Se crearon {} contenidos exitosamente.", contenidosCreados);
    }
  }

  private ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  private EditorialRequestDTO mapearDatosEditorial(Map<String, Object> editorialData) {
    return EditorialRequestDTO.builder()
        .nombre((String) editorialData.get("nombre"))
        .pais((String) editorialData.get("pais"))
        .website((String) editorialData.get("website"))
        .build();
  }

  private AutorRequestDTO mapearDatosAutor(Map<String, Object> autorData) {
    return AutorRequestDTO.builder()
        .nombre((String) autorData.get("nombre"))
        .biografia((String) autorData.get("biografia"))
        .fechaNacimiento(parsearFecha((String) autorData.get("fechaNacimiento")))
        .nacionalidad((String) autorData.get("nacionalidad"))
        .foto((String) autorData.get("foto"))
        .build();
  }

  private GeneroRequestDTO mapearDatosGenero(Map<String, Object> generoData) {
    return GeneroRequestDTO.builder()
        .nombre((String) generoData.get("nombre"))
        .descripcion((String) generoData.get("descripcion"))
        .parentId(parsearLong(generoData.get("parentId")))
        .nivel(parsearInteger(generoData.get("nivel")))
        .build();
  }

  private ObraRequestDTO mapearDatosObra(Map<String, Object> obraData) {
    ObraRequestDTO.ObraRequestDTOBuilder builder = ObraRequestDTO.builder();

    // Campos básicos
    builder.titulo((String) obraData.get("titulo"))
        .descripcion((String) obraData.get("descripcion"))
        .anioPublicacion(parsearInteger(obraData.get("anioPublicacion")))
        .isbn((String) obraData.get("isbn"))
        .editorialId(parsearLong(obraData.get("editorialId")));

    // Idiomas - puede ser String o List<String>
    Object idiomaObj = obraData.get("idioma");
    List<String> idiomas = new ArrayList<>();
    if (idiomaObj instanceof List) {
      List<?> idiomaList = (List<?>) idiomaObj;
      idiomas = idiomaList.stream()
          .filter(String.class::isInstance)
          .map(String.class::cast)
          .collect(Collectors.toList());
    } else if (idiomaObj instanceof String) {
      idiomas.add((String) idiomaObj);
    }
    builder.idioma(idiomas);

    // AutorIds - VALIDACIÓN para clave compuesta
    Object autorIdsObj = obraData.get("autorIds");
    List<Long> autorIds = new ArrayList<>();
    if (autorIdsObj instanceof List) {
      List<?> autorIdsList = (List<?>) autorIdsObj;
      autorIds = autorIdsList.stream()
          .map(this::parsearLong)
          .filter(id -> id != null && id > 0) // FILTRAR IDs VÁLIDOS
          .collect(Collectors.toList());
    }

    // VALIDACIÓN CRÍTICA: Si no hay autorIds válidos, omitir obra
    if (autorIds.isEmpty()) {
      log.warn("Obra '{}' no tiene autorIds válidos, omitiendo creación", obraData.get("titulo"));
      return null; // Retornar null para omitir esta obra
    }

    // VERIFICAR QUE LOS AUTORES EXISTAN EN LA BASE DE DATOS
    List<Long> autorIdsValidados = new ArrayList<>();
    for (Long autorId : autorIds) {
      try {
        // Verificar que el autor existe
        if (autorService.obtenerAutorPorId(autorId) != null) {
          autorIdsValidados.add(autorId);
        } else {
          log.warn("Autor con ID {} no existe, omitiendo de la obra '{}'", autorId, obraData.get("titulo"));
        }
      } catch (Exception e) {
        log.warn("Error al verificar autor con ID {}: {}", autorId, e.getMessage());
      }
    }

    if (autorIdsValidados.isEmpty()) {
      log.warn("Obra '{}' no tiene autorIds válidos existentes, omitiendo creación", obraData.get("titulo"));
      return null;
    }

    builder.autorIds(autorIdsValidados);

    // AutorRoles - VALIDACIÓN
    Object autorRolesObj = obraData.get("autorRoles");
    List<String> autorRoles = new ArrayList<>();
    if (autorRolesObj instanceof List) {
      List<?> autorRolesList = (List<?>) autorRolesObj;
      autorRoles = autorRolesList.stream()
          .filter(String.class::isInstance)
          .map(String.class::cast)
          .filter(role -> role != null && !role.trim().isEmpty())
          .collect(Collectors.toList());
    }

    // IMPORTANTE: Asegurar que hay un rol por cada autor
    while (autorRoles.size() < autorIdsValidados.size()) {
      autorRoles.add("Autor"); // Rol por defecto
    }

    // Si hay más roles que autores, truncar
    if (autorRoles.size() > autorIdsValidados.size()) {
      autorRoles = autorRoles.subList(0, autorIdsValidados.size());
    }

    builder.autorRoles(autorRoles);

    // GeneroIds - convertir a List<Long>
    Object generoIdsObj = obraData.get("generoIds");
    List<Long> generoIds = new ArrayList<>();
    if (generoIdsObj instanceof List) {
      List<?> generoIdsList = (List<?>) generoIdsObj;
      generoIds = generoIdsList.stream()
          .map(this::parsearLong)
          .filter(id -> id != null && id > 0)
          .collect(Collectors.toList());
    }
    builder.generoIds(generoIds);

    return builder.build();
  }

  private BeneficioRequestDTO mapearDatosBeneficio(Map<String, Object> beneficioData) {
    return BeneficioRequestDTO.builder()
        .categoriaId(parsearLong(beneficioData.get("categoriaId")))
        .nombre((String) beneficioData.get("nombre"))
        .descripcion((String) beneficioData.get("descripcion"))
        .icono((String) beneficioData.get("icono"))
        .tipoDato((String) beneficioData.get("tipoDato"))
        .activo(parsearBoolean(beneficioData.get("activo")))
        .build();
  }

  private PlanRequestDTO mapearDatosPlan(Map<String, Object> planData) {
    return PlanRequestDTO.builder()
        .nombre((String) planData.get("nombre"))
        .descripcion((String) planData.get("descripcion"))
        .descripcionCorta((String) planData.get("descripcionCorta"))
        .precioMensual(parsearInteger(planData.get("precioMensual")))
        .precioAnual(parsearInteger(planData.get("precioAnual")))
        .diasPrueba(parsearInteger(planData.get("diasPrueba")))
        .periodoFacturacion((String) planData.get("periodoFacturacion"))
        .activo(parsearBoolean(planData.get("activo")))
        .build();
  }

  private PlanBeneficioRequestDTO mapearDatosPlanBeneficio(Map<String, Object> planBeneficioData) {
    return PlanBeneficioRequestDTO.builder()
        .planId(parsearLong(planBeneficioData.get("planId")))
        .beneficioId(parsearLong(planBeneficioData.get("beneficioId")))
        .valor((String) planBeneficioData.get("valor"))
        .activo(parsearBoolean(planBeneficioData.get("activo")))
        .build();
  }

  private SerieRequestDTO mapearDatosSerie(Map<String, Object> serieData) {
    return SerieRequestDTO.builder()
        .nombre((String) serieData.get("nombre"))
        .descripcion((String) serieData.get("descripcion"))
        .numeroVolumenes(parsearInteger(serieData.get("numeroVolumenes")))
        .completa(parsearBoolean(serieData.get("completa")))
        .build();
  }

  private UsuarioAdminDTO mapearDatosUsuario(Map<String, Object> usuarioData) {
    UsuarioAdminDTO usuarioDTO = new UsuarioAdminDTO();

    usuarioDTO.setUsername((String) usuarioData.get("username"));
    usuarioDTO.setEmail((String) usuarioData.get("email"));
    usuarioDTO.setPassword((String) usuarioData.get("password"));
    usuarioDTO.setActivo(parsearBoolean(usuarioData.get("activo")));

    // Mapear fecha de registro
    String fechaRegistroStr = (String) usuarioData.get("fechaRegistro");
    if (fechaRegistroStr != null) {
      try {
        usuarioDTO.setFechaRegistro(LocalDateTime.parse(fechaRegistroStr));
      } catch (Exception e) {
        log.warn("Error al parsear fecha de registro para {}: {}",
            usuarioData.get("username"), e.getMessage());
        usuarioDTO.setFechaRegistro(LocalDateTime.now());
      }
    } else {
      usuarioDTO.setFechaRegistro(LocalDateTime.now());
    }

    // Mapear roles
    Object rolesObj = usuarioData.get("roles");
    Set<String> roles = new HashSet<>();
    if (rolesObj instanceof List) {
      List<?> rolesList = (List<?>) rolesObj;
      roles = rolesList.stream()
          .filter(String.class::isInstance)
          .map(String.class::cast)
          .collect(Collectors.toSet());
    }
    usuarioDTO.setRoles(roles);

    return usuarioDTO;
  }

  private ContenidoRequestDTO mapearDatosContenido(Map<String, Object> contenidoData) {
    ContenidoRequestDTO.ContenidoRequestDTOBuilder builder = ContenidoRequestDTO.builder();

    // Campos básicos de Contenido
    builder.tipo(TipoContenido.valueOf((String) contenidoData.get("tipo")))
        .obraId(parsearLong(contenidoData.get("obraId")))
        .editorialId(parsearLong(contenidoData.get("editorialId")))
        .portadaUrl((String) contenidoData.get("portadaUrl"))
        .sinopsis((String) contenidoData.get("sinopsis"))
        .precio(parsearInteger(contenidoData.get("precio")))
        .enVenta(parsearBoolean(contenidoData.get("enVenta")))
        .puedeSerPrestado(parsearBoolean(contenidoData.get("puedeSerPrestado")))
        .isbn((String) contenidoData.get("isbn"));

    // Campos de ContenidoFisico
    if (contenidoData.containsKey("stockDisponible")) {
      builder.stockDisponible(parsearInteger(contenidoData.get("stockDisponible")))
          .minStock(parsearInteger(contenidoData.get("minStock")))
          .ubicacionFisica((String) contenidoData.get("ubicacionFisica"));

      String formatoFisicoStr = (String) contenidoData.get("formatoFisico");
      if (formatoFisicoStr != null) {
        builder.formatoFisico(FormatoFisico.valueOf(formatoFisicoStr));
      }
    }

    // Campos de ContenidoDigital
    if (contenidoData.containsKey("tamanioArchivo")) {
      builder.tamanioArchivo(parsearBigDecimal(contenidoData.get("tamanioArchivo")))
          .formatoDigital((String) contenidoData.get("formatoDigital"))
          .permiteDescarga(parsearBoolean(contenidoData.get("permiteDescarga")))
          .licencias(parsearInteger(contenidoData.get("licencias")));

      String tipoLicenciaStr = (String) contenidoData.get("tipoLicencia");
      if (tipoLicenciaStr != null) {
        builder.tipoLicencia(TipoLicenciaDigital.valueOf(tipoLicenciaStr));
      }
    }

    // Campos específicos por tipo
    mapearCamposEspecificosPorTipo(contenidoData, builder);

    return builder.build();
  }

  private void mapearCamposEspecificosPorTipo(Map<String, Object> contenidoData,
      ContenidoRequestDTO.ContenidoRequestDTOBuilder builder) {
    // Campos de LibroFisico
    if (contenidoData.containsKey("paginas")) {
      builder.paginas(parsearInteger(contenidoData.get("paginas")));
    }

    // Campos de PublicacionIlustrada
    if (contenidoData.containsKey("ilustrador")) {
      builder.ilustrador((String) contenidoData.get("ilustrador"))
          .volumen(parsearInteger(contenidoData.get("volumen")));
    }

    // Campos de Manga
    if (contenidoData.containsKey("sentidoLectura")) {
      builder.sentidoLectura((String) contenidoData.get("sentidoLectura"));
    }

    // Campos de Comic
    if (contenidoData.containsKey("colorido")) {
      builder.colorido(parsearBoolean(contenidoData.get("colorido")));
    }

    // Campos de RevistaPeriodica
    if (contenidoData.containsKey("periodicidad")) {
      builder.periodicidad((String) contenidoData.get("periodicidad"))
          .edicion((String) contenidoData.get("edicion"))
          .issn((String) contenidoData.get("issn"));
    }

    // Campos de AudioLibro
    if (contenidoData.containsKey("duracionAudioLibro")) {
      builder.duracionAudioLibro((String) contenidoData.get("duracionAudioLibro"))
          .narrador((String) contenidoData.get("narrador"))
          .calidadAudio((String) contenidoData.get("calidadAudio"));
    }

    // Campos de MaterialEducativo
    if (contenidoData.containsKey("nivelEducativo")) {
      builder.nivelEducativo((String) contenidoData.get("nivelEducativo"))
          .asignatura((String) contenidoData.get("asignatura"))
          .recursosEducativos((String) contenidoData.get("recursosEducativos"));
    }

    // Campos de ContenidoMultimedia
    if (contenidoData.containsKey("duracionMultimedia")) {
      builder.duracionMultimedia((String) contenidoData.get("duracionMultimedia"))
          .calidadMultimedia((String) contenidoData.get("calidadMultimedia"));
    }

    // Campos de serie si existen
    if (contenidoData.containsKey("ordenEnSerie")) {
      builder.ordenEnSerie(parsearInteger(contenidoData.get("ordenEnSerie")));
    }
  }

  // Métodos de parseo
  private LocalDate parsearFecha(String fechaString) {
    if (fechaString == null || fechaString.trim().isEmpty()) {
      return null;
    }
    try {
      return LocalDate.parse(fechaString);
    } catch (Exception e) {
      log.warn("Error al parsear fecha: {}", fechaString);
      return null;
    }
  }

  private LocalDateTime parsearFechaHora(String fechaHoraString) {
    if (fechaHoraString == null || fechaHoraString.trim().isEmpty()) {
      return null;
    }
    try {
      // Intentar con diferentes formatos
      if (fechaHoraString.contains("T")) {
        return LocalDateTime.parse(fechaHoraString);
      } else {
        // Formato alternativo si no tiene T
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(fechaHoraString, formatter);
      }
    } catch (Exception e) {
      log.warn("Error al parsear fecha-hora: {}", fechaHoraString);
      return LocalDateTime.now();
    }
  }

  private Long parsearLong(Object valor) {
    if (valor == null) {
      return null;
    }
    if (valor instanceof Number) {
      return ((Number) valor).longValue();
    }
    try {
      return Long.valueOf(valor.toString());
    } catch (NumberFormatException e) {
      log.warn("Error al parsear Long: {}", valor);
      return null;
    }
  }

  private Integer parsearInteger(Object valor) {
    if (valor == null) {
      return null;
    }
    if (valor instanceof Number) {
      return ((Number) valor).intValue();
    }
    try {
      return Integer.valueOf(valor.toString());
    } catch (NumberFormatException e) {
      log.warn("Error al parsear Integer: {}", valor);
      return null;
    }
  }

  private BigDecimal parsearBigDecimal(Object valor) {
    if (valor == null) {
      return null;
    }
    if (valor instanceof Number) {
      return BigDecimal.valueOf(((Number) valor).doubleValue());
    }
    try {
      return new BigDecimal(valor.toString());
    } catch (NumberFormatException e) {
      log.warn("Error al parsear BigDecimal: {}", valor);
      return null;
    }
  }

  private boolean parsearBoolean(Object valor) {
    if (valor == null) {
      return false;
    }
    if (valor instanceof Boolean) {
      return (Boolean) valor;
    }
    return Boolean.parseBoolean(valor.toString());
  }

  private void cargarMetodosPago() throws IOException {
    log.info("Cargando datos de métodos de pago...");

    ClassPathResource resource = new ClassPathResource("data/metodos-pago-data.json");
    ObjectMapper mapper = new ObjectMapper();

    try (InputStream inputStream = resource.getInputStream()) {
      List<Map<String, Object>> metodosData = mapper.readValue(inputStream,
          new TypeReference<List<Map<String, Object>>>() {
          });

      int cargados = 0;
      int omitidos = 0;

      for (Map<String, Object> data : metodosData) {
        try {
          String nombre = (String) data.get("nombre");

          // Verificar si ya existe el método de pago
          if (metodoPagoService.obtenerMetodoPagoPorNombre(nombre).isPresent()) {
            omitidos++;
            continue;
          }

          // Crear método de pago
          MetodoPagoRequestDTO metodoPagoDTO = mapearDatosMetodoPago(data);

          metodoPagoService.crearMetodoPago(metodoPagoDTO);
          cargados++;
        } catch (Exception e) {
          omitidos++;
          log.warn("Error al crear método de pago {}: {}", data.get("nombre"), e.getMessage());
        }

      }
      log.info("Métodos de pago cargados: {} creados, {} omitidos", cargados, omitidos);
    }
  }

  private void actualizarSuscripcionesExistentes() {
    log.info("Verificando suscripciones existentes para actualizar modalidadPago...");

    try {
      List<SuscripcionResponseDTO> suscripciones = suscripcionService.obtenerTodasLasSuscripciones();

      int actualizadas = 0;
      for (SuscripcionResponseDTO suscripcion : suscripciones) {
        // Si la suscripción no tiene modalidadPago establecida
        if (suscripcion.getModalidadPago() == null || suscripcion.getModalidadPago().trim().isEmpty()) {
          // Establecer "mensual" como valor por defecto
          try {
            suscripcionService.cambiarPlan(suscripcion.getId(), suscripcion.getPlanId(), "mensual");
            actualizadas++;
            log.debug("Suscripción {} actualizada con modalidadPago: mensual", suscripcion.getId());
          } catch (Exception e) {
            log.warn("Error al actualizar modalidadPago para suscripción {}: {}",
                suscripcion.getId(), e.getMessage());
          }
        }
      }

      if (actualizadas > 0) {
        log.info("Se actualizaron {} suscripciones existentes con modalidadPago por defecto", actualizadas);
      } else {
        log.info("No se encontraron suscripciones que requirieran actualización de modalidadPago");
      }

    } catch (Exception e) {
      log.warn("Error al verificar suscripciones existentes: {}", e.getMessage());
    }
  }

  private MetodoPagoRequestDTO mapearDatosMetodoPago(Map<String, Object> metodoPagoData) {
    return MetodoPagoRequestDTO.builder()
        .tipo((String) metodoPagoData.get("tipo"))
        .nombre((String) metodoPagoData.get("nombre"))
        .descripcion((String) metodoPagoData.get("descripcion"))
        .requiereAutorizacion(parsearBoolean(metodoPagoData.get("requiereAutorizacion")))
        .activo(parsearBoolean(metodoPagoData.get("activo")))
        .build();
  }
}