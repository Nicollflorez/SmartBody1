package com.smartbody.model.repository

import com.smartbody.model.dto.PrediccionDto
import com.smartbody.model.dto.PrediccionRequest
import com.smartbody.model.dto.PrediccionResumenDto
import com.smartbody.model.dto.SeguimientoPrediccionDto
import com.smartbody.network.ApiService

/** Estimacion del tiempo hasta el peso objetivo y su seguimiento (backend progreso). */
class PrediccionRepository(private val api: ApiService) {

    suspend fun predecir(peticion: PrediccionRequest): Resultado<PrediccionDto> =
        ejecutarPeticion { api.predecirTiempoPesoObjetivo(peticion) }

    suspend fun historial(): Resultado<List<PrediccionResumenDto>> =
        ejecutarPeticion { api.historialPredicciones() }

    suspend fun seguimiento(id: Long): Resultado<SeguimientoPrediccionDto> =
        ejecutarPeticion { api.seguimientoPrediccion(id) }
}
