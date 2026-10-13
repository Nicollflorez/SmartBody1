package com.smartbody.planes.service.reglas;

import com.smartbody.planes.entity.Alimento;
import com.smartbody.planes.entity.MomentoComida;

import java.util.List;
import java.util.Set;

/**
 * Estrategia de generacion del plan nutricional. El MVP usa reglas; un agente IA
 * podra implementar esta misma interfaz desde el backend.
 */
public interface GeneradorPlanNutricional {

    int caloriasObjetivo(SolicitudNutricion solicitud);

    List<AlimentoPlanificado> generar(SolicitudNutricion solicitud, List<Alimento> catalogo);

    /** Alimentos compatibles del mismo grupo, adecuados para esa comida, que pueden reemplazar a {@code actual}. */
    List<Alimento> alternativas(Alimento actual, MomentoComida momento, Set<Long> excluidos,
                                SolicitudNutricion solicitud, List<Alimento> catalogo);

    /** Gramos del nuevo alimento que aportan aproximadamente las mismas calorias que el actual. */
    double gramosEquivalentes(Alimento actual, double gramosActuales, Alimento nuevo);

    /** Receta de una comida con sus ingredientes, pasos de preparacion y bebida sugerida. */
    Receta receta(MomentoComida momento, List<AlimentoPlanificado> alimentos);

    /** Litros de agua recomendados y consejos sobre bebidas. */
    Hidratacion hidratacion(SolicitudNutricion solicitud);
}
