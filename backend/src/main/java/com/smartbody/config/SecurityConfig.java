package com.smartbody.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbody.dto.ErrorResponse;
import com.smartbody.entity.Rol;
import com.smartbody.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

/** Seguridad stateless: sin sesiones, cada peticion protegida viaja con su JWT. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/ping").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Sondas de Docker/Kubernetes y metricas de Prometheus: solo en la red interna
                        // (Nginx no reenvia /actuator salvo /actuator/health).
                        .requestMatchers("/actuator/health/**", "/actuator/prometheus").permitAll()
                        // Panel de administracion: solo cuentas con rol de administrador (403 para las demas).
                        .requestMatchers("/api/admin/**").hasAuthority(Rol.ADMINISTRADOR)
                        .anyRequest().authenticated())
                .exceptionHandling(manejo -> manejo
                        .authenticationEntryPoint((peticion, respuesta, excepcion) ->
                                escribirError(respuesta, HttpStatus.UNAUTHORIZED,
                                        "Debes iniciar sesión para continuar."))
                        .accessDeniedHandler((peticion, respuesta, excepcion) ->
                                escribirError(respuesta, HttpStatus.FORBIDDEN,
                                        "No tienes permisos para realizar esta acción.")))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void escribirError(HttpServletResponse respuesta, HttpStatus estado, String mensaje) throws java.io.IOException {
        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.de(estado.value(), mensaje)));
    }
}
