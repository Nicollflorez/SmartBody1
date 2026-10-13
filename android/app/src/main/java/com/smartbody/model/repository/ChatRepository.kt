package com.smartbody.model.repository

import com.smartbody.model.dto.MensajeChat
import com.smartbody.model.dto.MensajeChatRequest
import com.smartbody.model.dto.RespuestaChatDto
import com.smartbody.network.ApiService
import java.util.concurrent.atomic.AtomicLong

/**
 * Conversacion con el asistente. El historial se conserva en memoria mientras dure la sesion,
 * para no perderlo al cerrar y volver a abrir el chat, y se borra al cambiar de usuario (RNF06).
 */
class ChatRepository(private val api: ApiService) {

    private val secuencia = AtomicLong()

    var historial: List<MensajeChat> = emptyList()
        private set

    var sugerencias: List<String> = emptyList()
        private set

    fun agregar(texto: String, deUsuario: Boolean): MensajeChat {
        val mensaje = MensajeChat(secuencia.incrementAndGet(), texto, deUsuario)
        historial = historial + mensaje
        return mensaje
    }

    suspend fun preguntar(texto: String): Resultado<RespuestaChatDto> {
        val resultado = ejecutarPeticion { api.chat(MensajeChatRequest(texto)) }
        if (resultado is Resultado.Exito) {
            agregar(resultado.datos.respuesta, deUsuario = false)
            sugerencias = resultado.datos.sugerencias.orEmpty()
        }
        return resultado
    }

    fun actualizarSugerencias(nuevas: List<String>) {
        sugerencias = nuevas
    }

    fun limpiar() {
        historial = emptyList()
        sugerencias = emptyList()
    }
}
