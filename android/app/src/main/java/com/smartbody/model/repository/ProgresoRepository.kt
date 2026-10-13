package com.smartbody.model.repository

import com.smartbody.model.dto.MedidaDto
import com.smartbody.model.dto.MedidaRequest
import com.smartbody.model.dto.ProgresoDto
import com.smartbody.network.ApiService

/** Registro de medidas e historial de progreso (RF10). */
class ProgresoRepository(private val api: ApiService) {

    suspend fun progreso(): Resultado<ProgresoDto> = ejecutarPeticion { api.progreso() }

    suspend fun registrar(medida: MedidaRequest): Resultado<MedidaDto> = ejecutarPeticion { api.registrarMedida(medida) }
}
