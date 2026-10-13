package com.smartbody.model.repository

import android.net.Uri
import com.smartbody.model.dto.ObjetivoDto
import com.smartbody.model.dto.PerfilDto
import com.smartbody.model.dto.PerfilRequest
import com.smartbody.model.dto.RestriccionDto
import com.smartbody.model.dto.ResumenDto
import com.smartbody.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Perfil del usuario, foto y catalogos de objetivos y restricciones (RF03, RF04, RF05). */
class PerfilRepository(
    private val api: ApiService,
    private val procesadorImagen: ProcesadorImagen
) {

    suspend fun resumen(): Resultado<ResumenDto> = ejecutarPeticion { api.resumen() }

    suspend fun perfil(): Resultado<PerfilDto> = ejecutarPeticion { api.perfil() }

    suspend fun guardarPerfil(peticion: PerfilRequest): Resultado<PerfilDto> =
        ejecutarPeticion { api.actualizarPerfil(peticion) }

    suspend fun objetivos(): Resultado<List<ObjetivoDto>> = ejecutarPeticion { api.objetivos() }

    suspend fun restricciones(): Resultado<List<RestriccionDto>> = ejecutarPeticion { api.restricciones() }

    /** Bytes de la foto de perfil, o null si el usuario aun no tiene una. */
    suspend fun foto(): Resultado<ByteArray?> = when (val resultado = ejecutarPeticion { api.foto() }) {
        is Resultado.Exito -> withContext(Dispatchers.IO) { Resultado.Exito(resultado.datos.bytes()) }
        is Resultado.Error -> if (resultado.codigo == 404) Resultado.Exito(null) else resultado
    }

    /** Prepara la imagen elegida y la sube. Devuelve los bytes enviados para mostrarlos de inmediato. */
    suspend fun subirFoto(uri: Uri): Resultado<ByteArray> {
        val bytes = withContext(Dispatchers.IO) {
            try {
                procesadorImagen.prepararFoto(uri)
            } catch (e: Exception) {
                null
            }
        } ?: return Resultado.Error("No fue posible leer la imagen seleccionada. Intenta con otra foto.")

        val parte = MultipartBody.Part.createFormData(
            "foto",
            "perfil.jpg",
            bytes.toRequestBody("image/jpeg".toMediaType())
        )
        return when (val resultado = ejecutarPeticion { api.subirFoto(parte) }) {
            is Resultado.Exito -> Resultado.Exito(bytes)
            is Resultado.Error -> resultado
        }
    }

    suspend fun eliminarFoto(): Resultado<Unit> = when (val resultado = ejecutarPeticion { api.eliminarFoto() }) {
        is Resultado.Exito -> Resultado.Exito(Unit)
        is Resultado.Error -> resultado
    }
}
