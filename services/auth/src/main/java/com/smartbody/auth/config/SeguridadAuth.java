package com.smartbody.auth.config;

import com.smartbody.comun.seguridad.RutasPublicas;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Rutas publicas de auth (registro y login) y codificador de contrasenas. */
@Configuration
public class SeguridadAuth {

    @Bean
    public RutasPublicas rutasPublicas() {
        return RutasPublicas.de("/api/auth/register", "/api/auth/login");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
