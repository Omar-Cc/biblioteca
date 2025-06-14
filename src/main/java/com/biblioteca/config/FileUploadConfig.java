package com.biblioteca.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.MultipartConfigElement;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

  // Cambiar a un directorio fuera del código fuente
  @Value("${app.upload.dir:uploads}")
  private String uploadDir;

  @Value("${app.upload.max-file-size:5MB}")
  private String maxFileSize;

  @Bean
  public MultipartConfigElement multipartConfigElement() {
    MultipartConfigFactory factory = new MultipartConfigFactory();
    factory.setMaxFileSize(DataSize.parse(maxFileSize));
    factory.setMaxRequestSize(DataSize.parse("10MB"));
    return factory.createMultipartConfig();
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Mapear la URL /uploads/** al directorio físico
    registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:" + System.getProperty("user.dir") + "/" + uploadDir + "/");
  }

  @PostConstruct
  public void createUploadDirectories() {
    try {
      // Crear directorio en la raíz del proyecto
      Path uploadPath = Paths.get(uploadDir);
      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
        System.out.println("✅ Directorio base creado: " + uploadPath.toAbsolutePath());
      }

      Path perfilesPath = uploadPath.resolve("perfiles");
      if (!Files.exists(perfilesPath)) {
        Files.createDirectories(perfilesPath);
        System.out.println("✅ Directorio perfiles creado: " + perfilesPath.toAbsolutePath());
      }
    } catch (IOException e) {
      throw new RuntimeException("No se pudieron crear los directorios de uploads", e);
    }
  }
}