package com.smartbody.comun.seguridad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;

/**
 * Carga solo la clave publica del JWT (public_key.pem). Es lo unico que necesitan los servicios
 * que no emiten tokens. El servicio auth desactiva esta configuracion
 * ({@code smartbody.jwt.solo-publica=false}) y registra el par completo de claves.
 */
@Configuration
@ConditionalOnProperty(name = "smartbody.jwt.solo-publica", havingValue = "true", matchIfMissing = true)
public class ClavePublicaConfig {

    private static final Logger log = LoggerFactory.getLogger(ClavePublicaConfig.class);

    @Bean
    public RSAPublicKey clavePublicaJwt(JwtProperties propiedades) {
        Path archivo = Path.of(propiedades.keysDir()).resolve(ClavesPem.ARCHIVO_PUBLICA);
        try {
            RSAPublicKey clave = ClavesPem.leerPublica(archivo);
            log.info("Clave publica del JWT cargada desde {}", archivo.toAbsolutePath());
            return clave;
        } catch (Exception e) {
            throw new IllegalStateException("No se encontro la clave publica del JWT en " + archivo.toAbsolutePath()
                    + ". Copia public_key.pem del servicio auth (variable SMARTBODY_KEYS_DIR).", e);
        }
    }
}
