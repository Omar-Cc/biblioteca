package com.biblioteca.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;

@Data
@Component
@ConfigurationProperties(prefix = "app.pdf")
public class PdfProperties {
    
    // Configuración general
    private boolean enabled = true;
    private String templateBasePath = "templates/pdf/";
    private String encoding = "UTF-8";
    
    // Configuración de página
    private float pageWidth = 595;
    private float pageHeight = 842;
    private String pageFormat = "A4";
    
    // Configuración de fuentes
    private String defaultFont = "Arial";
    private int defaultFontSize = 12;
    private boolean embedFonts = true;
    private String fontEncoding = "UTF-8";
    
    // Configuración de márgenes
    private float marginTop = 50;
    private float marginBottom = 50;
    private float marginLeft = 50;
    private float marginRight = 50;
    
    // Configuración de metadata
    private String author = "Biblioteca Digital";
    private String creator = "Sistema Biblioteca Digital v1.0";
    private String producer = "iText PDF Library";
    private String subject = "Documentos Biblioteca Digital";
    private String keywords = "factura,biblioteca,digital,pdf";
    
    // Configuración de compresión
    private boolean compressImages = true;
    private float imageCompressionQuality = 0.9f;
    private boolean compressStreams = true;
    
    // Configuración de seguridad
    private boolean enableSecurity = false;
    private String ownerPassword = "";
    private String userPassword = "";
    private boolean allowPrinting = true;
    private boolean allowCopy = true;
    private boolean allowModify = false;
    private boolean allowAnnotations = false;
    
    // Configuración de generación
    private int timeoutSeconds = 30;
    private int maxFileSizeMb = 10;
    private String tempDirectory = System.getProperty("java.io.tmpdir") + "/biblioteca-pdf/";
    
    // Plantillas
    private Map<String, String> templates = new HashMap<>();
    
    // Nombres de archivo
    private Map<String, String> filename = new HashMap<>();
    
    // Configuración de caché
    private Cache cache = new Cache();
    
    @Data
    public static class Cache {
        private boolean enabled = false;
        private int maxSize = 100;
        private int expiryMinutes = 60;
    }
}