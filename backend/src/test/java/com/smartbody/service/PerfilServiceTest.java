package com.smartbody.service;

import com.smartbody.dto.PerfilRequest;
import com.smartbody.dto.PerfilResponse;
import com.smartbody.entity.Medida;
import com.smartbody.entity.NivelExperiencia;
import com.smartbody.entity.Objetivo;
import com.smartbody.entity.Sexo;
import com.smartbody.entity.Usuario;
import com.smartbody.exception.ApiException;
import com.smartbody.repository.MedidaRepository;
import com.smartbody.repository.ObjetivoRepository;
import com.smartbody.repository.PlanNutricionalRepository;
import com.smartbody.repository.RestriccionRepository;
import com.smartbody.repository.RutinaRepository;
import com.smartbody.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pruebas del registro de perfil (RF03, RF04, RF05). */
class PerfilServiceTest {

    private ObjetivoRepository objetivoRepository;
    private RestriccionRepository restriccionRepository;
    private MedidaRepository medidaRepository;
    private PerfilService perfilService;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        UsuarioService usuarioService = mock(UsuarioService.class);
        objetivoRepository = mock(ObjetivoRepository.class);
        restriccionRepository = mock(RestriccionRepository.class);
        medidaRepository = mock(MedidaRepository.class);
        perfilService = new PerfilService(usuarioService, mock(UsuarioRepository.class), objetivoRepository,
                restriccionRepository, medidaRepository, mock(RutinaRepository.class), mock(PlanNutricionalRepository.class));

        usuario = new Usuario();
        usuario.setEmail("ana@smartbody.com");
        when(usuarioService.buscarPorEmail("ana@smartbody.com")).thenReturn(usuario);
    }

    private static PerfilRequest peticion(Long objetivoId) {
        return new PerfilRequest(30, Sexo.FEMENINO, 64.0, 160.0, NivelExperiencia.PRINCIPIANTE, 45, 3, objetivoId, List.of());
    }

    @Test
    void guardaElPerfilCalculaElImcYRegistraLaMedidaInicial() {
        Objetivo objetivo = new Objetivo();
        ReflectionTestUtils.setField(objetivo, "id", 1L);
        when(objetivoRepository.findById(1L)).thenReturn(Optional.of(objetivo));
        when(restriccionRepository.findAllById(any())).thenReturn(List.of());

        PerfilResponse respuesta = perfilService.actualizar("ana@smartbody.com", peticion(1L));

        assertThat(respuesta.completo()).isTrue();
        assertThat(respuesta.imc()).isEqualTo(25.0);
        assertThat(respuesta.clasificacionImc()).isEqualTo("Sobrepeso");

        ArgumentCaptor<Medida> medida = ArgumentCaptor.forClass(Medida.class);
        verify(medidaRepository).save(medida.capture());
        assertThat(medida.getValue().getPesoKg()).isEqualTo(64.0);
    }

    @Test
    void rechazaUnObjetivoInexistente() {
        when(objetivoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perfilService.actualizar("ana@smartbody.com", peticion(999L)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("objetivo");
        verify(medidaRepository, never()).save(any());
    }
}
