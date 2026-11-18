package com.smartbody.progreso.service.prediccion;

import com.smartbody.comun.error.ApiException;
import com.smartbody.progreso.dto.PrediccionRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Coherencia de la entrada y calculo de variables derivadas (mismas formulas que el generador). */
class ReglasPrediccionTest {

    @Test
    void calculaDiferenciaImcGastoYBalanceComoEnElEntrenamiento() {
        ReglasPrediccion.Derivados d = ReglasPrediccion.validarYDerivar(perderTipica(2430.0));

        assertThat(d.diferencia()).isEqualTo(10.0);
        assertThat(d.imc()).isEqualTo(31.14);
        // TMB = 10*90 + 6,25*170 - 5*28 + 5 = 1827,5 ; x 1,55 (moderado) = 2833
        assertThat(d.consumoKcal()).isEqualTo(2833);
        assertThat(d.balanceKcal()).isEqualTo(403);
    }

    @Test
    void rechazaPerderPesoConPesoObjetivoMayorOIgualAlInicial() {
        PrediccionRequest r = new PrediccionRequest(80.0, 85.0, 170.0, 28, "MASCULINO", "PERDER_PESO",
                "MODERADO", "INTERMEDIO", "MIXTO", 4, 45, 0, 2430.0, 90.0, 85.0);

        assertThatThrownBy(() -> ReglasPrediccion.validarYDerivar(r))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("menor que el peso inicial");
    }

    @Test
    void rechazaGanarMasaConPesoObjetivoMenorAlInicial() {
        PrediccionRequest r = new PrediccionRequest(70.0, 65.0, 175.0, 25, "MASCULINO", "GANAR_MASA",
                "MODERADO", "PRINCIPIANTE", "FUERZA", 3, 60, 0, 3000.0, 90.0, 90.0);

        assertThatThrownBy(() -> ReglasPrediccion.validarYDerivar(r))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("mayor que el peso inicial");
    }

    @Test
    void noPermiteCaloriasPorDebajoDelMinimoSeguro() {
        // Hombre: minimo 1500 kcal. Con 1000 kcal se trataria de un deficit extremo.
        assertThatThrownBy(() -> ReglasPrediccion.validarYDerivar(perderTipica(1000.0)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("1500");
    }

    @Test
    void noPermiteUnDeficitDiarioFueraDelRangoDeEntrenamiento() {
        // Gasto 2833; con 2000 kcal el deficit seria 833 (> 600 kcal entrenados).
        assertThatThrownBy(() -> ReglasPrediccion.validarYDerivar(perderTipica(2000.0)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("supera 600 kcal");
    }

    @Test
    void rechazaUnPesoObjetivoQueDejaIMCBajo() {
        // Altura 170 cm: IMC 18,5 corresponde a 53,5 kg. Con 52 kg el objetivo queda por debajo.
        PrediccionRequest r = new PrediccionRequest(60.0, 52.0, 170.0, 28, "FEMENINO", "PERDER_PESO",
                "MODERADO", "INTERMEDIO", "MIXTO", 4, 45, 0, 1500.0, 90.0, 85.0);

        assertThatThrownBy(() -> ReglasPrediccion.validarYDerivar(r))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("18,5");
    }

    @Test
    void generaTodosLosAtributosQueEspera() {
        ReglasPrediccion.Derivados d = ReglasPrediccion.validarYDerivar(perderTipica(2430.0));

        assertThat(ReglasPrediccion.aAtributos(perderTipica(2430.0), d)).hasSize(18)
                .containsKeys("edad", "sexo", "altura_cm", "peso_inicial_kg", "peso_objetivo_kg",
                        "diferencia_peso_kg", "imc_inicial", "objetivo", "nivel_actividad", "nivel_experiencia",
                        "entrenamientos_semana", "minutos_sesion", "tipo_actividad", "consumo_energetico_kcal",
                        "balance_energetico_kcal", "restricciones_total", "adherencia_nutricional_pct",
                        "adherencia_ejercicio_pct");
    }

    /** Hombre de 28 anos, 90 kg, 170 cm, actividad moderada, PERDER_PESO hacia 80 kg. */
    static PrediccionRequest perderTipica(double caloriasObjetivo) {
        return new PrediccionRequest(90.0, 80.0, 170.0, 28, "MASCULINO", "PERDER_PESO", "MODERADO",
                "INTERMEDIO", "MIXTO", 4, 45, 0, caloriasObjetivo, 90.0, 85.0);
    }
}
