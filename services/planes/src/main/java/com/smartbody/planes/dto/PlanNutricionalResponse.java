package com.smartbody.planes.dto;

import com.smartbody.planes.entity.MomentoComida;
import com.smartbody.planes.entity.PlanAlimento;
import com.smartbody.planes.entity.PlanNutricional;
import com.smartbody.planes.service.reglas.Hidratacion;
import com.smartbody.planes.service.reglas.Receta;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Plan nutricional diario agrupado por comidas. */
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

    /** Alimento recomendado. {@code id} identifica el item del plan; {@code alternativa} sugiere un reemplazo. */
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

    public static PlanNutricionalResponse desde(PlanNutricional plan,
                                                Map<Long, String> alternativas,
                                                Map<MomentoComida, Receta> recetas,
                                                Hidratacion hidratacion) {
        List<Comida> comidas = java.util.Arrays.stream(MomentoComida.values())
                .map(momento -> {
                    List<Item> items = plan.getAlimentos().stream()
                            .filter(a -> a.getMomento() == momento)
                            .sorted(Comparator.comparing(PlanAlimento::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                            .map(a -> item(a, alternativas.get(a.getId())))
                            .toList();
                    return new Comida(momento, momento.getNombre(), items.stream().mapToInt(Item::calorias).sum(), items,
                            recetas.get(momento));
                })
                .filter(comida -> !comida.alimentos().isEmpty())
                .toList();

        List<PlanAlimento> todos = plan.getAlimentos();
        return new PlanNutricionalResponse(plan.getId(), ObjetivoResponse.desde(plan.getObjetivo()),
                plan.getCaloriasObjetivo(),
                comidas.stream().mapToInt(Comida::calorias).sum(),
                redondear(todos.stream().mapToDouble(a -> a.aporte(a.getAlimento().getProteinaG())).sum()),
                redondear(todos.stream().mapToDouble(a -> a.aporte(a.getAlimento().getCarbohidratosG())).sum()),
                redondear(todos.stream().mapToDouble(a -> a.aporte(a.getAlimento().getGrasasG())).sum()),
                plan.getGeneradoEn(), hidratacion, comidas);
    }

    private static Item item(PlanAlimento a, String alternativa) {
        return new Item(a.getId(), a.getAlimento().getId(), a.getAlimento().getNombre(), a.getAlimento().getGrupo(),
                a.getCantidad(), a.getUnidad(),
                (int) Math.round(a.aporte(a.getAlimento().getCaloriasPor100g())),
                redondear(a.aporte(a.getAlimento().getProteinaG())),
                redondear(a.aporte(a.getAlimento().getCarbohidratosG())),
                redondear(a.aporte(a.getAlimento().getGrasasG())),
                a.getAlimento().getDescripcion(), a.getAlimento().getIcono(), alternativa);
    }

    private static double redondear(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
}
