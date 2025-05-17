package com.biblioteca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
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
                AntPathRequestMatcher.antMatcher("/images/**"),
                AntPathRequestMatcher.antMatcher("/webjars/**"),
                AntPathRequestMatcher.antMatcher("/"),
                AntPathRequestMatcher.antMatcher("/home"),
                AntPathRequestMatcher.antMatcher("/catalogo/**"),
                AntPathRequestMatcher.antMatcher("/registro"),
                AntPathRequestMatcher.antMatcher("/login"),
                AntPathRequestMatcher.antMatcher("/error"),
                AntPathRequestMatcher.antMatcher("/403"),
                AntPathRequestMatcher.antMatcher("/404"),
                AntPathRequestMatcher.antMatcher("/500"),
                AntPathRequestMatcher.antMatcher("/beneficios/**"),
                AntPathRequestMatcher.antMatcher("/planes/**"))
            .permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/admin/**")).hasRole("ADMIN")
            .requestMatchers(AntPathRequestMatcher.antMatcher("/mi-cuenta/**")).hasRole("USER")
            .requestMatchers(AntPathRequestMatcher.antMatcher("/ordenes/**")).hasAnyRole("USER", "ADMIN")
            .anyRequest().authenticated())
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
        .formLogin(form -> form
            .loginPage("/login")
            .successHandler(authSuccessHandler)
            .failureUrl("/login?error=true")
            .permitAll())
        .logout(logout -> logout
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            .logoutSuccessHandler(logoutSuccessHandler)
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .permitAll())
        .exceptionHandling(exceptions -> exceptions
            .accessDeniedPage("/403"));

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