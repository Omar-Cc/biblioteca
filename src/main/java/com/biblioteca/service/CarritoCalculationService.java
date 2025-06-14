package com.biblioteca.service;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.biblioteca.models.comercial.ItemCarrito;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio dedicado a los cálculos del carrito
 * Separado para mantener la lógica de cálculo organizada y reutilizable
 */
@Slf4j
@Service
public class CarritoCalculationService {
    
    /**
     * Calcular totales del carrito
     */
    public CalculationResult calcularTotales(Set<ItemCarrito> items) {
        if (items == null || items.isEmpty()) {
            return CalculationResult.empty();
        }
        
        int subtotal = 0;
        int totalDescuentos = 0;
        int cantidadTotal = 0;
        
        for (ItemCarrito item : items) {
            int precio = item.getPrecio() != null ? item.getPrecio() : 0;
            int cantidad = item.getCantidad() != null ? item.getCantidad() : 0;
            int descuento = item.getDescuento() != null ? item.getDescuento() : 0;
            
            subtotal += precio * cantidad;
            totalDescuentos += descuento * cantidad;
            cantidadTotal += cantidad;
        }
        
        int total = subtotal - totalDescuentos;
        
        return CalculationResult.builder()
            .subtotal(subtotal)
            .totalDescuentos(totalDescuentos)
            .total(total)
            .cantidadItems(cantidadTotal)
            .build();
    }
    
    /**
     * Calcular costo de envío estimado
     */
    public int calcularCostoEnvio(Set<ItemCarrito> items, String codigoPostal) {
        // Lógica simple de costo de envío
        // En producción sería más compleja considerando peso, dimensiones, ubicación, etc.
        
        if (items == null || items.isEmpty()) {
            return 0;
        }
        
        CalculationResult totales = calcularTotales(items);
        
        // Envío gratis para compras mayores a $50,000
        if (totales.getTotal() >= 50000) {
            return 0;
        }
        
        // $5,000 costo base de envío
        return 5000;
    }
    
    /**
     * Calcular descuento por volumen
     */
    public int calcularDescuentoVolumen(Set<ItemCarrito> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        
        int cantidadTotal = items.stream()
            .mapToInt(item -> item.getCantidad() != null ? item.getCantidad() : 0)
            .sum();
        
        // 5% descuento por más de 5 items
        if (cantidadTotal > 5) {
            CalculationResult totales = calcularTotales(items);
            return (int) (totales.getSubtotal() * 0.05);
        }
        
        return 0;
    }
    
    /**
     * Verificar si todos los precios están actualizados
     */
    public boolean verificarPreciosActualizados(Set<ItemCarrito> items) {
        // TODO: Implementar verificación contra precios actuales en base de datos
        // Por ahora retornamos true
        return true;
    }
    
    /**
     * Clase interna para resultados de cálculos
     */
    public static class CalculationResult {
        private int subtotal;
        private int totalDescuentos;
        private int total;
        private int cantidadItems;
        
        public static CalculationResult empty() {
            return new CalculationResult(0, 0, 0, 0);
        }
        
        public static CalculationResult builder() {
            return new CalculationResult();
        }
        
        public CalculationResult() {}
        
        public CalculationResult(int subtotal, int totalDescuentos, int total, int cantidadItems) {
            this.subtotal = subtotal;
            this.totalDescuentos = totalDescuentos;
            this.total = total;
            this.cantidadItems = cantidadItems;
        }
        
        public CalculationResult subtotal(int subtotal) {
            this.subtotal = subtotal;
            return this;
        }
        
        public CalculationResult totalDescuentos(int totalDescuentos) {
            this.totalDescuentos = totalDescuentos;
            return this;
        }
        
        public CalculationResult total(int total) {
            this.total = total;
            return this;
        }
        
        public CalculationResult cantidadItems(int cantidadItems) {
            this.cantidadItems = cantidadItems;
            return this;
        }
        
        public CalculationResult build() {
            return this;
        }
        
        // Getters
        public int getSubtotal() { return subtotal; }
        public int getTotalDescuentos() { return totalDescuentos; }
        public int getTotal() { return total; }
        public int getCantidadItems() { return cantidadItems; }
    }
}
