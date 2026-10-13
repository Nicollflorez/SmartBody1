package com.smartbody.planes.service.reglas;

import java.util.List;

/** Receta de una comida armada con los alimentos y cantidades del plan. */
public record Receta(String titulo,
                     List<String> ingredientes,
                     List<String> pasos,
                     String bebida) {
}
