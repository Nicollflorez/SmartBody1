package com.smartbody.comun.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Optional;

/** Verifica un JWT con la clave publica y construye el usuario autenticado a partir de sus claims. */
@Component
public class JwtVerificador {

    private static final Logger log = LoggerFactory.getLogger(JwtVerificador.class);

    private final RSAPublicKey clavePublica;
    private final JwtProperties propiedades;

    public JwtVerificador(RSAPublicKey clavePublica, JwtProperties propiedades) {
        this.clavePublica = clavePublica;
        this.propiedades = propiedades;
    }

    /** Usuario del token si la firma, el emisor y la vigencia son validos; vacio en cualquier otro caso. */
    @SuppressWarnings("unchecked")
    public Optional<UsuarioAutenticado> verificar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(clavePublica)
                    .requireIssuer(propiedades.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            // El claim numerico llega como Integer o Long segun su tamano.
            Number uid = claims.get("uid", Number.class);
            List<String> roles = claims.get("roles", List.class);
            return Optional.of(new UsuarioAutenticado(uid == null ? null : uid.longValue(), claims.getSubject(),
                    roles == null ? List.of() : List.copyOf(roles), token));
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token JWT rechazado: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
