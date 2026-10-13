package com.smartbody.service;

import com.smartbody.dto.SustitucionRequest;
import com.smartbody.entity.Usuario;
import com.smartbody.exception.ApiException;
import com.smartbody.repository.EjercicioRepository;
import com.smartbody.repository.RutinaRepository;
import com.smartbody.service.reglas.GeneradorRutina;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Aislamiento de datos entre usuarios y validaciones previas a la generacion (RNF06, RF06). */
class RutinaServiceTest {

    private UsuarioService usuarioService;
    private PerfilService perfilService;
    private RutinaRepository rutinaRepository;
    private GeneradorRutina generador;
    private RutinaService rutinaService;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        usuarioService = mock(UsuarioService.class);
        perfilService = mock(PerfilService.class);
        rutinaRepository = mock(RutinaRepository.class);
        generador = mock(GeneradorRutina.class);
        rutinaService = new RutinaService(usuarioService, perfilService, mock(EjercicioRepository.class),
                rutinaRepository, generador);

        usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", 10L);
        usuario.setEmail("ana@smartbody.com");
        when(usuarioService.buscarPorEmail("ana@smartbody.com")).thenReturn(usuario);
    }

    @Test
    void unaRutinaDeOtroUsuarioSeRespondeComoInexistente() {
        when(rutinaRepository.findByIdAndUsuarioId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rutinaService.detalle("ana@smartbody.com", 99L))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getEstado())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void noSePuedeSustituirEnUnaRutinaAjena() {
        when(rutinaRepository.findByIdAndUsuarioId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rutinaService.sustituir("ana@smartbody.com", 99L, new SustitucionRequest(1L, null)))
                .isInstanceOf(ApiException.class);
        verify(generador, never()).sustituir(any(), any(), any());
    }

    @Test
    void noGeneraRutinaSinPerfilCompleto() {
        when(perfilService.exigirPerfilCompleto(usuario))
                .thenThrow(ApiException.solicitudInvalida("Completa tu perfil antes de generar tu plan."));

        assertThatThrownBy(() -> rutinaService.generar("ana@smartbody.com"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Completa tu perfil");
        verify(rutinaRepository, never()).save(any());
    }
}
