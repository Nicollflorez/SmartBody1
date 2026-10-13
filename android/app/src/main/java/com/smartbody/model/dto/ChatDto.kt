package com.smartbody.model.dto

/** Chatbot: pregunta al asistente y su respuesta. */

data class MensajeChatRequest(
    val mensaje: String
)

data class RespuestaChatDto(
    val respuesta: String,
    val sugerencias: List<String>?
)

/** Mensaje mostrado en la conversacion. */
data class MensajeChat(
    val id: Long,
    val texto: String,
    val deUsuario: Boolean
)
