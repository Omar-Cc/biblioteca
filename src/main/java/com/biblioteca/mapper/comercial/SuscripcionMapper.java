package com.biblioteca.mapper.comercial;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.biblioteca.dto.comercial.SuscripcionRequestDTO;
import com.biblioteca.dto.comercial.SuscripcionResponseDTO;
import com.biblioteca.models.comercial.Suscripcion;

@Mapper(componentModel = "spring", uses = { MetodoPagoMapper.class })
public interface SuscripcionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "metodoPago", ignore = true)
    @Mapping(target = "historialPagos", ignore = true)
    @Mapping(target = "estadoAnterior", ignore = true)
    @Mapping(target = "fechaCambioEstado", ignore = true)
    @Mapping(target = "motivoCambio", ignore = true)
    Suscripcion toEntity(SuscripcionRequestDTO dto);

    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "planId", source = "plan.id")
    @Mapping(target = "metodoPagoId", source = "metodoPago.id")
    @Mapping(target = "usuarioNombre", source = "usuario.username")
    @Mapping(target = "emailUsuario", source = "usuario.email")
    @Mapping(target = "planNombre", source = "plan.nombre")
    @Mapping(target = "metodoPagoNombre", source = "metodoPago.nombre")
    @Mapping(target = "precioMensual", source = "plan.precioMensual")
    @Mapping(target = "precioAnual", source = "plan.precioAnual")
    @Mapping(target = "descripcionPlan", source = "plan.descripcion")
    @Mapping(target = "diasPrueba", source = "plan.diasPrueba")
    @Mapping(target = "diasTranscurridos", ignore = true)
    @Mapping(target = "diasTotales", ignore = true)
    @Mapping(target = "diasRestantes", ignore = true)
    @Mapping(target = "diasParaRenovacion", ignore = true)
    @Mapping(target = "historialPagos", ignore = true)
    SuscripcionResponseDTO toResponseDTO(Suscripcion entity);

    List<SuscripcionResponseDTO> toResponseDTOList(List<Suscripcion> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "metodoPago", ignore = true)
    @Mapping(target = "historialPagos", ignore = true)
    @Mapping(target = "estadoAnterior", ignore = true)
    @Mapping(target = "fechaCambioEstado", ignore = true)
    @Mapping(target = "motivoCambio", ignore = true)
    void updateEntityFromDTO(SuscripcionRequestDTO dto, @MappingTarget Suscripcion entity);

    @AfterMapping
    default void calculateDays(@MappingTarget SuscripcionResponseDTO dto, Suscripcion entity) {
        LocalDate hoy = LocalDate.now();

        if (entity.getFechaInicio() != null) {
            dto.setDiasTranscurridos((int) ChronoUnit.DAYS.between(entity.getFechaInicio(), hoy));
        }

        if (entity.getFechaRenovacion() != null) {
            dto.setDiasParaRenovacion((int) ChronoUnit.DAYS.between(hoy, entity.getFechaRenovacion()));
            dto.setDiasRestantes((int) ChronoUnit.DAYS.between(hoy, entity.getFechaRenovacion()));
        }

        if (entity.getFechaInicio() != null && entity.getFechaRenovacion() != null) {
            dto.setDiasTotales((int) ChronoUnit.DAYS.between(entity.getFechaInicio(), entity.getFechaRenovacion()));
        }

        // Calcular precio según modalidad de pago
        if (entity.getPlan() != null) {
            if ("ANUAL".equals(entity.getModalidadPago())) {
                dto.setPrecio(entity.getPlan().getPrecioAnual());
            } else {
                dto.setPrecio(entity.getPlan().getPrecioMensual());
            }
        }
    }
}