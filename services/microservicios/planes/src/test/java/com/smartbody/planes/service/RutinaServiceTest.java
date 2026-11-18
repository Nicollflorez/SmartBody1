package com.smartbody.planes.service;

import com.smartbody.comun.error.ApiException;
import com.smartbody.planes.cliente.PerfilRemoto;
import com.smartbody.planes.dto.SustitucionRequest;
import com.smartbody.planes.entity.NivelExperiencia;
import com.smartbody.planes.entity.Sexo;
import com.smartbody.planes.repository.EjercicioRepository;
import com.smartbody.planes.repository.ObjetivoRepository;
import com.smartbody.planes.repository.RutinaRepository;
import com.smartbody.planes.service.reglas.GeneradorRutina;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Aislamiento de datos entre usuarios y validaciones previas a la generacion (RNF06, RF06). */
class RutinaServiceTest {

    private RutinaRepository rutinaRepository;
    private GeneradorRutina generador;
    private RutinaService rutinaService;

    @BeforeEach
    void preparar() {
        rutinaRepository = mock(RutinaRepository.class);
        generador = mock(GeneradorRutina.class);
        rutinaService = new RutinaService(mock(ObjetivoRepository.class), mock(EjercicioRepository.class),
                rutinaRepository, generador);
    }

    private static PerfilRemoto perfilCompleto() {
        return new PerfilRemoto(30, Sexo.FEMENINO, 60.0, 165.0, NivelExperiencia.INTERMEDIO, 30, 5,
                new PerfilRemoto.ObjetivoRemoto(2L, "GANAR_MASA", "Ganar masa"), List.of(), true);
    }

    private static PerfilRemoto perfilIncompleto() {
        return new PerfilRemoto(null, null, null, null, null, null, null, null, List.of(), false);
    }

    @Test
    void unaRutinaDeOtroUsuarioSeRespondeComoInexistente() {
        when(rutinaRepository.findByIdAndUsuarioId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rutinaService.detalle(10L, 99L))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getEstado())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void noSePuedeSustituirEnUnaRutinaAjena() {
        when(rutinaRepository.findByIdAndUsuarioId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rutinaService.sustituir(10L, perfilCompleto(), 99L, new SustitucionRequest(1L, null)))
                .isInstanceOf(ApiException.class);
        verify(generador, never()).sustituir(any(), any(), any());
    }

    @Test
    void noGeneraRutinaSinPerfilCompleto() {
        assertThatThrownBy(() -> rutinaService.generar(10L, perfilIncompleto()))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Completa tu perfil");
        verify(rutinaRepository, never()).save(any());
    }

    @Test
    void sinRutinaActivaRespondeNoEncontrado() {
        when(rutinaRepository.findFirstByUsuarioIdAndActivaTrueOrderByGeneradaEnDesc(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rutinaService.activa(10L))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getEstado())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
