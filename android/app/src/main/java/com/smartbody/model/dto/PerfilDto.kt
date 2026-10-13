package com.smartbody.model.dto

/** Objetos del perfil (RF03, RF04, RF05). Los campos opcionales pueden no venir en la respuesta. */

data class ObjetivoDto(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val descripcion: String?
)

data class RestriccionDto(
    val id: Long,
    val codigo: String,
    val tipo: String,
    val nombre: String,
    val descripcion: String?
)

data class PerfilDto(
    val edad: Int?,
    val sexo: String?,
    val pesoKg: Double?,
    val alturaCm: Double?,
    val nivelExperiencia: String?,
    val minutosPorSesion: Int?,
    val diasPorSemana: Int?,
    val objetivo: ObjetivoDto?,
    val restricciones: List<RestriccionDto>?,
    val imc: Double?,
    val clasificacionImc: String?,
    val completo: Boolean
)

data class PerfilRequest(
    val edad: Int,
    val sexo: String,
    val pesoKg: Double,
    val alturaCm: Double,
    val nivelExperiencia: String,
    val minutosPorSesion: Int,
    val diasPorSemana: Int,
    val objetivoId: Long,
    val restriccionIds: List<Long>
)

/** Datos de la pantalla de inicio. */
data class ResumenDto(
    val nombre: String,
    val email: String,
    val perfilCompleto: Boolean,
    val objetivo: String?,
    val pesoKg: Double?,
    val imc: Double?,
    val clasificacionImc: String?,
    val diasPorSemana: Int?,
    val minutosPorSesion: Int?,
    val rutinaActivaId: Long?,
    val planActivoId: Long?,
    val diasDesdeRutina: Long?,
    val sugerirCambioRutina: Boolean,
    val esAdministrador: Boolean = false
)
