package com.smartbody.presenter.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbody.model.dto.AlimentoDto
import com.smartbody.model.dto.ItemPlanDto
import com.smartbody.model.repository.NutricionRepository
import com.smartbody.model.repository.Resultado
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SustituirAlimentoUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val actual: ItemPlanDto? = null,
    val alternativas: List<AlimentoDto> = emptyList(),
    val seleccionado: Long? = null,
    val guardando: Boolean = false,
    val mensajeError: String? = null,
    val sustituido: Boolean = false
)

/** RF09 - reemplazo de un alimento del plan por otro compatible del mismo grupo. */
class SustituirAlimentoPresenter(
    private val nutricionRepository: NutricionRepository,
    private val planId: Long,
    private val itemId: Long
) : ViewModel() {

    private val _estado = MutableStateFlow(SustituirAlimentoUiState())
    val estado: StateFlow<SustituirAlimentoUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        _estado.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            val plan = async { nutricionRepository.activo() }
            val alternativas = async { nutricionRepository.alternativas(planId, itemId) }

            val actual = when (val p = plan.await()) {
                is Resultado.Exito -> p.datos.comidas.flatMap { it.alimentos }.firstOrNull { it.id == itemId }
                is Resultado.Error -> return@launch errorDeCarga(p.mensaje)
            }
            when (val a = alternativas.await()) {
                is Resultado.Exito -> _estado.update { it.copy(cargando = false, actual = actual, alternativas = a.datos) }
                is Resultado.Error -> errorDeCarga(a.mensaje)
            }
        }
    }

    fun seleccionar(alimentoId: Long) {
        _estado.update { it.copy(seleccionado = alimentoId) }
    }

    fun confirmar() {
        val actual = _estado.value
        if (actual.guardando) return
        val seleccionado = actual.seleccionado
        if (seleccionado == null) {
            _estado.update { it.copy(mensajeError = "Elige el alimento que prefieres.") }
            return
        }

        _estado.update { it.copy(guardando = true, mensajeError = null) }
        viewModelScope.launch {
            when (val resultado = nutricionRepository.sustituir(planId, itemId, seleccionado)) {
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
