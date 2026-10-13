package com.smartbody.comun.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Autentica las llamadas entre servicios a {@code /internal/**} con un secreto compartido
 * ({@code X-Internal-Token}). Nginx nunca reenvia esas rutas desde fuera del despliegue.
 */
@Component
public class TokenInternoFilter extends OncePerRequestFilter {

    public static final String CABECERA = "X-Internal-Token";
    public static final String ROL_SERVICIO = "ROLE_SERVICIO_INTERNO";

    private final String tokenEsperado;

    public TokenInternoFilter(@Value("${smartbody.interno.token:}") String tokenEsperado) {
        this.tokenEsperado = tokenEsperado;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String recibido = request.getHeader(CABECERA);
        if (recibido != null && !tokenEsperado.isBlank() && coinciden(recibido, tokenEsperado)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            var autenticacion = new UsernamePasswordAuthenticationToken("servicio-interno", null,
                    List.of(new SimpleGrantedAuthority(ROL_SERVICIO)));
            SecurityContextHolder.getContext().setAuthentication(autenticacion);
        }
        filterChain.doFilter(request, response);
    }

    private static boolean coinciden(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
