package com.smartbody.service.reglas;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/** Un usuario mejora si su evolucion va en la direccion de su objetivo. */
class EvaluadorProgresoTest {

    @ParameterizedTest(name = "{0}: {1} kg -> {2} kg, IMC {3} -> {4} => {5}")
    @CsvSource({
            "PERDER_PESO,         80, 76,   26.1, 24.8, true",
            "PERDER_PESO,         80, 81,   26.1, 26.4, false",
            "GANAR_MASA,          64, 67,   20.2, 21.1, true",
            "GANAR_MASA,          64, 63.5, 20.2, 20.0, false",
            "MANTENER_PESO,       58, 58.8, 22.7, 23.0, true",
            "MANTENER_PESO,       58, 60.5, 22.7, 23.6, false",
            "MEJORAR_CONDICION,   90, 88,   29.4, 28.7, true",
            "MEJORAR_RESISTENCIA, 55, 55.4, 20.2, 20.3, true",
            "MEJORAR_RESISTENCIA, 90, 92,   29.4, 30.0, false"
    })
    void evaluaElProgresoSegunElObjetivo(String objetivo, double pesoInicial, double pesoActual,
                                          double imcInicial, double imcActual, boolean esperado) {
        assertThat(EvaluadorProgreso.mejoro(objetivo, pesoInicial, pesoActual, imcInicial, imcActual)).isEqualTo(esperado);
    }
}
