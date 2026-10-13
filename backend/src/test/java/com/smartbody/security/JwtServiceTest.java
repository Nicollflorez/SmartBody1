package com.smartbody.security;

import com.smartbody.entity.Rol;
import com.smartbody.entity.Usuario;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Pruebas de generacion y validacion de JWT (seccion 34). */
class JwtServiceTest {

    @TempDir
    Path directorioClaves;

    private JwtProperties propiedades;
    private RsaKeyProvider claves;
    private JwtService jwtService;
    private Usuario usuario;

    @BeforeEach
    void prepararServicio() {
        propiedades = new JwtProperties("smartbody-api", 60, directorioClaves.toString());
        claves = new RsaKeyProvider(propiedades);
        claves.inicializar();
        jwtService = new JwtService(claves, propiedades);

        usuario = new Usuario();
        usuario.setNombre("Usuario Demo");
        usuario.setEmail("demo@smartbody.com");
        usuario.setPasswordHash("hash-no-usado");
        usuario.agregarRol(new Rol(Rol.USUARIO));
    }

    @Test
    void generaUnTokenValidoParaElUsuario() {
        String token = jwtService.generarToken(usuario);

        Optional<String> email = jwtService.emailDeTokenValido(token);

        assertThat(token).isNotBlank();
        assertThat(email).contains("demo@smartbody.com");
    }

    @Test
    void rechazaUnTokenManipulado() {
        String token = jwtService.generarToken(usuario) + "alteracion";

        assertThat(jwtService.emailDeTokenValido(token)).isEmpty();
    }

    @Test
    void rechazaUnTextoQueNoEsUnToken() {
        assertThat(jwtService.emailDeTokenValido("esto-no-es-un-jwt")).isEmpty();
    }

    @Test
    void rechazaUnTokenExpirado() {
        Instant hace2Horas = Instant.now().minus(2, ChronoUnit.HOURS);
        String tokenVencido = Jwts.builder()
                .issuer(propiedades.issuer())
                .subject(usuario.getEmail())
                .issuedAt(Date.from(hace2Horas))
                .expiration(Date.from(hace2Horas.plus(30, ChronoUnit.MINUTES)))
                .signWith(claves.getClavePrivada(), Jwts.SIG.RS256)
                .compact();

        assertThat(jwtService.emailDeTokenValido(tokenVencido)).isEmpty();
    }

    @Test
    void rechazaUnTokenFirmadoConOtraClave() {
        String token = jwtService.generarToken(usuario);

        JwtProperties otras = new JwtProperties("smartbody-api", 60,
                directorioClaves.resolve("otras").toString());
        RsaKeyProvider otrasClaves = new RsaKeyProvider(otras);
        otrasClaves.inicializar();
        JwtService otroServicio = new JwtService(otrasClaves, otras);

        assertThat(otroServicio.emailDeTokenValido(token)).isEmpty();
    }
}
