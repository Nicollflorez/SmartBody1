package com.smartbody.presenter.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbody.model.dto.MensajeChat
import com.smartbody.model.repository.AuthRepository
import com.smartbody.model.repository.ChatRepository
import com.smartbody.model.repository.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val mensajes: List<MensajeChat> = emptyList(),
    val texto: String = "",
    val sugerencias: List<String> = emptyList(),
    val enviando: Boolean = false
) {
    val puedeEnviar: Boolean get() = texto.isNotBlank() && !enviando
}

/** Presenter del chatbot: envia las preguntas al asistente y mantiene la conversacion. */
class ChatPresenter(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        const val LONGITUD_MAXIMA = 500

        /** Saludo calido y motivador con el primer nombre del usuario. */
        fun bienvenida(nombreCompleto: String?): String {
            val nombre = nombreCompleto?.trim()?.split(Regex("\\s+"))?.firstOrNull()
                ?.replaceFirstChar { it.uppercase() }
                ?.takeIf { it.isNotBlank() }
            val saludo = if (nombre != null) "¡Hola, $nombre! 👋" else "¡Hola! 👋"
            return "$saludo ¡Qué alegría tenerte por aquí!\n\n" +
                "Soy tu asistente de Smartbody y estoy para acompañarte en cada paso hacia tu mejor versión. " +
                "Pregúntame por tu rutina, tus comidas y recetas, cuánta agua tomar o tu progreso.\n\n" +
                "¡Hoy es un gran día para avanzar! 💪"
        }

        private val SUGERENCIAS_INICIALES = listOf(
            "¿Qué ejercicios tengo en el día 1?",
            "¿Cuánta agua debo tomar?",
            "¿Qué como en el almuerzo?",
            "¿Cómo va mi progreso?"
        )
    }

    private val _estado = MutableStateFlow(ChatUiState())
    val estado: StateFlow<ChatUiState> = _estado.asStateFlow()

    init {
        if (chatRepository.historial.isEmpty()) {
            viewModelScope.launch {
                chatRepository.agregar(bienvenida(authRepository.nombreDeSesion()), deUsuario = false)
                chatRepository.actualizarSugerencias(SUGERENCIAS_INICIALES)
                sincronizar()
            }
        } else {
            sincronizar()
        }
    }

    fun alCambiarTexto(valor: String) {
        _estado.update { it.copy(texto = valor.take(LONGITUD_MAXIMA)) }
    }

    fun enviar() = enviarTexto(_estado.value.texto)

    fun elegirSugerencia(sugerencia: String) = enviarTexto(sugerencia)

    private fun enviarTexto(valor: String) {
        val pregunta = valor.trim()
        if (pregunta.isEmpty() || _estado.value.enviando) return

        chatRepository.agregar(pregunta, deUsuario = true)
        _estado.update { it.copy(texto = "", enviando = true, sugerencias = emptyList()) }
        sincronizar()

        viewModelScope.launch {
            val resultado = chatRepository.preguntar(pregunta)
            if (resultado is Resultado.Error) {
                chatRepository.agregar("😕 ${resultado.mensaje}", deUsuario = false)
                chatRepository.actualizarSugerencias(listOf(pregunta))
            }
            _estado.update { it.copy(enviando = false) }
            sincronizar()
        }
    }

    private fun sincronizar() {
        _estado.update { it.copy(mensajes = chatRepository.historial, sugerencias = chatRepository.sugerencias) }
    }
}
