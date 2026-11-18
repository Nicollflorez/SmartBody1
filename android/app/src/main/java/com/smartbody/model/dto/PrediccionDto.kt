package com.smartbody.model.dto

/**
 * Estimacion de semanas hasta el peso objetivo (mineria de datos, backend progreso).
 * Es orientativa: el backend la presenta siempre con el campo [aviso].
 */

data class PrediccionRequest(
    val pesoInicial: Double,
    val pesoObjetivo: Double,
    val alturaCm: Double,
    val edad: Int,
    val sexo: String,
    val objetivo: String,
    val nivelActividad: String,
    val nivelExperiencia: String,
    val tipoActividad: String,
    val entrenamientosSemana: Int,
    val minutosSesion: Int,
    val restriccionesTotal: Int,
    val caloriasObjetivo: Double,
    val adherenciaNutricional: Double,
    val adherenciaEjercicio: Double
)

data class PrediccionDto(
    val id: Long,
    val pesoInicial: Double,
    val pesoObjetivo: Double,
    val objetivo: String,
    val semanasEstimadas: Double,
    val fechaAproximada: String?,
    val modelo: String,
    val versionModelo: String,
    val tipoPrediccion: String,
    val aviso: String,
    val fechaPrediccion: String
)

data class PrediccionResumenDto(
    val id: Long,
    val objetivo: String,
    val pesoInicial: Double,
    val pesoObjetivo: Double,
    val semanasEstimadas: Double,
    val estado: String,
    val fechaPrediccion: String
)

data class PuntoPesoDto(
    val fecha: String,
    val pesoKg: Double
)

/** Comparacion entre la estimacion y las medidas reales. Los campos nulos aun no tienen datos. */
data class SeguimientoPrediccionDto(
    val id: Long,
    val objetivo: String,
    val pesoInicial: Double,
    val pesoObjetivo: Double,
    val semanasEstimadas: Double,
    val pesoActual: Double?,
    val semanasTranscurridas: Double?,
    val avancePorcentaje: Double?,
    val ritmoRealKgSemana: Double?,
    val estado: String,
    val semanasRealesHastaObjetivo: Double?,
    val diferenciaSemanas: Double?,
    val semanasRestantesEstimadas: Double?,
    val ultimaActualizacion: String?,
    val puntos: List<PuntoPesoDto> = emptyList(),
    val aviso: String
)
