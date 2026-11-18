package com.smartbody.progreso.service.reglas;

import com.smartbody.progreso.dto.ObjetivoResponse;
import com.smartbody.progreso.dto.PerfilResponse;
import com.smartbody.progreso.dto.PlanNutricionalResponse;
import com.smartbody.progreso.dto.ProgresoResponse;
import com.smartbody.progreso.dto.RestriccionResponse;
import com.smartbody.progreso.dto.RutinaResponse;
import com.smartbody.progreso.entity.MomentoComida;
import com.smartbody.progreso.entity.NivelExperiencia;
import com.smartbody.progreso.entity.Sexo;
import com.smartbody.progreso.entity.TipoEjercicio;
import com.smartbody.progreso.entity.TipoRestriccion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/** Pruebas del asistente del chatbot: deteccion de intenciones y respuestas con datos del usuario. */
class AsistentePorReglasTest {

    private final AsistentePorReglas asistente = new AsistentePorReglas(new Random(3));

    private static PerfilResponse perfil(boolean completo) {
        return new PerfilResponse(25, Sexo.MASCULINO, 75.5, 175.0, NivelExperiencia.PRINCIPIANTE, 45, 3,
                new ObjetivoResponse(1L, "PERDER_PESO", "Perder peso", null),
                List.of(new RestriccionResponse(1L, "LESION_RODILLA", TipoRestriccion.SALUD, "Lesión de rodilla", null)),
                24.65, "Peso saludable", completo);
    }

    private static ContextoChat contextoCompleto() {
        RutinaResponse rutina = new RutinaResponse(1L, "Rutina para perder peso", null, 3, 45, true, null, List.of(
                new RutinaResponse.Dia(1, "Cuerpo completo", 41, List.of(
                        new RutinaResponse.Item(1L, 5L, "Puente de glúteos", "PIERNAS", TipoEjercicio.FUERZA, 3, 15, null, 45),
                        new RutinaResponse.Item(2L, 29L, "Elíptica", "CARDIO", TipoEjercicio.CARDIO, 1, null, 960, 0)))));

        Receta receta = new Receta("Pechuga de pollo con arroz integral",
                List.of("200 g de pechuga de pollo", "150 g de arroz integral"),
                List.of("Cocina la pechuga.", "Cocina el arroz."), "Un vaso de agua.");
        PlanNutricionalResponse plan = new PlanNutricionalResponse(1L, null, 2280, 2068, 150.8, 221.5, 70.6, null, null, List.of(
                new PlanNutricionalResponse.Comida(MomentoComida.ALMUERZO, "Almuerzo", 689, List.of(
                        new PlanNutricionalResponse.Item(4L, 1L, "Pechuga de pollo", "PROTEINA", 200, "g", 330, 62, 0, 7,
                                null, "🍗", "Atún en agua")), receta)));

        Hidratacion hidratacion = new Hidratacion(3.0, 2.6, 12, List.of("Bebe un vaso de agua al despertar."),
                List.of("Gaseosas y jugos con azúcar añadida."));
        ProgresoResponse progreso = new ProgresoResponse(List.of(), 78.0, 74.8, -3.2, 24.42, "Peso saludable");

        return new ContextoChat("Usuario Demo", perfil(true), rutina, plan, hidratacion, progreso);
    }

    @ParameterizedTest
    @CsvSource({
            "¿Cuánta agua debo tomar?, AGUA",
            "Hola ¿qué tal?, SALUDO",
            "¿Cuánto debo descansar después de entrenar?, DESCANSO",
            "¿Qué ejercicios tengo en el día 1?, RUTINA",
            "¿Cómo cambio un alimento?, CAMBIAR",
            "Receta del almuerzo, RECETA",
            "¿Cuánta proteína tiene mi plan?, MACROS",
            "¿Cuántas calorías debo comer?, NUTRICION",
            "¿Cómo va mi progreso?, PROGRESO",
            "Me duele la rodilla al entrenar, DOLOR",
            "No tengo ganas de entrenar hoy, MOTIVACION"
    })
    void detectaLaIntencionAunqueLaPreguntaTengaTildes(String pregunta, AsistentePorReglas.Intencion esperada) {
        assertThat(AsistentePorReglas.detectarIntencion(AsistentePorReglas.normalizar(pregunta))).isEqualTo(esperada);
    }

