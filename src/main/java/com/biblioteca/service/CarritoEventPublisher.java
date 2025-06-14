package com.biblioteca.service;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.biblioteca.events.ItemAgregadoEvent;
import com.biblioteca.events.ItemEliminadoEvent;
import com.biblioteca.events.CarritoVaciadoEvent;
import com.biblioteca.events.CuponAplicadoEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para publicar eventos relacionados con el carrito
 * Esto permite un diseño desacoplado donde otros servicios pueden
 * reaccionar a los cambios del carrito sin acoplamiento directo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CarritoEventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * Publicar evento cuando se agrega un item al carrito
     */
    public void publicarItemAgregado(Long perfilId, Long contenidoId, String contenidoTipo, int cantidad, Integer precio) {
        log.debug("Publicando evento item agregado - Perfil: {}, Contenido: {}", perfilId, contenidoId);
        
        ItemAgregadoEvent event = ItemAgregadoEvent.builder()
            .perfilId(perfilId)
            .contenidoId(contenidoId)
            .contenidoTipo(contenidoTipo)
            .cantidad(cantidad)
            .precio(precio)
            .timestamp(LocalDateTime.now())
            .build();
            
        eventPublisher.publishEvent(event);
    }
    
    /**
     * Publicar evento cuando se elimina un item del carrito
     */
    public void publicarItemEliminado(Long perfilId, Long contenidoId, String contenidoTipo, int cantidad) {
        log.debug("Publicando evento item eliminado - Perfil: {}, Contenido: {}", perfilId, contenidoId);
        
        ItemEliminadoEvent event = ItemEliminadoEvent.builder()
            .perfilId(perfilId)
            .contenidoId(contenidoId)
            .contenidoTipo(contenidoTipo)
            .cantidad(cantidad)
            .timestamp(LocalDateTime.now())
            .build();
            
        eventPublisher.publishEvent(event);
    }
    
    /**
     * Publicar evento cuando se vacía el carrito
     */
    public void publicarCarritoVaciado(Long perfilId, int itemsEliminados, Integer valorTotal) {
        log.debug("Publicando evento carrito vaciado - Perfil: {}, Items: {}", perfilId, itemsEliminados);
        
        CarritoVaciadoEvent event = CarritoVaciadoEvent.builder()
            .perfilId(perfilId)
            .itemsEliminados(itemsEliminados)
            .valorTotal(valorTotal)
            .timestamp(LocalDateTime.now())
            .build();
            
        eventPublisher.publishEvent(event);
    }
    
    /**
     * Publicar evento cuando se aplica un cupón
     */
    public void publicarCuponAplicado(Long perfilId, String codigoCupon, Integer descuentoAplicado) {
        log.debug("Publicando evento cupón aplicado - Perfil: {}, Cupón: {}", perfilId, codigoCupon);
        
        CuponAplicadoEvent event = CuponAplicadoEvent.builder()
            .perfilId(perfilId)
            .codigoCupon(codigoCupon)
            .descuentoAplicado(descuentoAplicado)
            .timestamp(LocalDateTime.now())
            .build();
            
        eventPublisher.publishEvent(event);
    }
}
