package com.biblioteca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.biblioteca.service.UsuarioService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final UsuarioService usuarioService;
  private final PasswordEncoder passwordEncoder;
  private final CustomAuthenticationSuccessHandler authSuccessHandler;
  private final CustomLogoutSuccessHandler logoutSuccessHandler;
  private final SessionValidationFilter sessionValidationFilter;

  public SecurityConfig(UsuarioService usuarioService, PasswordEncoder passwordEncoder,
      CustomAuthenticationSuccessHandler authSuccessHandler,
      CustomLogoutSuccessHandler logoutSuccessHandler,
      SessionValidationFilter sessionValidationFilter) {
    this.usuarioService = usuarioService;
    this.passwordEncoder = passwordEncoder;
    this.authSuccessHandler = authSuccessHandler;
    this.logoutSuccessHandler = logoutSuccessHandler;
    this.sessionValidationFilter = sessionValidationFilter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .addFilterBefore(sessionValidationFilter, SessionManagementFilter.class)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(
                AntPathRequestMatcher.antMatcher("/css/**"),
                AntPathRequestMatcher.antMatcher("/js/**"),
                AntPathRequestMatcher.antMatcher("/image/**"),
                AntPathRequestMatcher.antMatcher("/uploads/**"),
                AntPathRequestMatcher.antMatcher("/webjars/**"),
                AntPathRequestMatcher.antMatcher("/"),
                AntPathRequestMatcher.antMatcher("/home"),
                AntPathRequestMatcher.antMatcher("/catalogo/**"),
                AntPathRequestMatcher.antMatcher("/carrito"),
                AntPathRequestMatcher.antMatcher("/buscar/**"),
                AntPathRequestMatcher.antMatcher("/api/buscar/**"),
                AntPathRequestMatcher.antMatcher("/test-busqueda"),
                AntPathRequestMatcher.antMatcher("/registro"),
                AntPathRequestMatcher.antMatcher("/login"),
                AntPathRequestMatcher.antMatcher("/error"),
                AntPathRequestMatcher.antMatcher("/403"),
                AntPathRequestMatcher.antMatcher("/404"),
                AntPathRequestMatcher.antMatcher("/500"),
                AntPathRequestMatcher.antMatcher("/beneficios/**"),
                AntPathRequestMatcher.antMatcher("/planes/**"),
                AntPathRequestMatcher.antMatcher("/nosotros"),
                AntPathRequestMatcher.antMatcher("/contacto"),
                AntPathRequestMatcher.antMatcher("/politica-de-privacidad"),
                AntPathRequestMatcher.antMatcher("/terminos-y-condiciones"),
                AntPathRequestMatcher.antMatcher("/licencias-de-contenido"),
                AntPathRequestMatcher.antMatcher("/loader"),
                AntPathRequestMatcher.antMatcher("/favicon.ico"))
            .permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/admin/**")).hasRole("ADMIN")
            .requestMatchers(AntPathRequestMatcher.antMatcher("/mi-cuenta/**")).hasAnyRole("LECTOR", "ADMIN")
            .requestMatchers(AntPathRequestMatcher.antMatcher("/ordenes/**")).hasAnyRole("LECTOR", "ADMIN")
            .anyRequest().authenticated())
        .csrf(csrf -> csrf
            .csrfTokenRepository(new HttpSessionCsrfTokenRepository()))
        .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .usernameParameter("username")
            .passwordParameter("password")
            .successHandler(authSuccessHandler)
            .failureUrl("/login?error=true")
            .permitAll())
        .logout(logout -> logout
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            .logoutSuccessHandler(logoutSuccessHandler)
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .permitAll())
        .sessionManagement(session -> session
            .maximumSessions(10)
            .maxSessionsPreventsLogin(false))
        .exceptionHandling(exceptions -> exceptions
            .accessDeniedPage("/403")
            .authenticationEntryPoint((request, response, authException) -> {
                // Guardar la URL solicitada manualmente antes de redirigir
                String targetUrl = request.getRequestURL().toString();
                String queryString = request.getQueryString();
                if (queryString != null && !queryString.isEmpty()) {
                    targetUrl += "?" + queryString;
                }
                request.getSession().setAttribute("REQUESTED_URL", targetUrl);
                System.out.println("🔍 DEBUG - SecurityConfig: URL guardada automáticamente: " + targetUrl);
                response.sendRedirect("/login");
            }));

    return http.build();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(usuarioService);
    authProvider.setPasswordEncoder(this.passwordEncoder);
    return authProvider;
  }
}