package com.smartbody.model.repository

import com.smartbody.model.dto.EjercicioDto
import com.smartbody.model.dto.RutinaDto
import com.smartbody.model.dto.SustitucionRequest
import com.smartbody.network.ApiService

/** Rutinas de entrenamiento y detalle de ejercicios (RF06 - RF09). */
class RutinaRepository(private val api: ApiService) {

    suspend fun generar(): Resultado<RutinaDto> = ejecutarPeticion { api.generarRutina() }

    /** Un error 404 significa que el usuario aun no tiene rutina. */
    suspend fun activa(): Resultado<RutinaDto> = ejecutarPeticion { api.rutinaActiva() }

    suspend fun rutina(id: Long): Resultado<RutinaDto> = ejecutarPeticion { api.rutina(id) }

    suspend fun ejercicio(id: Long): Resultado<EjercicioDto> = ejecutarPeticion { api.ejercicio(id) }

    suspend fun alternativas(rutinaId: Long, itemId: Long): Resultado<List<EjercicioDto>> =
        ejecutarPeticion { api.alternativasEjercicio(rutinaId, itemId) }

    suspend fun sustituir(rutinaId: Long, itemId: Long, nuevoEjercicioId: Long): Resultado<RutinaDto> =
        ejecutarPeticion { api.sustituirEjercicio(rutinaId, SustitucionRequest(itemId, nuevoEjercicioId)) }
}
