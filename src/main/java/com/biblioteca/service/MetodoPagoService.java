package com.biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.biblioteca.dto.comercial.MetodoPagoRequestDTO;
import com.biblioteca.dto.comercial.MetodoPagoResponseDTO;
import com.biblioteca.models.comercial.MetodoPago;

public interface MetodoPagoService {
    // Operaciones CRUD básicas
    MetodoPagoResponseDTO crearMetodoPago(MetodoPagoRequestDTO metodoPagoDTO);
    
    Optional<MetodoPagoResponseDTO> obtenerMetodoPagoPorId(Long id);
    
    List<MetodoPagoResponseDTO> obtenerTodosLosMetodosPago();
    
    List<MetodoPagoResponseDTO> obtenerMetodosPagoActivos();
    
    Optional<MetodoPagoResponseDTO> actualizarMetodoPago(Long id, MetodoPagoRequestDTO metodoPagoDTO);
    
    boolean eliminarMetodoPago(Long id);
    
    // Para uso interno principalmente
    Optional<MetodoPago> obtenerEntidadMetodoPagoPorId(Long id);
    
    // Operaciones específicas
    boolean activarMetodoPago(Long id);
    
    boolean desactivarMetodoPago(Long id);
    
    // Verificación de métodos de pago
    boolean verificarRequiereAutorizacion(Long id);
    
    // Obtener métodos de pago por nombre
    Optional<MetodoPagoResponseDTO> obtenerMetodoPagoPorNombre(String nombre);
}