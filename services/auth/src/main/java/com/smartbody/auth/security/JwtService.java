package com.smartbody.auth.security;

import com.smartbody.auth.entity.Rol;
import com.smartbody.auth.entity.Usuario;
import com.smartbody.comun.seguridad.JwtProperties;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/** Emision de JWT firmados con RSA (RS256). La verificacion la hace la libreria comun con la clave publica. */
@Service
public class JwtService {

    private final RSAPrivateKey clavePrivada;
    private final JwtProperties propiedades;

    public JwtService(RSAPrivateKey clavePrivada, JwtProperties propiedades) {
        this.clavePrivada = clavePrivada;
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
                .signWith(clavePrivada, Jwts.SIG.RS256)
                .compact();
    }

    public long segundosDeValidez() {
        return propiedades.expirationMinutes() * 60;
    }
}
