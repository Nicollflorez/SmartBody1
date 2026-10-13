package com.smartbody.comun.seguridad;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/** Lectura y escritura de claves RSA en formato PEM. */
public final class ClavesPem {

    public static final String ARCHIVO_PRIVADA = "private_key.pem";
    public static final String ARCHIVO_PUBLICA = "public_key.pem";

    private ClavesPem() {
    }

    public static RSAPrivateKey leerPrivada(Path archivo) throws Exception {
        byte[] datos = decodificar(Files.readString(archivo, StandardCharsets.UTF_8));
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(datos));
    }

    public static RSAPublicKey leerPublica(Path archivo) throws Exception {
        byte[] datos = decodificar(Files.readString(archivo, StandardCharsets.UTF_8));
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(datos));
    }

    public static void escribir(Path destino, String tipo, byte[] contenido) throws IOException {
        String base64 = Base64.getMimeEncoder(64, System.lineSeparator().getBytes(StandardCharsets.UTF_8))
                .encodeToString(contenido);
        String pem = "-----BEGIN " + tipo + "-----" + System.lineSeparator()
                + base64 + System.lineSeparator()
                + "-----END " + tipo + "-----" + System.lineSeparator();
        Files.writeString(destino, pem, StandardCharsets.UTF_8);
    }

    private static byte[] decodificar(String pem) {
        String base64 = pem.replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }
}
