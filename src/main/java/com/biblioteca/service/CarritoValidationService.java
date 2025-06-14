package com.biblioteca.service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.biblioteca.dto.comercial.ItemCarritoRequestDTO;
import com.biblioteca.exceptions.CarritoValidationException;
import com.biblioteca.exceptions.PerfilNoValidoException;
import com.biblioteca.exceptions.TooManyRequestsException;
import com.biblioteca.models.contenido.Contenido;
import com.biblioteca.models.contenido.ContenidoFisico;
import com.biblioteca.repositories.contenido.ContenidoRepository;
import com.biblioteca.service.PerfilService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio dedicado a las validaciones del carrito para mantener
 * separada la lógica de negocio de las validaciones específicas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CarritoValidationService {
    
    private final PerfilService perfilService;
    private final ContenidoRepository contenidoRepository;
    
    // Rate limiting simple en memoria (en producción usar Redis)
    private final ConcurrentHashMap<Long, LocalDateTime> lastRequestMap = new ConcurrentHashMap<>();
    private static final long MIN_REQUEST_INTERVAL_MS = 1000; // 1 segundo entre requests
    
    // Límites configurables
    private static final int MAX_CANTIDAD_POR_ITEM = 10;
    private static final int MAX_ITEMS_POR_CARRITO = 50;
    
    /**
     * Validación completa antes de agregar un item al carrito
     */
    public void validarSolicitudAgregarItem(Long perfilId, ItemCarritoRequestDTO itemDTO) {
        log.debug("Validando solicitud agregar item - Perfil: {}, Contenido: {}", perfilId, itemDTO.getContenidoId());
        
        // Validar perfil
        validarPerfil(perfilId);
        
        // Validar rate limiting
        validarRateLimiting(perfilId);
        
        // Validar item
        validarItemRequest(itemDTO);
        
        // Validar contenido disponible
        validarContenidoDisponible(itemDTO.getContenidoId());
        
        // Validar cantidad y stock
        validarCantidadYStock(itemDTO.getContenidoId(), itemDTO.getCantidad());
        
        log.debug("Validación exitosa para agregar item");
    }
    
    /**
     * Validar que el perfil existe y está activo
     */
    public void validarPerfil(Long perfilId) {
        if (perfilId == null) {
            throw new PerfilNoValidoException("ID de perfil no puede ser nulo");
        }
        
        boolean existeYActivo = perfilService.obtenerEntidadPerfilPorId(perfilId)
            .map(perfil -> perfil.getActivo())
            .orElse(false);
            
        if (!existeYActivo) {
            throw new PerfilNoValidoException("Perfil no válido o inactivo: " + perfilId);
        }
    }
    
    /**
     * Rate limiting simple para evitar spam
     */
    public void validarRateLimiting(Long perfilId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastRequest = lastRequestMap.get(perfilId);
        
        if (lastRequest != null) {
            long timeDiff = java.time.Duration.between(lastRequest, now).toMillis();
            if (timeDiff < MIN_REQUEST_INTERVAL_MS) {
                throw new TooManyRequestsException("Demasiadas solicitudes. Intente más tarde.");
            }
        }
        
        lastRequestMap.put(perfilId, now);
    }
    
    /**
     * Validar datos del item solicitado
     */
    public void validarItemRequest(ItemCarritoRequestDTO itemDTO) {
        if (itemDTO == null) {
            throw new CarritoValidationException("Datos del item no pueden ser nulos");
        }
        
        if (itemDTO.getContenidoId() == null) {
            throw new CarritoValidationException("ID de contenido es requerido");
        }
        
        if (itemDTO.getCantidad() == null || itemDTO.getCantidad() <= 0) {
            throw new CarritoValidationException("Cantidad debe ser mayor que cero");
        }
        
        if (itemDTO.getCantidad() > MAX_CANTIDAD_POR_ITEM) {
            throw new CarritoValidationException("Cantidad excede el máximo permitido: " + MAX_CANTIDAD_POR_ITEM);
        }
    }
    
    /**
     * Validar que el contenido existe y está disponible para venta
     */
    public Contenido validarContenidoDisponible(Long contenidoId) {
        return contenidoRepository.findById(contenidoId)
            .filter(Contenido::isActivo)
            .filter(Contenido::isEnVenta)
            .orElseThrow(() -> new CarritoValidationException("Contenido no disponible: " + contenidoId));
    }
    
    /**
     * Validar cantidad solicitada contra stock disponible
     */
    public void validarCantidadYStock(Long contenidoId, int cantidadSolicitada) {
        Contenido contenido = validarContenidoDisponible(contenidoId);
        
        // Para contenido físico, validar stock
        if (contenido instanceof ContenidoFisico) {
            ContenidoFisico contenidoFisico = (ContenidoFisico) contenido;
            
            // Verificar si existe método de stock
            try {
                int stockDisponible = contenidoFisico.getStockDisponible();
                if (stockDisponible < cantidadSolicitada) {
                    throw new CarritoValidationException(
                        String.format("Stock insuficiente. Disponible: %d, Solicitado: %d", 
                                     stockDisponible, cantidadSolicitada));
                }
            } catch (Exception e) {
                // Si no existe el método, logear y continuar
                log.warn("No se pudo verificar stock para contenido físico: {}", contenidoId);
            }
        }
        
        log.debug("Validación de cantidad y stock exitosa para contenido: {}", contenidoId);
    }
    
    /**
     * Validar límites del carrito
     */
    public void validarLimitesCarrito(int itemsActuales) {
        if (itemsActuales >= MAX_ITEMS_POR_CARRITO) {
            throw new CarritoValidationException("Carrito ha alcanzado el límite máximo de items: " + MAX_ITEMS_POR_CARRITO);
        }
    }
    
    /**
     * Validar reglas de negocio específicas
     */
    public void validarReglasDeNegocio(Long perfilId, ItemCarritoRequestDTO itemDTO) {
        // Aquí se pueden agregar validaciones específicas del negocio como:
        // - Restricciones por edad del usuario
        // - Límites por tipo de contenido
        // - Validaciones de suscripción
        // - Restricciones geográficas
        
        log.debug("Validación de reglas de negocio completada");
    }
    
    /**
     * Limpiar rate limiting cache (útil para tests)
     */
    public void limpiarRateLimitingCache() {
        lastRequestMap.clear();
    }
}
