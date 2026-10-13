package com.smartbody.auth.service;

import com.smartbody.auth.dto.AuthResponse;
import com.smartbody.auth.dto.LoginRequest;
import com.smartbody.auth.dto.RegistroRequest;
import com.smartbody.auth.entity.Rol;
import com.smartbody.auth.entity.Usuario;
import com.smartbody.auth.repository.RolRepository;
import com.smartbody.auth.repository.UsuarioRepository;
import com.smartbody.auth.security.JwtService;
import com.smartbody.comun.error.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pruebas de registro e inicio de sesion (RF01, RF02). */
class AuthServiceTest {

    private UsuarioRepository usuarioRepository;
    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void prepararServicio() {
        usuarioRepository = mock(UsuarioRepository.class);
        RolRepository rolRepository = mock(RolRepository.class);
        jwtService = mock(JwtService.class);
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(usuarioRepository, rolRepository, passwordEncoder, jwtService);

        when(rolRepository.findByNombre(Rol.USUARIO)).thenReturn(Optional.of(new Rol(Rol.USUARIO)));
        when(jwtService.generarToken(any())).thenReturn("jwt-de-prueba");
        when(jwtService.segundosDeValidez()).thenReturn(10800L);
    }

    @Test
    void registraElUsuarioGuardandoLaContrasenaHasheada() {
        when(usuarioRepository.existsByEmailIgnoreCase("nuevo@smartbody.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(llamada -> llamada.getArgument(0));

        AuthResponse respuesta = authService.registrar(
                new RegistroRequest("Nuevo Usuario", "nuevo@smartbody.com", "Clave12345"));

        assertThat(respuesta.token()).isEqualTo("jwt-de-prueba");
        assertThat(respuesta.usuario().email()).isEqualTo("nuevo@smartbody.com");
        ArgumentCaptor<Usuario> capturado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(capturado.capture());
        assertThat(capturado.getValue().getPasswordHash()).startsWith("$2");
        assertThat(passwordEncoder.matches("Clave12345", capturado.getValue().getPasswordHash())).isTrue();
    }

    @Test
    void rechazaElRegistroCuandoElCorreoYaExiste() {
        when(usuarioRepository.existsByEmailIgnoreCase("demo@smartbody.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar(new RegistroRequest("Otro", "demo@smartbody.com", "Clave12345")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Ya existe una cuenta");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void iniciaSesionConCredencialesCorrectas() {
        when(usuarioRepository.findByEmailIgnoreCase("demo@smartbody.com"))
                .thenReturn(Optional.of(usuarioDemo("Smartbody123")));

        AuthResponse respuesta = authService.login(new LoginRequest("demo@smartbody.com", "Smartbody123"));

        assertThat(respuesta.token()).isEqualTo("jwt-de-prueba");
        assertThat(respuesta.tipo()).isEqualTo("Bearer");
        assertThat(respuesta.expiraEnSegundos()).isEqualTo(10800L);
    }

    @Test
    void rechazaElLoginConContrasenaIncorrecta() {
        when(usuarioRepository.findByEmailIgnoreCase("demo@smartbody.com"))
                .thenReturn(Optional.of(usuarioDemo("Smartbody123")));

        assertThatThrownBy(() -> authService.login(new LoginRequest("demo@smartbody.com", "incorrecta")))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getEstado()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void rechazaElLoginDeUnUsuarioInexistente() {
        when(usuarioRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nadie@smartbody.com", "Clave12345")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Correo o contraseña incorrectos");
    }

    @Test
    void rechazaElLoginDeUnaCuentaDesactivada() {
        Usuario desactivado = usuarioDemo("Smartbody123");
        desactivado.setActivo(false);
        when(usuarioRepository.findByEmailIgnoreCase("demo@smartbody.com")).thenReturn(Optional.of(desactivado));

        assertThatThrownBy(() -> authService.login(new LoginRequest("demo@smartbody.com", "Smartbody123")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("desactivada");
    }

    private Usuario usuarioDemo(String password) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Demo");
        usuario.setEmail("demo@smartbody.com");
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.agregarRol(new Rol(Rol.USUARIO));
        return usuario;
    }
}
