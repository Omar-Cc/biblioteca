package com.biblioteca.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app.suscripciones")
public class SuscripcionProperties {
    private int periodoGraciaDias = 3;
    private int intentosCobroMax = 3;
    private List<Integer> diasNotificacionPrevia = List.of(7, 3, 1);
}