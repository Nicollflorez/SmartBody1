package com.smartbody.auth.security;

import com.smartbody.comun.seguridad.ClavesPem;
import com.smartbody.comun.seguridad.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Par de claves RSA del JWT. Solo el servicio auth tiene la clave privada (firma); la publica se
 * comparte con los demas servicios para que verifiquen los tokens. Si el directorio no contiene
 * las claves, se genera un par nuevo la primera vez para facilitar la instalacion local.
 */
@Configuration
public class RsaKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(RsaKeyProvider.class);

    private final Path directorio;
    private RSAPrivateKey clavePrivada;
    private RSAPublicKey clavePublica;

    public RsaKeyProvider(JwtProperties propiedades) {
        this.directorio = Path.of(propiedades.keysDir());
        inicializar();
    }

    void inicializar() {
        try {
            Path privada = directorio.resolve(ClavesPem.ARCHIVO_PRIVADA);
            Path publica = directorio.resolve(ClavesPem.ARCHIVO_PUBLICA);
            if (!Files.exists(privada) || !Files.exists(publica)) {
                generarYGuardar(privada, publica);
            }
            this.clavePrivada = ClavesPem.leerPrivada(privada);
            this.clavePublica = ClavesPem.leerPublica(publica);
            log.info("Claves RSA para JWT cargadas desde {}", directorio.toAbsolutePath());
        } catch (Exception e) {
            throw new IllegalStateException(
                    "No fue posible preparar las claves RSA para JWT en " + directorio.toAbsolutePath(), e);
        }
    }

    @Bean
    public RSAPrivateKey clavePrivadaJwt() {
        return clavePrivada;
    }

    @Bean
    public RSAPublicKey clavePublicaJwt() {
        return clavePublica;
    }

    public RSAPrivateKey getClavePrivada() {
        return clavePrivada;
    }

    public RSAPublicKey getClavePublica() {
        return clavePublica;
    }

    private void generarYGuardar(Path privada, Path publica) throws Exception {
        KeyPairGenerator generador = KeyPairGenerator.getInstance("RSA");
        generador.initialize(2048);
        KeyPair par = generador.generateKeyPair();
        Files.createDirectories(directorio);
        ClavesPem.escribir(privada, "PRIVATE KEY", par.getPrivate().getEncoded());
        ClavesPem.escribir(publica, "PUBLIC KEY", par.getPublic().getEncoded());
        log.warn("Se generó un nuevo par de claves RSA en {}. No lo publiques en el repositorio.",
                directorio.toAbsolutePath());
    }
}
