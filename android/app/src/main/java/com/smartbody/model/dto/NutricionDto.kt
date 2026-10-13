package com.smartbody.model.dto

/** Plan nutricional diario agrupado por comidas. */

data class PlanNutricionalDto(
    val id: Long,
    val objetivo: ObjetivoDto?,
    val caloriasObjetivo: Int?,
    val caloriasTotales: Int,
    val proteinaG: Double,
    val carbohidratosG: Double,
    val grasasG: Double,
    val generadoEn: String?,
    val hidratacion: HidratacionDto?,
    val comidas: List<ComidaDto>
)

data class ComidaDto(
    val momento: String,
    val nombre: String,
    val calorias: Int,
    val alimentos: List<ItemPlanDto>,
    val receta: RecetaDto?
)

/** Receta armada con los alimentos y cantidades de la comida. */
data class RecetaDto(
    val titulo: String,
    val ingredientes: List<String>,
    val pasos: List<String>,
    val bebida: String?
)

/** Agua recomendada al dia y consejos sobre bebidas. */
data class HidratacionDto(
    val litrosDiaEntrenamiento: Double,
    val litrosDiaDescanso: Double,
    val vasosDiaEntrenamiento: Int,
    val recomendaciones: List<String>,
    val evitar: List<String>
)

/** Alimento recomendado. [id] identifica el item del plan; [alternativa] sugiere un reemplazo. */
data class ItemPlanDto(
    val id: Long,
    val alimentoId: Long,
    val nombre: String,
    val grupo: String,
    val cantidad: Double,
    val unidad: String,
    val calorias: Int,
    val proteinaG: Double,
    val carbohidratosG: Double,
    val grasasG: Double,
    val descripcion: String?,
    val icono: String?,
    val alternativa: String?
)

/** Alimento del catalogo con valores por cada 100 g. */
data class AlimentoDto(
    val id: Long,
    val nombre: String,
    val grupo: String,
    val caloriasPor100g: Double,
    val proteinaG: Double,
    val carbohidratosG: Double,
    val grasasG: Double,
    val descripcion: String?,
    val icono: String?
)
