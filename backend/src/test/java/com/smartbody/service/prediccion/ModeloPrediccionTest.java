package com.smartbody.service.prediccion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Carga del modelo Weka entrenado y construccion de instancias respetando el entrenamiento. */
class ModeloPrediccionTest {

    private ModeloPrediccion modelo;

    @BeforeEach
    void cargar() {
        modelo = new ModeloPrediccion();
        modelo.cargar();
    }

    @Test
    void cargaElModeloEntrenadoDesdeLosRecursos() {
        assertThat(modelo.disponible()).isTrue();
    }

    @Test
    void laPrediccionEstaEnElRangoDelEntrenamientoYEsReproducible() {
        Map<String, Object> valores = entradaTipica();

        double primera = modelo.predecir(valores);
        double segunda = modelo.predecir(valores);

        assertThat(primera).isBetween(2.0, 104.0);
        assertThat(segunda).isEqualTo(primera);
    }

    @Test
    void rechazaUnValorNominalQueNoExisteEnElEntrenamiento() {
        Map<String, Object> valores = entradaTipica();
        valores.put("objetivo", "BAJAR_TODO");

        assertThatThrownBy(() -> modelo.predecir(valores)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaUnAtributoFaltante() {
        Map<String, Object> valores = entradaTipica();
        valores.remove("imc_inicial");

        assertThatThrownBy(() -> modelo.predecir(valores))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("imc_inicial");
    }

    @Test
    void sinModeloCargadoNoDisponibleYNoPredice() {
        ModeloPrediccion sinCargar = new ModeloPrediccion();

        assertThat(sinCargar.disponible()).isFalse();
        assertThatThrownBy(() -> sinCargar.predecir(entradaTipica())).isInstanceOf(IllegalStateException.class);
    }

    /** Persona tipica de PERDER_PESO con los 18 atributos que espera el modelo. */
    static Map<String, Object> entradaTipica() {
        Map<String, Object> v = new HashMap<>();
        v.put("edad", 28.0);
        v.put("sexo", "MASCULINO");
        v.put("altura_cm", 170.0);
        v.put("peso_inicial_kg", 90.0);
        v.put("peso_objetivo_kg", 80.0);
        v.put("diferencia_peso_kg", 10.0);
        v.put("imc_inicial", 31.14);
        v.put("objetivo", "PERDER_PESO");
        v.put("nivel_actividad", "MODERADO");
        v.put("nivel_experiencia", "INTERMEDIO");
        v.put("entrenamientos_semana", 4.0);
        v.put("minutos_sesion", 45.0);
        v.put("tipo_actividad", "MIXTO");
        v.put("consumo_energetico_kcal", 2833.0);
        v.put("balance_energetico_kcal", 400.0);
        v.put("restricciones_total", 0.0);
        v.put("adherencia_nutricional_pct", 90.0);
        v.put("adherencia_ejercicio_pct", 85.0);
        return v;
    }
}
