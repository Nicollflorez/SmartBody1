package com.smartbody.model.dto

/** Rutina de entrenamiento (RF06, RF07, RF08, RF09). */

data class RutinaDto(
    val id: Long,
    val nombre: String,
    val objetivo: ObjetivoDto?,
    val diasPorSemana: Int,
    val minutosPorSesion: Int,
    val activa: Boolean,
    val generadaEn: String?,
    val dias: List<DiaRutinaDto>
)

data class DiaRutinaDto(
    val numero: Int,
    val enfoque: String,
    val minutosEstimados: Int,
    val ejercicios: List<ItemRutinaDto>
)

/** Ejercicio dentro de la rutina. [id] identifica el item; [ejercicioId] el ejercicio del catalogo. */
data class ItemRutinaDto(
    val id: Long,
    val ejercicioId: Long,
    val nombre: String,
    val grupoMuscular: String,
    val tipo: String,
    val series: Int,
    val repeticiones: Int?,
    val duracionSeg: Int?,
    val descansoSeg: Int
)

data class EjercicioDto(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val grupoMuscular: String,
    val equipamiento: String?,
    val tipo: String,
    val nivelMinimo: String?,
    val instrucciones: String?,
    val imagenUrl: String?
)

/** Si [nuevoId] es null el backend elige la primera alternativa compatible. */
data class SustitucionRequest(
    val itemId: Long,
    val nuevoId: Long?
)
