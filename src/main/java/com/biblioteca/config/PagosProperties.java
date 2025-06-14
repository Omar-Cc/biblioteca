package com.biblioteca.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.pagos.simulacion")
public class PagosProperties {
  private int tasaExito = 90;
  private long tiempoProcesamiento = 2000;
  private boolean habilitarFallosAleatorios = true;
}