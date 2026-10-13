package com.smartbody.security;

import com.smartbody.entity.Rol;
import com.smartbody.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/** Generacion y validacion de JWT firmados con RSA (RS256). */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final RsaKeyProvider claves;
    private final JwtProperties propiedades;

    public JwtService(RsaKeyProvider claves, JwtProperties propiedades) {
        this.claves = claves;
        this.propiedades = propiedades;
    }

    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plus(propiedades.expirationMinutes(), ChronoUnit.MINUTES);
        List<String> roles = usuario.getRoles().stream().map(Rol::getNombre).toList();

        return Jwts.builder()
                .issuer(propiedades.issuer())
                .subject(usuario.getEmail())
                .claim("uid", usuario.getId())
                .claim("roles", roles)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expiracion))
                .signWith(claves.getClavePrivada(), Jwts.SIG.RS256)
                .compact();
    }

    /** Devuelve el correo del usuario si el token es valido; vacio en cualquier otro caso. */
    public Optional<String> emailDeTokenValido(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(claves.getClavePublica())
                    .requireIssuer(propiedades.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token JWT rechazado: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public long segundosDeValidez() {
        return propiedades.expirationMinutes() * 60;
    }
}
