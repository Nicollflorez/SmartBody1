package com.smartbody.presenter.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbody.model.dto.EjercicioDto
import com.smartbody.model.dto.ItemRutinaDto
import com.smartbody.model.repository.Resultado
import com.smartbody.model.repository.RutinaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SustituirEjercicioUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val actual: ItemRutinaDto? = null,
    val alternativas: List<EjercicioDto> = emptyList(),
    val seleccionado: Long? = null,
    val guardando: Boolean = false,
    val mensajeError: String? = null,
    val sustituido: Boolean = false
)

/** RF09 - reemplazo de un ejercicio por una alternativa compatible. */
class SustituirEjercicioPresenter(
    private val rutinaRepository: RutinaRepository,
    private val rutinaId: Long,
    private val itemId: Long
) : ViewModel() {

    private val _estado = MutableStateFlow(SustituirEjercicioUiState())
    val estado: StateFlow<SustituirEjercicioUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        _estado.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            val rutina = async { rutinaRepository.rutina(rutinaId) }
            val alternativas = async { rutinaRepository.alternativas(rutinaId, itemId) }

            val actual = when (val r = rutina.await()) {
                is Resultado.Exito -> r.datos.dias.flatMap { it.ejercicios }.firstOrNull { it.id == itemId }
                is Resultado.Error -> return@launch errorDeCarga(r.mensaje)
            }
            when (val a = alternativas.await()) {
                is Resultado.Exito -> _estado.update { it.copy(cargando = false, actual = actual, alternativas = a.datos) }
                is Resultado.Error -> errorDeCarga(a.mensaje)
            }
        }
    }

    fun seleccionar(ejercicioId: Long) {
        _estado.update { it.copy(seleccionado = ejercicioId) }
    }

    fun confirmar() {
        val actual = _estado.value
        if (actual.guardando) return
        val seleccionado = actual.seleccionado
        if (seleccionado == null) {
            _estado.update { it.copy(mensajeError = "Elige el ejercicio que quieres hacer.") }
            return
        }

        _estado.update { it.copy(guardando = true, mensajeError = null) }
        viewModelScope.launch {
            when (val resultado = rutinaRepository.sustituir(rutinaId, itemId, seleccionado)) {
                is Resultado.Exito -> _estado.update { it.copy(guardando = false, sustituido = true) }
                is Resultado.Error -> _estado.update { it.copy(guardando = false, mensajeError = resultado.mensaje) }
            }
        }
    }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensajeError = null) }
    }

    private fun errorDeCarga(mensaje: String) {
        _estado.update { it.copy(cargando = false, errorCarga = mensaje) }
    }
}
