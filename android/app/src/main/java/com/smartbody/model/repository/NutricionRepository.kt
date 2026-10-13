package com.smartbody.model.repository

import com.smartbody.model.dto.AlimentoDto
import com.smartbody.model.dto.PlanNutricionalDto
import com.smartbody.model.dto.SustitucionRequest
import com.smartbody.network.ApiService

/** Plan nutricional y sustitucion de alimentos. */
class NutricionRepository(private val api: ApiService) {

    suspend fun generar(): Resultado<PlanNutricionalDto> = ejecutarPeticion { api.generarPlanNutricional() }

    /** Un error 404 significa que el usuario aun no tiene plan. */
    suspend fun activo(): Resultado<PlanNutricionalDto> = ejecutarPeticion { api.planNutricionalActivo() }

    suspend fun alternativas(planId: Long, itemId: Long): Resultado<List<AlimentoDto>> =
        ejecutarPeticion { api.alternativasAlimento(planId, itemId) }

    suspend fun sustituir(planId: Long, itemId: Long, nuevoAlimentoId: Long): Resultado<PlanNutricionalDto> =
        ejecutarPeticion { api.sustituirAlimento(planId, SustitucionRequest(itemId, nuevoAlimentoId)) }
}
