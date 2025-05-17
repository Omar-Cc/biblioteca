package com.biblioteca.service;

import java.util.Optional;

import com.biblioteca.dto.comercial.ItemCarritoRequestDTO;
import com.biblioteca.dto.comercial.ItemCarritoResponseDTO;
import com.biblioteca.dto.comercial.CarritoResponseDTO;
import com.biblioteca.models.comercial.Carrito;

public interface CarritoService {
    // Obtener carrito de un usuario
    Optional<CarritoResponseDTO> obtenerCarritoPorPerfil(Long perfilId);
    
    // Agregar un ítem al carrito
    CarritoResponseDTO agregarItemAlCarritoPorPerfil(Long perfilId, ItemCarritoRequestDTO itemDTO);
    
    // Actualizar cantidad de un ítem
    Optional<ItemCarritoResponseDTO> actualizarCantidadItemPorPerfil(Long perfilId, Long itemId, int cantidad);
    
    // Eliminar un ítem del carrito
    boolean eliminarItemDelCarritoPorPerfil(Long perfilId, Long itemId);
    
    // Vaciar el carrito completo
    boolean vaciarCarritoPorPerfil(Long perfilId);
    
    // Obtener total del carrito
    double calcularTotalCarritoPorPerfil(Long perfilId);
    
    // Para uso interno principalmente
    Optional<Carrito> obtenerEntidadCarritoPorPerfil(Long perfilId);
    
    // Aplicar cupón de descuento
    boolean aplicarCuponDescuentoPorPerfil(Long perfilId, String codigoCupon);
    
    // Verificar disponibilidad de ítems
    boolean verificarDisponibilidadItemsPorPerfil(Long perfilId);
}