package com.smartbody.auth.security;

import com.smartbody.auth.entity.Rol;
import com.smartbody.auth.entity.Usuario;
import com.smartbody.comun.seguridad.JwtProperties;
import com.smartbody.comun.seguridad.JwtVerificador;
import com.smartbody.comun.seguridad.UsuarioAutenticado;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** auth emite el token con la clave privada y cualquier servicio lo verifica con la publica. */
class JwtServiceTest {

    @TempDir
    Path directorioClaves;

    private JwtProperties propiedades;
    private RsaKeyProvider claves;
    private JwtService jwtService;
    private JwtVerificador verificador;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        propiedades = new JwtProperties("smartbody-api", 60, directorioClaves.toString());
        claves = new RsaKeyProvider(propiedades);
        jwtService = new JwtService(claves.getClavePrivada(), propiedades);
        verificador = new JwtVerificador(claves.getClavePublica(), propiedades);

        usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", 7L);
        usuario.setNombre("Usuario Demo");
        usuario.setEmail("demo@smartbody.com");
        usuario.setPasswordHash("hash-no-usado");
        usuario.agregarRol(new Rol(Rol.USUARIO));
    }

    @Test
    void elTokenLlevaLaIdentidadYLosRolesDelUsuario() {
        Optional<UsuarioAutenticado> autenticado = verificador.verificar(jwtService.generarToken(usuario));

        assertThat(autenticado).isPresent();
        assertThat(autenticado.get().id()).isEqualTo(7L);
        assertThat(autenticado.get().email()).isEqualTo("demo@smartbody.com");
        assertThat(autenticado.get().roles()).containsExactly(Rol.USUARIO);
        assertThat(autenticado.get().esAdministrador()).isFalse();
    }

    @Test
    void rechazaUnTokenManipulado() {
        assertThat(verificador.verificar(jwtService.generarToken(usuario) + "alteracion")).isEmpty();
    }

    @Test
    void rechazaUnTextoQueNoEsUnToken() {
        assertThat(verificador.verificar("esto-no-es-un-jwt")).isEmpty();
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

        assertThat(verificador.verificar(tokenVencido)).isEmpty();
    }

    @Test
    void rechazaUnTokenFirmadoConOtraClave() {
        JwtProperties otras = new JwtProperties("smartbody-api", 60, directorioClaves.resolve("otras").toString());
        RsaKeyProvider otrasClaves = new RsaKeyProvider(otras);
        JwtVerificador otroVerificador = new JwtVerificador(otrasClaves.getClavePublica(), otras);

        assertThat(otroVerificador.verificar(jwtService.generarToken(usuario))).isEmpty();
    }
}
