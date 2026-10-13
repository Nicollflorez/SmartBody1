package com.smartbody.model.repository

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.smartbody.model.dto.ErrorResponse
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Ejecuta una llamada a la API y traduce cualquier fallo a un mensaje comprensible.
 * El usuario nunca ve excepciones tecnicas (RNF05, seccion 26 del documento).
 */
suspend fun <T> ejecutarPeticion(llamada: suspend () -> Response<T>): Resultado<T> {
    return try {
        val respuesta = llamada()
        val cuerpo = respuesta.body()

        if (respuesta.isSuccessful && cuerpo != null) {
            Resultado.Exito(cuerpo)
        } else if (respuesta.isSuccessful) {
            Resultado.Error("El servidor no devolvió información.", respuesta.code())
        } else {
            errorDeRespuesta(respuesta)
        }
    } catch (e: SocketTimeoutException) {
        Resultado.Error("El servidor no responde. Verifica que el backend esté encendido e intenta nuevamente.")
    } catch (e: UnknownHostException) {
        Resultado.Error("No fue posible conectarse con el servidor. Revisa tu conexión a internet.")
    } catch (e: ConnectException) {
        Resultado.Error("No fue posible conectarse con el servidor. Verifica que el backend esté encendido.")
    } catch (e: IOException) {
        Resultado.Error("No fue posible conectarse con el servidor.")
    } catch (e: Exception) {
        Resultado.Error("Ocurrió un error inesperado. Intenta nuevamente.")
    }
}

private fun <T> errorDeRespuesta(respuesta: Response<T>): Resultado.Error {
    val error = leerError(respuesta.errorBody()?.string())
    val mensaje = error?.mensaje?.takeIf { it.isNotBlank() } ?: mensajePorCodigo(respuesta.code())
    return Resultado.Error(mensaje, respuesta.code(), error?.errores ?: emptyMap())
}

private fun leerError(json: String?): ErrorResponse? {
    if (json.isNullOrBlank()) return null
    return try {
        Gson().fromJson(json, ErrorResponse::class.java)
    } catch (e: JsonSyntaxException) {
        null
    }
}

/** Traduccion de los codigos HTTP a mensajes de usuario (seccion 23 del documento). */
private fun mensajePorCodigo(codigo: Int): String = when (codigo) {
    400 -> "Los datos enviados no son válidos."
    401 -> "Tu sesión expiró. Inicia sesión nuevamente."
    403 -> "No tienes permisos para realizar esta acción."
    404 -> "No se encontró la información solicitada."
    409 -> "La información ya existe."
    in 500..599 -> "El servidor no está disponible en este momento."
    else -> "No fue posible completar la operación."
}
