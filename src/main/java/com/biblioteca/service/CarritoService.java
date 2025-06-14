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
    
    // Para uso interno principalmente
    Optional<Carrito> obtenerEntidadCarritoPorPerfil(Long perfilId);
    
    // Aplicar cupón de descuento
    boolean aplicarCuponDescuentoPorPerfil(Long perfilId, String codigoCupon);
    
    // Verificar disponibilidad de ítems
    boolean verificarDisponibilidadItemsPorPerfil(Long perfilId);
    
    // ============ NUEVOS MÉTODOS SUGERIDOS ============
    
    // Fusionar carritos (útil cuando un usuario anónimo se registra)
    CarritoResponseDTO fusionarCarritos(Long carritoDestinoId, Long carritoOrigenId);
    
    // Obtener cantidad total de items en carrito
    int obtenerCantidadTotalItemsPorPerfil(Long perfilId);
    
    // Calcular valor total sin descuentos
    Integer calcularSubtotalPorPerfil(Long perfilId);
    
    // Limpiar carritos abandonados (tarea programada)
    int limpiarCarritosAbandonados(int diasInactividad);
    
    // Validar límites de cantidad por item
    boolean validarLimiteCantidadItem(Long contenidoId, int cantidadSolicitada);
    
    // Transferir carrito entre perfiles del mismo usuario
    boolean transferirCarritoEntrePerfiles(Long perfilOrigenId, Long perfilDestinoId);
    
    // Guardar carrito para compra posterior
    boolean guardarCarritoParaCompra(Long perfilId, String nombreGuardado);
    
    // Restaurar carrito guardado
    CarritoResponseDTO restaurarCarritoGuardado(Long perfilId, Long carritoGuardadoId);
}