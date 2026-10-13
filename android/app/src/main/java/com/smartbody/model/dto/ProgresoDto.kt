package com.smartbody.model.dto

/** Historial de medidas y progreso (RF10). */

data class MedidaDto(
    val id: Long,
    val pesoKg: Double,
    val alturaCm: Double?,
    val cinturaCm: Double?,
    val caderaCm: Double?,
    val pechoCm: Double?,
    val brazoCm: Double?,
    val imc: Double?,
    val registradoEn: String
)

data class ProgresoDto(
    val medidas: List<MedidaDto>,
    val pesoInicial: Double?,
    val pesoActual: Double?,
    val cambioPesoKg: Double?,
    val imcActual: Double?,
    val clasificacionImc: String?
)

data class MedidaRequest(
    val pesoKg: Double,
    val alturaCm: Double?,
    val cinturaCm: Double?,
    val caderaCm: Double?,
    val pechoCm: Double?,
    val brazoCm: Double?
)
