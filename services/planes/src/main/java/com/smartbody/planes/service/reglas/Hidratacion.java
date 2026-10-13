package com.smartbody.planes.service.reglas;

import java.util.List;

/** Recomendacion diaria de agua y de bebidas. */
public record Hidratacion(double litrosDiaEntrenamiento,
                          double litrosDiaDescanso,
                          int vasosDiaEntrenamiento,
                          List<String> recomendaciones,
                          List<String> evitar) {
}
