package com.smartbody.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracion del JWT. Ningun valor sensible vive en el codigo fuente:
 * las claves RSA se leen desde {@code keysDir}, configurable por variable de entorno.
 */
@ConfigurationProperties(prefix = "smartbody.jwt")
public record JwtProperties(String issuer, long expirationMinutes, String keysDir) {

    public JwtProperties {
        if (issuer == null || issuer.isBlank()) {
            issuer = "smartbody-api";
        }
        if (expirationMinutes <= 0) {
            expirationMinutes = 180;
        }
        if (keysDir == null || keysDir.isBlank()) {
            keysDir = "./keys";
        }
    }
}
