package com.smartbody.planes.service.reglas;

import com.smartbody.planes.entity.Alimento;
import com.smartbody.planes.entity.MomentoComida;
import com.smartbody.planes.entity.Sexo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static com.smartbody.planes.service.reglas.CatalogoDePrueba.INTOLERANCIA_LACTOSA;
import static com.smartbody.planes.service.reglas.CatalogoDePrueba.VEGANO;
import static org.assertj.core.api.Assertions.assertThat;

/** Pruebas de las reglas del plan nutricional. */
class GeneradorPlanNutricionalPorReglasTest {

    private GeneradorPlanNutricionalPorReglas generador;
    private List<Alimento> catalogo;

    @BeforeEach
    void preparar() {
        generador = new GeneradorPlanNutricionalPorReglas(new Random(7));
        catalogo = CatalogoDePrueba.alimentos();
    }

    private static SolicitudNutricion solicitud(String objetivo, Set<Long> restricciones) {
        return new SolicitudNutricion(objetivo, Sexo.MASCULINO, 25, 75.5, 175, 3, 45, restricciones, Set.of());
    }

    @Test
    void lasCaloriasDependenDelObjetivo() {
        int perder = generador.caloriasObjetivo(solicitud("PERDER_PESO", Set.of()));
        int mantener = generador.caloriasObjetivo(solicitud("MANTENER_PESO", Set.of()));
        int ganar = generador.caloriasObjetivo(solicitud("GANAR_MASA", Set.of()));

        assertThat(perder).isLessThan(mantener);
        assertThat(mantener).isLessThan(ganar);
        // Mifflin-St Jeor: (10*75.5 + 6.25*175 - 5*25 + 5) * 1.55 = 2680 kcal aprox.
        assertThat(mantener).isBetween(2650, 2710);
    }

    @Test
    void nuncaBajaDelMinimoDeCaloriasSeguro() {
        SolicitudNutricion pequena = new SolicitudNutricion("PERDER_PESO", Sexo.FEMENINO, 80, 38, 140, 1, 30, Set.of(), Set.of());

        assertThat(generador.caloriasObjetivo(pequena)).isGreaterThanOrEqualTo(GeneradorPlanNutricionalPorReglas.CALORIAS_MINIMAS);
    }

    @Test
    void incluyeLasCuatroComidasDelDia() {
        List<AlimentoPlanificado> plan = generador.generar(solicitud("MANTENER_PESO", Set.of()), catalogo);

        assertThat(plan.stream().map(AlimentoPlanificado::momento).collect(Collectors.toSet()))
                .containsExactlyInAnyOrder(MomentoComida.values());
        assertThat(plan).allMatch(a -> a.gramos() > 0);
    }

    @Test
    void excluyeLosAlimentosIncompatiblesConLasRestricciones() {
        Set<Long> restricciones = Set.of(INTOLERANCIA_LACTOSA, VEGANO);

        List<AlimentoPlanificado> plan = generador.generar(solicitud("GANAR_MASA", restricciones), catalogo);

        assertThat(plan).isNotEmpty();
        assertThat(plan).noneMatch(a -> a.alimento().esIncompatibleCon(restricciones));
        assertThat(plan).noneMatch(a -> a.alimento().getGrupo().equals("LACTEO"));
    }

    @Test
    void laSustitucionMantieneLasCaloriasAproximadas() {
        Alimento arroz = catalogo.get(6);
        Alimento papa = catalogo.get(8);

        double gramosPapa = generador.gramosEquivalentes(arroz, 150, papa);

        double caloriasArroz = 150 * arroz.getCaloriasPor100g() / 100;
        double caloriasPapa = gramosPapa * papa.getCaloriasPor100g() / 100;
        assertThat(caloriasPapa).isBetween(caloriasArroz * 0.9, caloriasArroz * 1.1);
    }