    @Test
    void respondeLaHidratacionConLosDatosDelUsuario() {
        RespuestaChat respuesta = asistente.responder("¿Cuánta agua debo tomar?", contextoCompleto());

        assertThat(respuesta.respuesta()).contains("3 litros", "12 vasos", "2,6 litros");
        assertThat(respuesta.sugerencias()).isNotEmpty();
    }

    @Test
    void muestraLosEjerciciosDelDiaSolicitado() {
        RespuestaChat respuesta = asistente.responder("ejercicios del día 1", contextoCompleto());

        assertThat(respuesta.respuesta()).contains("Cuerpo completo", "Puente de glúteos — 3 x 15 reps", "Elíptica — 16 min");
    }

    @Test
    void daLaRecetaDeLaComidaPedida() {
        RespuestaChat respuesta = asistente.responder("¿Cómo preparo el almuerzo?", contextoCompleto());

        assertThat(respuesta.respuesta()).contains("Pechuga de pollo con arroz integral", "1. Cocina la pechuga.", "Un vaso de agua.");
    }

    @Test
    void anteDolorRecomiendaDetenerseYConsultarUnProfesional() {
        RespuestaChat respuesta = asistente.responder("Siento dolor en la rodilla", contextoCompleto());

        assertThat(respuesta.respuesta()).contains("detén el ejercicio", "profesional de la salud", "Lesión de rodilla");
    }

    @Test
    void sinPerfilCompletoPideCompletarlo() {
        ContextoChat sinPerfil = new ContextoChat("Ana", perfil(false), null, null, null, null);

        RespuestaChat respuesta = asistente.responder("¿Cuánta agua debo tomar?", sinPerfil);

        assertThat(respuesta.respuesta()).contains("Completa tu perfil");
    }

    @Test
    void cuandoNoEntiendeOfreceSugerencias() {
        RespuestaChat respuesta = asistente.responder("xyz qwerty", contextoCompleto());

        assertThat(respuesta.respuesta()).contains("Usuario", "No logré entenderte");
        assertThat(respuesta.sugerencias()).isEqualTo(AsistentePorReglas.SUGERENCIAS_INICIALES);
    }

    @ParameterizedTest
    @CsvSource({
            "hola",
            "¿Cuánta agua debo tomar?",
            "ejercicios del día 1",
            "receta del almuerzo",
            "¿Cómo va mi progreso?",
            "no tengo ganas de entrenar",
            "gracias"
    })
    void llamaAlUsuarioPorSuNombreYLoMotiva(String pregunta) {
        RespuestaChat respuesta = asistente.responder(pregunta, contextoCompleto());

        assertThat(respuesta.respuesta()).contains("Usuario");
        assertThat(respuesta.respuesta()).containsAnyOf("💪", "🚀", "🌟", "🔥", "💚", "🏆");
    }

    @Test
    void anteDolorEsAmableSinInvitarAEntrenar() {
        RespuestaChat respuesta = asistente.responder("me duele la espalda", contextoCompleto());

        assertThat(respuesta.respuesta()).contains("Usuario, lo primero siempre es tu bienestar");
        AsistentePorReglas.CIERRES_MOTIVADORES.forEach(cierre ->
                assertThat(respuesta.respuesta()).doesNotContain(cierre.replace("{nombre}", "Usuario")));
    }

    @Test
    void usaElPrimerNombreConMayusculaInicial() {
        ContextoChat contexto = new ContextoChat("nicoll yuherly florez", perfil(true), null, null, null, null);

        assertThat(AsistentePorReglas.nombre(contexto)).isEqualTo("Nicoll");
    }
}
