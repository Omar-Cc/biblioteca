package com.biblioteca.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableCaching
@EnableScheduling
@ConditionalOnProperty(value = "dashboard.cache.enabled", havingValue = "true", matchIfMissing = true)
public class DashboardCacheConfig {

  @Bean
  public CacheManager dashboardCacheManager() {
    return new ConcurrentMapCacheManager(
        "dashboard-completo",
        "dashboard-periodo");
  }

  /**
   * Limpia automáticamente el caché cada 15 minutos para mantener datos frescos
   */
  @Scheduled(fixedRate = 900000) // 15 minutos
  public void limpiarCacheAutomaticamente() {
    dashboardCacheManager().getCacheNames().forEach(cacheName -> {
      var cache = dashboardCacheManager().getCache(cacheName);
      if (cache != null) {
        cache.clear();
      }
    });
  }
}