    @Test
    void cadaRecetaUsaLosAlimentosYCantidadesDeLaComida() {
        Alimento pollo = catalogo.get(0);
        Alimento arroz = catalogo.get(6);
        Alimento brocoli = catalogo.get(11);
        Alimento aceite = catalogo.get(14);
        List<AlimentoPlanificado> almuerzo = List.of(
                new AlimentoPlanificado(pollo, MomentoComida.ALMUERZO, 180),
                new AlimentoPlanificado(arroz, MomentoComida.ALMUERZO, 150),
                new AlimentoPlanificado(brocoli, MomentoComida.ALMUERZO, 100),
                new AlimentoPlanificado(aceite, MomentoComida.ALMUERZO, 10));

        Receta receta = generador.receta(MomentoComida.ALMUERZO, almuerzo);

        // El aceite es un alino: esta en los ingredientes pero no en el nombre del plato.
        assertThat(receta.titulo()).isEqualTo("Pechuga de pollo con arroz integral y brócoli");
        assertThat(receta.ingredientes()).containsExactly(
                "180 g de pechuga de pollo", "150 g de arroz integral", "100 g de brócoli", "10 g de aceite de oliva");
        assertThat(receta.pasos()).hasSize(5).first().isEqualTo("Prepara pechuga de pollo.");
        assertThat(receta.bebida()).isNotBlank();
    }

    @Test
    void laHidratacionDependeDelPesoYDelTiempoDeEntrenamiento() {
        Hidratacion hidratacion = generador.hidratacion(solicitud("MANTENER_PESO", Set.of()));

        // 75,5 kg x 35 ml = 2,6 L; con 45 minutos de entrenamiento se suman 0,4 L.
        assertThat(hidratacion.litrosDiaDescanso()).isEqualTo(2.6);
        assertThat(hidratacion.litrosDiaEntrenamiento()).isEqualTo(3.0);
        assertThat(hidratacion.vasosDiaEntrenamiento()).isEqualTo(12);
        assertThat(hidratacion.recomendaciones()).isNotEmpty();
        assertThat(hidratacion.evitar()).anyMatch(e -> e.contains("azúcar"));
    }

    @Test
    void sugiereBebidaVegetalCuandoElUsuarioNoConsumeLacteos() {
        SolicitudNutricion vegano = new SolicitudNutricion("GANAR_MASA", Sexo.FEMENINO, 30, 60, 165, 4, 60,
                Set.of(VEGANO), Set.of("VEGANO"));

        Hidratacion hidratacion = generador.hidratacion(vegano);

        assertThat(hidratacion.recomendaciones()).anyMatch(r -> r.contains("bebida de soya"));
        assertThat(hidratacion.recomendaciones()).noneMatch(r -> r.contains("leche"));
    }

    @Test
    void lasAlternativasSonDelMismoGrupoYCompatibles() {
        Alimento pollo = catalogo.get(0);

        List<Alimento> alternativas = generador.alternativas(pollo, MomentoComida.ALMUERZO, Set.of(), solicitud("PERDER_PESO", Set.of(VEGANO)), catalogo);

        assertThat(alternativas).extracting(Alimento::getNombre).containsExactly("Lentejas", "Tofu");
    }

    @Test
    void laAvenaSoloSeRecomiendaEnElDesayunoOElSnack() {
        for (int semilla = 0; semilla < 40; semilla++) {
            List<AlimentoPlanificado> plan = new GeneradorPlanNutricionalPorReglas(new Random(semilla))
                    .generar(solicitud("GANAR_MASA", Set.of()), catalogo);

            assertThat(plan)
                    .filteredOn(a -> a.alimento().getNombre().equals("Avena"))
                    .allMatch(a -> a.momento() == MomentoComida.DESAYUNO || a.momento() == MomentoComida.SNACK);
        }
    }

    @Test
    void lasAlternativasRespetanElMomentoDelDia() {
        Alimento papa = catalogo.get(8);

        List<Alimento> enElAlmuerzo = generador.alternativas(papa, MomentoComida.ALMUERZO, Set.of(), solicitud("MANTENER_PESO", Set.of()), catalogo);
        List<Alimento> enElDesayuno = generador.alternativas(papa, MomentoComida.DESAYUNO, Set.of(), solicitud("MANTENER_PESO", Set.of()), catalogo);

        assertThat(enElAlmuerzo).extracting(Alimento::getNombre).containsExactly("Arroz integral");
        assertThat(enElDesayuno).extracting(Alimento::getNombre).containsExactly("Avena");
    }

    @Test
    void siNingunAlimentoEsAdecuadoParaLaComidaUsaLosDisponibles() {
        List<Alimento> soloDesayuno = List.of(catalogo.get(7));

        assertThat(GeneradorPlanNutricionalPorReglas.preferirMomento(soloDesayuno, MomentoComida.CENA)).containsExactly(catalogo.get(7));
    }
}
