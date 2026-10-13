package com.smartbody.comun.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbody.comun.error.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Seguridad comun a todos los microservicios: sin sesiones, JWT en cada peticion, rutas
 * internas solo con el token de servicio y panel de administracion solo para administradores.
 */
@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final TokenInternoFilter tokenInternoFilter;
    private final ObjectMapper objectMapper;

    public SeguridadConfig(JwtAuthenticationFilter jwtFilter, TokenInternoFilter tokenInternoFilter,
                           ObjectMapper objectMapper) {
        this.jwtFilter = jwtFilter;
        this.tokenInternoFilter = tokenInternoFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectProvider<RutasPublicas> rutasPublicas)
            throws Exception {
        String[] publicas = rutasPublicas.getIfAvailable(RutasPublicas::ninguna).patrones().toArray(String[]::new);
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> {
                    rutas.requestMatchers("/api/ping", "/error", "/actuator/health/**", "/actuator/prometheus").permitAll();
                    if (publicas.length > 0) {
                        rutas.requestMatchers(publicas).permitAll();
                    }
                    rutas.requestMatchers("/internal/**").hasAuthority(TokenInternoFilter.ROL_SERVICIO)
                         .requestMatchers("/api/admin/**").hasAuthority(UsuarioAutenticado.ROL_ADMINISTRADOR)
                         .anyRequest().authenticated();
                })
                .exceptionHandling(manejo -> manejo
                        .authenticationEntryPoint((peticion, respuesta, excepcion) ->
                                escribirError(respuesta, HttpStatus.UNAUTHORIZED,
                                        "Debes iniciar sesión para continuar."))
                        .accessDeniedHandler((peticion, respuesta, excepcion) ->
                                escribirError(respuesta, HttpStatus.FORBIDDEN,
                                        "No tienes permisos para realizar esta acción.")))
                .addFilterBefore(tokenInternoFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void escribirError(HttpServletResponse respuesta, HttpStatus estado, String mensaje) throws IOException {
        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.de(estado.value(), mensaje)));
    }
}
