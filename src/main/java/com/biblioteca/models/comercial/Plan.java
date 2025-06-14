package com.biblioteca.models.comercial;

import java.util.HashSet;
import java.util.Set;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;

@Data
@SuperBuilder
@NoArgsConstructor
@Entity
@EqualsAndHashCode(exclude = {"planBeneficios", "suscripciones"})
@ToString(exclude = {"planBeneficios", "suscripciones"})
public class Plan {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nombre;
	private String descripcion;
	private String descripcionCorta;
	private Integer precioMensual; // En centavos
	private Integer precioAnual; // En centavos
	private Integer diasPrueba;
	private String periodoFacturacion;

	@Builder.Default
	private boolean activo = true;

	@OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<PlanBeneficio> planBeneficios = new HashSet<>();

	@OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<Suscripcion> suscripciones = new HashSet<>();

	public void addPlanBeneficio(PlanBeneficio planBeneficio) {
		this.planBeneficios.add(planBeneficio);
		planBeneficio.setPlan(this);
	}

	public void removePlanBeneficio(PlanBeneficio planBeneficio) {
		this.planBeneficios.remove(planBeneficio);
		planBeneficio.setPlan(null);
	}

	public void addSuscripcion(Suscripcion suscripcion) {
		this.suscripciones.add(suscripcion);
		suscripcion.setPlan(this);
	}

	public void removeSuscripcion(Suscripcion suscripcion) {
		this.suscripciones.remove(suscripcion);
		suscripcion.setPlan(null);
	}
}