package com.biblioteca.service.impl;

import com.biblioteca.service.PdfService;
import com.biblioteca.config.PdfProperties;
import com.biblioteca.service.FacturaService;
import com.biblioteca.service.PagoService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

// Imports para iText html2pdf
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PdfServiceImpl implements PdfService {

  private final TemplateEngine templateEngine;
  private final FacturaService facturaService;
  private final PdfProperties pdfProperties;
  private final PagoService pagoService;

  public PdfServiceImpl(TemplateEngine templateEngine, @Lazy FacturaService facturaService,
      PdfProperties pdfProperties, @Lazy PagoService pagoService) {
    this.templateEngine = templateEngine;
    this.facturaService = facturaService;
    this.pdfProperties = pdfProperties;
    this.pagoService = pagoService;
  }

  @Override
  public byte[] generarPdfDesdeTemplate(String template, Map<String, Object> variables) {
    try {
      // Agregar variables comunes
      variables.put("fechaGeneracion", LocalDateTime.now());
      variables.put("año", LocalDateTime.now().getYear());

      Context context = new Context();
      variables.forEach(context::setVariable);

      String html = templateEngine.process(template, context);

      return convertirHtmlAPdf(html);

    } catch (Exception e) {
      log.error("Error generando PDF desde template {}: {}", template, e.getMessage());
      throw new RuntimeException("Error generando PDF", e);
    }
  }

  @Override
  public byte[] generarFacturaOrden(Long facturaId) {
    // Cambiamos para obtener la ENTIDAD completa, no el DTO
    return facturaService.obtenerEntidadFacturaPorId(facturaId)
        .map(factura -> {
          Map<String, Object> variables = new HashMap<>();
          variables.put("factura", factura); // Ahora es una entidad con todas las relaciones
          variables.put("tipo", "ORDEN");

          return generarPdfDesdeTemplate("pdf/factura-orden", variables);
        })
        .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
  }

  @Override
  public byte[] generarFacturaSuscripcion(Long suscripcionId, String periodo) {
    Map<String, Object> variables = new HashMap<>();
    variables.put("suscripcionId", suscripcionId);
    variables.put("periodo", periodo);
    variables.put("tipo", "SUSCRIPCION");

    return generarPdfDesdeTemplate("pdf/factura-suscripcion", variables);
  }

  @Override
  public byte[] generarReciboPago(Long pagoId) {
    // Obtener el pago completo con todas sus relaciones
    return pagoService.obtenerEntidadPagoPorId(pagoId)
        .map(pago -> {
          Map<String, Object> variables = new HashMap<>();
          variables.put("pago", pago);
          variables.put("pagoId", pagoId);
          variables.put("numeroRecibo", "REC-" + String.format("%06d", pagoId));
          variables.put("tipo", "RECIBO");

          return generarPdfDesdeTemplate("pdf/recibo-pago", variables);
        })
        .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + pagoId));
  }

  @Override
  public byte[] generarHistorialPagos(Long usuarioId, String periodo) {
    Map<String, Object> variables = new HashMap<>();
    variables.put("usuarioId", usuarioId);
    variables.put("periodo", periodo);
    variables.put("tipo", "HISTORIAL");

    return generarPdfDesdeTemplate("pdf/historial-pagos", variables);
  }

  private byte[] convertirHtmlAPdf(String html) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

      // Configurar propiedades de conversión usando la configuración
      ConverterProperties properties = new ConverterProperties();
      properties.setFontProvider(new DefaultFontProvider(
          pdfProperties.isEmbedFonts(),
          pdfProperties.isEmbedFonts(),
          pdfProperties.isEmbedFonts()));

      // Convertir HTML a PDF usando iText
      HtmlConverter.convertToPdf(html, outputStream, properties);

      return outputStream.toByteArray();

    } catch (Exception e) {
      log.error("Error convirtiendo HTML a PDF: {}", e.getMessage());
      throw new RuntimeException("Error en conversión PDF", e);
    }
  }
}