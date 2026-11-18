package com.smartbody.progreso.dto;

import com.smartbody.progreso.entity.MomentoComida;
import com.smartbody.progreso.service.reglas.Hidratacion;
import com.smartbody.progreso.service.reglas.Receta;

import java.time.LocalDateTime;
import java.util.List;

/** Plan nutricional tal como lo expone el servicio planes (GET /api/nutrition/active). */
public record PlanNutricionalResponse(Long id,
                                      ObjetivoResponse objetivo,
                                      Integer caloriasObjetivo,
                                      int caloriasTotales,
                                      double proteinaG,
                                      double carbohidratosG,
                                      double grasasG,
                                      LocalDateTime generadoEn,
                                      Hidratacion hidratacion,
                                      List<Comida> comidas) {

    public record Comida(MomentoComida momento, String nombre, int calorias, List<Item> alimentos, Receta receta) {
    }

    public record Item(Long id,
                       Long alimentoId,
                       String nombre,
                       String grupo,
                       double cantidad,
                       String unidad,
                       int calorias,
                       double proteinaG,
                       double carbohidratosG,
                       double grasasG,
                       String descripcion,
                       String icono,
                       String alternativa) {
    }
}
