package com.biblioteca.models.acceso;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.biblioteca.enums.NivelLector;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = { "usuario", "informacionPerfil" })
@ToString(exclude = { "usuario", "informacionPerfil" })
@Entity
public class Perfil {

	// ========== IDENTIFICACIÓN DEL PERFIL ==========
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String nombreVisible;

	@Column(length = 500)
	private String descripcionPerfil;

	// ========== RELACIÓN CON USUARIO ==========
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@OneToOne(mappedBy = "perfil", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private InformacionPerfil informacionPerfil;

	// ========== CONFIGURACIÓN VISUAL DEL PERFIL ==========
	@Column(name = "foto_perfil", length = 500)
	private String fotoPerfil; // URL o path a la imagen

	@Column(length = 20)
	private String temaPreferido; // "CLARO", "OSCURO", "AUTO"

	@Column(length = 10)
	private String colorAcento; // Color personalizado para el perfil

	// ========== CONFIGURACIÓN BÁSICA ==========
	@Column(length = 10)
	private String idiomaPreferido; // "ES", "EN", "FR"

	@Builder.Default
	private Integer limitePrestamosDesignado = 2; // Límite específico para este perfil

	// ========== ESTADO Y TIPO DE PERFIL ==========
	@Builder.Default
	private Boolean activo = true;

	@Builder.Default
	private Boolean esPerfilPrincipal = false;

	@Builder.Default
	private Boolean esInfantil = false;

	// ========== CONFIGURACIÓN DE PRIVACIDAD ==========
	@Builder.Default
	private Boolean perfilPublico = false; // Si otros usuarios pueden ver este perfil

	@Builder.Default
	private Boolean mostrarHistorialLectura = false;

	@Builder.Default
	private Boolean permitirRecomendaciones = true;

	// ========== CONFIGURACIÓN DE CONTENIDO ==========
	@Builder.Default
	private Boolean filtroContenidoAdulto = false; // Para perfiles infantiles o familiares

	@Column(length = 20)
	@Builder.Default
	private String nivelRestriccion = "NINGUNO"; // "NINGUNO", "MODERADO", "ESTRICTO"

	// ========== CONFIGURACIÓN DE NOTIFICACIONES POR PERFIL ==========
	@Builder.Default
	private Boolean notificacionesPrestamos = true;

	@Builder.Default
	private Boolean notificacionesVencimientos = true;

	@Builder.Default
	private Boolean notificacionesRecomendaciones = true;

	@Builder.Default
	private Boolean notificacionesEventos = false;

	@Builder.Default
	private Boolean notificacionesNuevasAdquisiciones = false;

	@Builder.Default
	private Boolean notificacionesGruposLectura = false;

	// ========== CONFIGURACIÓN AVANZADA DE NOTIFICACIONES ==========
	@Column(length = 20)
	@Builder.Default
	private String frecuenciaResumen = "SEMANAL"; // "DIARIO", "SEMANAL", "MENSUAL", "NUNCA"

	@Builder.Default
	private Boolean recibirResumenActividad = true;

	@Column(length = 500)
	private String horariosPreferidos; // JSON: {"inicio": "09:00", "fin": "22:00"}

	// ========== INFORMACIÓN GEOGRÁFICA ==========
	@Column(length = 100)
	private String ubicacion; // Ubicación opcional para eventos locales

	// ========== ESTADÍSTICAS BÁSICAS DEL PERFIL ==========
	@Builder.Default
	private Integer vecesUsado = 0; // Cuántas veces se ha usado este perfil

	private LocalDateTime ultimoUso;

	// ========== FECHAS Y METADATOS ==========
	private LocalDate fechaCreacion;
	private LocalDate fechaModificacion;
	private LocalDateTime ultimaActividad;

	// ========== MÉTODOS HELPER ==========
	public InformacionPerfil getInformacionPerfil() {
		if (informacionPerfil == null && this.id != null) {
			informacionPerfil = InformacionPerfil.builder()
					.id(this.id)
					.perfil(this)
					.fechaCreacion(LocalDateTime.now())
					.ultimaActividad(LocalDateTime.now())
					.ultimaActualizacion(LocalDateTime.now())
					.nivelLectura(NivelLector.PRINCIPIANTE)
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
		}
		return informacionPerfil;
	}

	public void registrarUso() {
		this.vecesUsado = (this.vecesUsado == null ? 0 : this.vecesUsado) + 1;
		this.ultimoUso = LocalDateTime.now();
		this.ultimaActividad = LocalDateTime.now();
	}

	public boolean puedeAccederContenidoAdulto() {
		return !esInfantil && !filtroContenidoAdulto;
	}

	public boolean esPerfilActivo() {
		return activo && (usuario != null && usuario.estaActivo());
	}
}