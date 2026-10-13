package com.smartbody.network

import com.smartbody.model.dto.AlimentoDto
import com.smartbody.model.dto.CambioEstadoRequest
import com.smartbody.model.dto.DetalleUsuarioAdminDto
import com.smartbody.model.dto.ReporteEfectividadDto
import com.smartbody.model.dto.ResumenAdminDto
import com.smartbody.model.dto.UsuarioAdminDto
import com.smartbody.model.dto.AuthResponse
import com.smartbody.model.dto.EjercicioDto
import com.smartbody.model.dto.LoginRequest
import com.smartbody.model.dto.MedidaDto
import com.smartbody.model.dto.MedidaRequest
import com.smartbody.model.dto.MensajeChatRequest
import com.smartbody.model.dto.RespuestaChatDto
import com.smartbody.model.dto.MensajeResponse
import com.smartbody.model.dto.ObjetivoDto
import com.smartbody.model.dto.PerfilDto
import com.smartbody.model.dto.PerfilRequest
import com.smartbody.model.dto.PlanNutricionalDto
import com.smartbody.model.dto.ProgresoDto
import com.smartbody.model.dto.RegistroRequest
import com.smartbody.model.dto.RestriccionDto
import com.smartbody.model.dto.ResumenDto
import com.smartbody.model.dto.RutinaDto
import com.smartbody.model.dto.SustitucionRequest
import com.smartbody.model.dto.UsuarioDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

/** Contrato de la API REST de Smartbody. */
interface ApiService {

    @GET("api/ping")
    suspend fun ping(): Response<Map<String, String>>

    // --- Autenticacion (RF01, RF02, RF12) ---

    @POST("api/auth/register")
    suspend fun registrar(@Body peticion: RegistroRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body peticion: LoginRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<MensajeResponse>

    // --- Usuario y perfil (RF03, RF04, RF05) ---

    @GET("api/users/me")
    suspend fun usuarioActual(): Response<UsuarioDto>

    @GET("api/users/summary")
    suspend fun resumen(): Response<ResumenDto>

    @GET("api/users/profile")
    suspend fun perfil(): Response<PerfilDto>

    @PUT("api/users/profile")
    suspend fun actualizarPerfil(@Body peticion: PerfilRequest): Response<PerfilDto>

    @Multipart
    @PUT("api/users/photo")
    suspend fun subirFoto(@Part foto: MultipartBody.Part): Response<MensajeResponse>

    /** Imagen de la foto de perfil; 404 si el usuario no tiene foto. */
    @GET("api/users/photo")
    suspend fun foto(): Response<ResponseBody>

    @DELETE("api/users/photo")
    suspend fun eliminarFoto(): Response<MensajeResponse>

    @GET("api/objectives")
    suspend fun objetivos(): Response<List<ObjetivoDto>>

    @GET("api/restrictions")
    suspend fun restricciones(): Response<List<RestriccionDto>>

    // --- Rutinas (RF06 - RF09) ---

    @POST("api/routines/generate")
    suspend fun generarRutina(): Response<RutinaDto>

    @GET("api/routines/active")
    suspend fun rutinaActiva(): Response<RutinaDto>

    @GET("api/routines/{id}")
    suspend fun rutina(@Path("id") id: Long): Response<RutinaDto>

    @GET("api/routines/{id}/items/{itemId}/alternatives")
    suspend fun alternativasEjercicio(@Path("id") rutinaId: Long, @Path("itemId") itemId: Long): Response<List<EjercicioDto>>

    @POST("api/routines/{id}/replace-exercise")
    suspend fun sustituirEjercicio(@Path("id") rutinaId: Long, @Body peticion: SustitucionRequest): Response<RutinaDto>

    @GET("api/exercises/{id}")
    suspend fun ejercicio(@Path("id") id: Long): Response<EjercicioDto>

    // --- Nutricion ---

    @POST("api/nutrition/generate")
    suspend fun generarPlanNutricional(): Response<PlanNutricionalDto>

    @GET("api/nutrition/active")
    suspend fun planNutricionalActivo(): Response<PlanNutricionalDto>

    @GET("api/nutrition/{id}/items/{itemId}/alternatives")
    suspend fun alternativasAlimento(@Path("id") planId: Long, @Path("itemId") itemId: Long): Response<List<AlimentoDto>>

    @POST("api/nutrition/{id}/replace-food")
    suspend fun sustituirAlimento(@Path("id") planId: Long, @Body peticion: SustitucionRequest): Response<PlanNutricionalDto>

    // --- Administracion (solo ROLE_ADMIN) ---

    @GET("api/admin/summary")
    suspend fun resumenAdmin(): Response<ResumenAdminDto>

    @GET("api/admin/users")
    suspend fun usuariosAdmin(): Response<List<UsuarioAdminDto>>

    @PUT("api/admin/users/{id}/status")
    suspend fun cambiarEstadoUsuario(@Path("id") usuarioId: Long, @Body peticion: CambioEstadoRequest): Response<MensajeResponse>

    @GET("api/admin/users/{id}")
    suspend fun detalleUsuarioAdmin(@Path("id") usuarioId: Long): Response<DetalleUsuarioAdminDto>

    @POST("api/admin/users/{id}/plan")
    suspend fun generarPlanUsuario(@Path("id") usuarioId: Long): Response<DetalleUsuarioAdminDto>

    @GET("api/admin/reports/effectiveness")
    suspend fun reporteEfectividad(): Response<ReporteEfectividadDto>

    // --- Chatbot ---

    @POST("api/chat")
    suspend fun chat(@Body peticion: MensajeChatRequest): Response<RespuestaChatDto>

    // --- Progreso (RF10) ---

    @GET("api/progress")
    suspend fun progreso(): Response<ProgresoDto>

    @POST("api/progress")
    suspend fun registrarMedida(@Body peticion: MedidaRequest): Response<MedidaDto>
}
