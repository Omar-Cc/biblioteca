package com.biblioteca.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

  @RequestMapping("/error")
  public String handleError(HttpServletRequest request, Model model) {
    Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    Object error = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
    Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

    if (status != null) {
      Integer statusCode = Integer.valueOf(status.toString());
      model.addAttribute("status", statusCode);
      model.addAttribute("error", error);
      model.addAttribute("path", path);

      System.out.println("🚨 Error " + statusCode + " en ruta: " + path);

      // Redirigir a templates específicos
      switch (statusCode) {
        case 403:
          return "error/403";
        case 404:
          return "error/404";
        case 500:
          return "error/500";
        default:
          return "error/error";
      }
    }

    return "error/error";
  }
}