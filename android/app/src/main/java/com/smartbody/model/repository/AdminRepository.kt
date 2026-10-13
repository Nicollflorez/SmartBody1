package com.smartbody.model.repository

import com.smartbody.model.dto.CambioEstadoRequest
import com.smartbody.model.dto.DetalleUsuarioAdminDto
import com.smartbody.model.dto.MensajeResponse
import com.smartbody.model.dto.ReporteEfectividadDto
import com.smartbody.model.dto.ResumenAdminDto
import com.smartbody.model.dto.UsuarioAdminDto
import com.smartbody.network.ApiService

/** Panel de administracion. El backend responde 403 si la cuenta no es de administrador. */
class AdminRepository(private val api: ApiService) {

    suspend fun resumen(): Resultado<ResumenAdminDto> = ejecutarPeticion { api.resumenAdmin() }

    suspend fun usuarios(): Resultado<List<UsuarioAdminDto>> = ejecutarPeticion { api.usuariosAdmin() }

    suspend fun cambiarEstado(usuarioId: Long, activo: Boolean): Resultado<MensajeResponse> =
        ejecutarPeticion { api.cambiarEstadoUsuario(usuarioId, CambioEstadoRequest(activo)) }

    suspend fun detalleUsuario(usuarioId: Long): Resultado<DetalleUsuarioAdminDto> =
        ejecutarPeticion { api.detalleUsuarioAdmin(usuarioId) }

    suspend fun generarPlan(usuarioId: Long): Resultado<DetalleUsuarioAdminDto> =
        ejecutarPeticion { api.generarPlanUsuario(usuarioId) }

    suspend fun reporteEfectividad(): Resultado<ReporteEfectividadDto> = ejecutarPeticion { api.reporteEfectividad() }
}
