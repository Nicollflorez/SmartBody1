package com.smartbody.progreso.service;

import com.smartbody.progreso.cliente.ClienteUsuarioYPlanes;
import com.smartbody.progreso.dto.MedidaRequest;
import com.smartbody.progreso.dto.MedidaResponse;
import com.smartbody.progreso.dto.PerfilResponse;
import com.smartbody.progreso.dto.ProgresoResponse;
import com.smartbody.progreso.entity.Medida;
import com.smartbody.progreso.repository.MedidaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** RF10: la medida calcula el IMC con la altura del perfil y propaga el peso nuevo a auth. */
class ProgresoServiceTest {

    private MedidaRepository medidaRepository;
    private ClienteUsuarioYPlanes cliente;
    private ProgresoService progresoService;

    @BeforeEach
    void preparar() {
        medidaRepository = mock(MedidaRepository.class);
        cliente = mock(ClienteUsuarioYPlanes.class);
        progresoService = new ProgresoService(medidaRepository, cliente);
        when(medidaRepository.save(any(Medida.class))).thenAnswer(llamada -> llamada.getArgument(0));
    }

    @Test
    void registraLaMedidaConLaAlturaDelPerfilYActualizaElPesoEnAuth() {
        PerfilResponse perfil = new PerfilResponse(30, null, 70.0, 175.0, null, 45, 3, null, List.of(), null, null, true);

        MedidaResponse medida = progresoService.registrar(4L,
                new MedidaRequest(74.8, null, 87.5, null, null, null), perfil);

        assertThat(medida.alturaCm()).isEqualTo(175.0);
        assertThat(medida.imc()).isEqualTo(24.42);
        ArgumentCaptor<Medida> guardada = ArgumentCaptor.forClass(Medida.class);
        verify(medidaRepository).save(guardada.capture());
        assertThat(guardada.getValue().getUsuarioId()).isEqualTo(4L);
        verify(cliente).actualizarPeso(4L, 74.8);
    }

    @Test
    void laMedidaQueVieneDelPerfilNoVuelveAAvisarAAuth() {
        progresoService.registrarDesdePerfil(4L, 62.0, 165.0);

        verify(medidaRepository).save(any(Medida.class));
        verify(cliente, never()).actualizarPeso(any(), any());
    }

    @Test
    void elResumenCalculaElCambioDePesoEntreLaPrimeraYLaUltimaMedida() {
        Medida primera = new Medida();
        primera.setUsuarioId(4L);
        primera.setPesoKg(78.0);
        primera.setImc(25.47);
        Medida ultima = new Medida();
        ultima.setUsuarioId(4L);
        ultima.setPesoKg(74.8);
        ultima.setImc(24.42);
        when(medidaRepository.findByUsuarioIdOrderByRegistradoEnAsc(4L)).thenReturn(List.of(primera, ultima));

        ProgresoResponse progreso = progresoService.consultar(4L);

        assertThat(progreso.cambioPesoKg()).isEqualTo(-3.2);
        assertThat(progreso.clasificacionImc()).isEqualTo("Peso saludable");
    }

    @Test
    void sinMedidasElResumenVieneVacio() {
        when(medidaRepository.findByUsuarioIdOrderByRegistradoEnAsc(9L)).thenReturn(List.of());

        assertThat(progresoService.consultar(9L).medidas()).isEmpty();
        assertThat(progresoService.consultar(9L).pesoActual()).isNull();
    }
}
