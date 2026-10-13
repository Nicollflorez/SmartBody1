package com.smartbody.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Carga el par de claves RSA usado para firmar y verificar los JWT.
 *
 * <p>La clave privada permanece unicamente en el servidor: nunca se expone por la API
 * ni se envia a la aplicacion Android. Si el directorio configurado no contiene las
 * claves, se genera un par nuevo la primera vez para facilitar la instalacion local.</p>
 */
@Component
public class RsaKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(RsaKeyProvider.class);
    private static final String ARCHIVO_PRIVADA = "private_key.pem";
    private static final String ARCHIVO_PUBLICA = "public_key.pem";

    private final Path directorio;
    private RSAPrivateKey clavePrivada;
    private RSAPublicKey clavePublica;

    public RsaKeyProvider(JwtProperties propiedades) {
        this.directorio = Path.of(propiedades.keysDir());
    }

    @PostConstruct
    void inicializar() {
        try {
            Path privada = directorio.resolve(ARCHIVO_PRIVADA);
            Path publica = directorio.resolve(ARCHIVO_PUBLICA);

            if (!Files.exists(privada) || !Files.exists(publica)) {
                generarYGuardar(privada, publica);
            }
            this.clavePrivada = leerClavePrivada(privada);
            this.clavePublica = leerClavePublica(publica);
            log.info("Claves RSA para JWT cargadas desde {}", directorio.toAbsolutePath());
        } catch (Exception e) {
            throw new IllegalStateException(
                    "No fue posible preparar las claves RSA para JWT en " + directorio.toAbsolutePath(), e);
        }
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
        escribirPem(privada, "PRIVATE KEY", par.getPrivate().getEncoded());
        escribirPem(publica, "PUBLIC KEY", par.getPublic().getEncoded());
        log.warn("Se generó un nuevo par de claves RSA en {}. No lo publiques en el repositorio.",
                directorio.toAbsolutePath());
    }

    private void escribirPem(Path destino, String tipo, byte[] contenido) throws IOException {
        String base64 = Base64.getMimeEncoder(64, System.lineSeparator().getBytes(StandardCharsets.UTF_8))
                .encodeToString(contenido);
        String pem = "-----BEGIN " + tipo + "-----" + System.lineSeparator()
                + base64 + System.lineSeparator()
                + "-----END " + tipo + "-----" + System.lineSeparator();
        Files.writeString(destino, pem, StandardCharsets.UTF_8);
    }

    private RSAPrivateKey leerClavePrivada(Path archivo) throws Exception {
        byte[] datos = decodificarPem(Files.readString(archivo, StandardCharsets.UTF_8));
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(datos));
    }

    private RSAPublicKey leerClavePublica(Path archivo) throws Exception {
        byte[] datos = decodificarPem(Files.readString(archivo, StandardCharsets.UTF_8));
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(datos));
    }

    private byte[] decodificarPem(String pem) {
        String base64 = pem.replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }
}
