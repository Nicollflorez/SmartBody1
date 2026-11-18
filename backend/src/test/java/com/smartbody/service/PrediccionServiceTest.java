package com.smartbody.service;

import com.smartbody.dto.PrediccionRequest;
import com.smartbody.dto.PrediccionResponse;
import com.smartbody.dto.SeguimientoPrediccionResponse;
import com.smartbody.entity.EstadoSeguimiento;
import com.smartbody.entity.Medida;
import com.smartbody.entity.Prediccion;
import com.smartbody.entity.Usuario;
import com.smartbody.exception.ApiException;
import com.smartbody.repository.MedidaRepository;
import com.smartbody.repository.PrediccionRepository;
import com.smartbody.service.prediccion.ModeloPrediccion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Predicciones del monolito: validacion, persistencia, aislamiento por usuario y seguimiento. */
class PrediccionServiceTest {

    private static final long USUARIO = 4L;
    private static final String EMAIL = "usuario@prueba.smartbody.com";

    private ModeloPrediccion modelo;
    private PrediccionRepository predicciones;
    private MedidaRepository medidas;
    private UsuarioService usuarios;
    private PrediccionService servicio;

    @BeforeEach
    void preparar() {
        modelo = mock(ModeloPrediccion.class);
        predicciones = mock(PrediccionRepository.class);
        medidas = mock(MedidaRepository.class);
        usuarios = mock(UsuarioService.class);
        Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(USUARIO);
        when(usuarios.buscarPorEmail(EMAIL)).thenReturn(usuario);
        servicio = new PrediccionService(modelo, predicciones, medidas, usuarios);
        when(predicciones.save(any(Prediccion.class))).thenAnswer(llamada -> llamada.getArgument(0));
    }

    @Test
    void predicePersisteYDevuelveElResultadoOrientativo() {
        when(modelo.disponible()).thenReturn(true);
        when(modelo.predecir(anyMap())).thenReturn(12.345);

        PrediccionResponse respuesta = servicio.predecir(EMAIL, perderTipica());

        assertThat(respuesta.semanasEstimadas()).isEqualTo(12.3);
        assertThat(respuesta.modelo()).isEqualTo("REPTree");
        assertThat(respuesta.tipoPrediccion()).isEqualTo("orientativa");
        assertThat(respuesta.aviso()).contains("No garantiza");
        assertThat(respuesta.fechaAproximada()).isEqualTo(respuesta.fechaPrediccion().toLocalDate().plusDays(86));
    }

    @Test
    void sinModeloCargadoResponde503SinGuardarNada() {
        when(modelo.disponible()).thenReturn(false);

        assertThatThrownBy(() -> servicio.predecir(EMAIL, perderTipica()))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getEstado()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void unaPrediccionAjenaSeTrataComoInexistente() {
        when(predicciones.findByIdAndUsuarioId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.seguimiento(EMAIL, 99L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getEstado()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void sinMedidasDespuesDePredecirElSeguimientoQuedaPendiente() {
        Prediccion p = prediccion(LocalDateTime.now().minusDays(10));
        when(predicciones.findByIdAndUsuarioId(1L, USUARIO)).thenReturn(Optional.of(p));
        when(medidas.findByUsuarioIdOrderByRegistradoEnAsc(USUARIO)).thenReturn(List.of());

        SeguimientoPrediccionResponse seguimiento = servicio.seguimiento(EMAIL, 1L);

        assertThat(seguimiento.estado()).isEqualTo(EstadoSeguimiento.PENDIENTE);
        assertThat(seguimiento.pesoActual()).isNull();
        assertThat(seguimiento.puntos()).isEmpty();
    }

    @Test
    void elSeguimientoDetectaCuandoSeAlcanzaElObjetivoYCompara() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(30);
        Prediccion p = prediccion(inicio); // estima 20 semanas
        when(predicciones.findByIdAndUsuarioId(1L, USUARIO)).thenReturn(Optional.of(p));
        when(medidas.findByUsuarioIdOrderByRegistradoEnAsc(USUARIO)).thenReturn(List.of(
                medida(inicio.minusDays(5), 91.0),
                medida(inicio.plusDays(10), 88.0),
                medida(inicio.plusDays(20), 85.0),
                medida(inicio.plusDays(29), 79.5)));

        SeguimientoPrediccionResponse seguimiento = servicio.seguimiento(EMAIL, 1L);

        assertThat(seguimiento.estado()).isEqualTo(EstadoSeguimiento.OBJETIVO_ALCANZADO);
        assertThat(seguimiento.pesoActual()).isEqualTo(79.5);
        assertThat(seguimiento.avancePorcentaje()).isEqualTo(100.0);
        assertThat(seguimiento.semanasRealesHastaObjetivo()).isEqualTo(4.1);
        assertThat(seguimiento.diferenciaSemanas()).isEqualTo(-15.9);
        assertThat(seguimiento.puntos()).hasSize(3);
        assertThat(p.getEstadoSeguimiento()).isEqualTo(EstadoSeguimiento.OBJETIVO_ALCANZADO);
    }

    @Test
    void elRitmoNoSeCalculaAntesDeUnaSemana() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(2);
        Prediccion p = prediccion(inicio);
        when(predicciones.findByIdAndUsuarioId(1L, USUARIO)).thenReturn(Optional.of(p));
        when(medidas.findByUsuarioIdOrderByRegistradoEnAsc(USUARIO)).thenReturn(List.of(
                medida(inicio.plusHours(1), 85.0)));

        SeguimientoPrediccionResponse seguimiento = servicio.seguimiento(EMAIL, 1L);

        assertThat(seguimiento.ritmoRealKgSemana()).isNull();
        assertThat(seguimiento.avancePorcentaje()).isEqualTo(50.0);
    }

    private static PrediccionRequest perderTipica() {
        return new PrediccionRequest(90.0, 80.0, 170.0, 28, "MASCULINO", "PERDER_PESO", "MODERADO",
                "INTERMEDIO", "MIXTO", 4, 45, 0, 2430.0, 90.0, 85.0);
    }

    private static Prediccion prediccion(LocalDateTime fecha) {
        Prediccion p = new Prediccion();
        p.setUsuarioId(USUARIO);
        p.setObjetivo("PERDER_PESO");
        p.setPesoInicialKg(90.0);
        p.setPesoObjetivoKg(80.0);
        p.setSemanasEstimadas(20.0);
        p.setFechaPrediccion(fecha);
        return p;
    }

    private static Medida medida(LocalDateTime cuando, double peso) {
        Medida m = new Medida();
        m.setPesoKg(peso);
        ReflectionTestUtils.setField(m, "registradoEn", cuando);
        return m;
    }
}
