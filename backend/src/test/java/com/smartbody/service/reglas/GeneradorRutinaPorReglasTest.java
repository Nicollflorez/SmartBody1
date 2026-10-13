package com.smartbody.service.reglas;

import com.smartbody.entity.Ejercicio;
import com.smartbody.entity.NivelExperiencia;
import com.smartbody.entity.TipoEjercicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static com.smartbody.service.reglas.CatalogoDePrueba.LESION_RODILLA;
import static org.assertj.core.api.Assertions.assertThat;

/** Pruebas del motor de reglas de rutinas (RF06, RF09). */
class GeneradorRutinaPorReglasTest {

    private GeneradorRutinaPorReglas generador;
    private List<Ejercicio> catalogo;

    @BeforeEach
    void preparar() {
        generador = new GeneradorRutinaPorReglas(new Random(42));
        catalogo = CatalogoDePrueba.ejercicios();
    }

    private static SolicitudRutina solicitud(String objetivo, NivelExperiencia nivel, int minutos, int dias, Set<Long> restricciones) {
        return new SolicitudRutina(objetivo, nivel, minutos, dias, restricciones);
    }

    @Test
    void generaUnaSesionPorCadaDiaDisponible() {
        List<EjercicioPlanificado> plan = generador.generar(
                solicitud("MEJORAR_CONDICION", NivelExperiencia.PRINCIPIANTE, 45, 3, Set.of()), catalogo);

        assertThat(plan).isNotEmpty();
        assertThat(plan.stream().map(EjercicioPlanificado::dia).collect(Collectors.toSet())).containsExactlyInAnyOrder(1, 2, 3);
    }

    @ParameterizedTest
    @CsvSource({
            "PERDER_PESO, PRINCIPIANTE, 15",
            "PERDER_PESO, PRINCIPIANTE, 30",
            "GANAR_MASA, INTERMEDIO, 30",
            "GANAR_MASA, INTERMEDIO, 60",
            "MEJORAR_RESISTENCIA, INTERMEDIO, 45",
            "MANTENER_PESO, PRINCIPIANTE, 90"
    })
    void cadaSesionSeAjustaAlTiempoDisponible(String objetivo, NivelExperiencia nivel, int minutos) {
        List<EjercicioPlanificado> plan = generador.generar(solicitud(objetivo, nivel, minutos, 3, Set.of()), catalogo);

        Map<Integer, Integer> segundosPorDia = plan.stream()
                .collect(Collectors.groupingBy(EjercicioPlanificado::dia, Collectors.summingInt(EjercicioPlanificado::segundosEstimados)));

        assertThat(segundosPorDia.values()).allSatisfy(segundos -> {
            assertThat(segundos).isLessThanOrEqualTo(minutos * 60);
            assertThat(segundos).isGreaterThanOrEqualTo(minutos * 60 / 2);
        });
    }

    @Test
    void nuncaIncluyeEjerciciosContraindicadosParaLasRestricciones() {
        List<EjercicioPlanificado> plan = generador.generar(
                solicitud("PERDER_PESO", NivelExperiencia.INTERMEDIO, 60, 5, Set.of(LESION_RODILLA)), catalogo);

        assertThat(plan).isNotEmpty();
        assertThat(plan).noneMatch(p -> p.ejercicio().estaContraindicadoPara(Set.of(LESION_RODILLA)));
    }

    @Test
    void noIncluyeEjerciciosPorEncimaDelNivelDelUsuario() {
        List<EjercicioPlanificado> plan = generador.generar(
                solicitud("GANAR_MASA", NivelExperiencia.PRINCIPIANTE, 60, 4, Set.of()), catalogo);

        assertThat(plan).allMatch(p -> p.ejercicio().getNivelMinimo() == NivelExperiencia.PRINCIPIANTE);
    }

    @Test
    void perderPesoIncluyeCardioYGanarMasaNo() {
        List<EjercicioPlanificado> perderPeso = generador.generar(
                solicitud("PERDER_PESO", NivelExperiencia.PRINCIPIANTE, 45, 3, Set.of()), catalogo);
        List<EjercicioPlanificado> ganarMasa = generador.generar(
                solicitud("GANAR_MASA", NivelExperiencia.PRINCIPIANTE, 45, 3, Set.of()), catalogo);

        assertThat(perderPeso).anyMatch(p -> p.ejercicio().getTipo() == TipoEjercicio.CARDIO);
        assertThat(ganarMasa).noneMatch(p -> p.ejercicio().getTipo() == TipoEjercicio.CARDIO);
    }

    @Test
    void ganarMasaUsaMenosRepeticionesYMasDescansoQuePerderPeso() {
        EjercicioPlanificado fuerzaMasa = primeraDeFuerzaConRepeticiones(generador.generar(
                solicitud("GANAR_MASA", NivelExperiencia.INTERMEDIO, 45, 3, Set.of()), catalogo));
        EjercicioPlanificado fuerzaPeso = primeraDeFuerzaConRepeticiones(generador.generar(
                solicitud("PERDER_PESO", NivelExperiencia.INTERMEDIO, 45, 3, Set.of()), catalogo));

        assertThat(fuerzaMasa.repeticiones()).isLessThan(fuerzaPeso.repeticiones());
        assertThat(fuerzaMasa.descansoSeg()).isGreaterThan(fuerzaPeso.descansoSeg());
    }

    @Test
    void lasAlternativasSonDelMismoGrupoYRespetanLasRestricciones() {
        Ejercicio prensa = catalogo.get(1);
        SolicitudRutina conLesion = solicitud("GANAR_MASA", NivelExperiencia.INTERMEDIO, 45, 3, Set.of(LESION_RODILLA));

        List<Ejercicio> alternativas = generador.alternativas(prensa, Set.of(), conLesion, catalogo);

        assertThat(alternativas).isNotEmpty();
        assertThat(alternativas).allMatch(e -> e.getGrupoMuscular().equals("PIERNAS"));
        assertThat(alternativas).noneMatch(e -> e.estaContraindicadoPara(Set.of(LESION_RODILLA)));
        assertThat(alternativas).doesNotContain(prensa);
    }

    @Test
    void laSustitucionConservaElDiaYElOrden() {
        SolicitudRutina solicitud = solicitud("MANTENER_PESO", NivelExperiencia.PRINCIPIANTE, 45, 3, Set.of());
        EjercicioPlanificado actual = generador.generar(solicitud, catalogo).get(0);
        Ejercicio nuevo = generador.alternativas(actual.ejercicio(), Set.of(), solicitud, catalogo).get(0);

        EjercicioPlanificado reemplazo = generador.sustituir(actual, nuevo, solicitud);

        assertThat(reemplazo.ejercicio()).isEqualTo(nuevo);
        assertThat(reemplazo.dia()).isEqualTo(actual.dia());
        assertThat(reemplazo.orden()).isEqualTo(actual.orden());
    }

    private static EjercicioPlanificado primeraDeFuerzaConRepeticiones(List<EjercicioPlanificado> plan) {
        return plan.stream()
                .filter(p -> p.ejercicio().getTipo() == TipoEjercicio.FUERZA && p.repeticiones() != null)
                .findFirst()
                .orElseThrow();
    }
}
