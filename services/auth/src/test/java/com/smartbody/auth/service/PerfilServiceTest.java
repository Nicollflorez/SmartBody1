package com.smartbody.auth.service;

import com.smartbody.auth.cliente.ClientePlanes;
import com.smartbody.auth.cliente.ClienteProgreso;
import com.smartbody.auth.dto.PerfilRequest;
import com.smartbody.auth.dto.PerfilResponse;
import com.smartbody.auth.dto.ResumenResponse;
import com.smartbody.auth.entity.NivelExperiencia;
import com.smartbody.auth.entity.Objetivo;
import com.smartbody.auth.entity.Sexo;
import com.smartbody.auth.entity.Usuario;
import com.smartbody.auth.repository.ObjetivoRepository;
import com.smartbody.auth.repository.RestriccionRepository;
import com.smartbody.auth.repository.UsuarioRepository;
import com.smartbody.comun.error.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
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

/** Perfil (RF03-RF05): el cambio de peso se propaga al servicio progreso y el resumen consulta a planes. */
class PerfilServiceTest {

    private ObjetivoRepository objetivoRepository;
    private RestriccionRepository restriccionRepository;
    private ClienteProgreso clienteProgreso;
    private ClientePlanes clientePlanes;
    private PerfilService perfilService;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        UsuarioService usuarioService = mock(UsuarioService.class);
        objetivoRepository = mock(ObjetivoRepository.class);
        restriccionRepository = mock(RestriccionRepository.class);
        clienteProgreso = mock(ClienteProgreso.class);
        clientePlanes = mock(ClientePlanes.class);
        perfilService = new PerfilService(usuarioService, mock(UsuarioRepository.class), objetivoRepository,
                restriccionRepository, clienteProgreso, clientePlanes);

        usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", 5L);
        usuario.setNombre("Ana");
        usuario.setEmail("ana@smartbody.com");
        when(usuarioService.buscarPorEmail("ana@smartbody.com")).thenReturn(usuario);
    }

    private static PerfilRequest peticion(Long objetivoId) {
        return new PerfilRequest(30, Sexo.FEMENINO, 64.0, 160.0, NivelExperiencia.PRINCIPIANTE, 45, 3, objetivoId, List.of());
    }

    @Test
    void guardaElPerfilCalculaElImcYRegistraLaMedidaEnProgreso() {
        Objetivo objetivo = new Objetivo();
        ReflectionTestUtils.setField(objetivo, "id", 1L);
        when(objetivoRepository.findById(1L)).thenReturn(Optional.of(objetivo));
        when(restriccionRepository.findAllById(any())).thenReturn(List.of());

        PerfilResponse respuesta = perfilService.actualizar("ana@smartbody.com", peticion(1L));

        assertThat(respuesta.completo()).isTrue();
        assertThat(respuesta.imc()).isEqualTo(25.0);
        assertThat(respuesta.clasificacionImc()).isEqualTo("Sobrepeso");
        verify(clienteProgreso).registrarMedida(5L, 64.0, 160.0);
    }

    @Test
    void rechazaUnObjetivoInexistente() {
        when(objetivoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perfilService.actualizar("ana@smartbody.com", peticion(999L)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("objetivo");
        verify(clienteProgreso, never()).registrarMedida(any(), any(), any());
    }

    @Test
    void elResumenSugiereRenovarLaRutinaDespuesDe28Dias() {
        when(clientePlanes.rutinaActiva()).thenReturn(Optional.of(
                new ClientePlanes.RutinaActiva(9L, LocalDateTime.now().minusDays(30))));
        when(clientePlanes.planActivo()).thenReturn(Optional.empty());

        ResumenResponse resumen = perfilService.resumen("ana@smartbody.com");

        assertThat(resumen.rutinaActivaId()).isEqualTo(9L);
        assertThat(resumen.planActivoId()).isNull();
        assertThat(resumen.sugerirCambioRutina()).isTrue();
        assertThat(resumen.perfilCompleto()).isFalse();
    }
}
