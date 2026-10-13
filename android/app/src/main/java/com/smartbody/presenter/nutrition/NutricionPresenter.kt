package com.smartbody.presenter.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbody.model.dto.PlanNutricionalDto
import com.smartbody.model.repository.NutricionRepository
import com.smartbody.model.repository.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NutricionUiState(
    val cargando: Boolean = true,
    val plan: PlanNutricionalDto? = null,
    val sinPlan: Boolean = false,
    val mensajeError: String? = null
)

/** Consulta del plan nutricional vigente. */
class NutricionPresenter(private val nutricionRepository: NutricionRepository) : ViewModel() {

    private val _estado = MutableStateFlow(NutricionUiState())
    val estado: StateFlow<NutricionUiState> = _estado.asStateFlow()

    fun cargar() {
        _estado.update { it.copy(cargando = it.plan == null, mensajeError = null) }
        viewModelScope.launch {
            when (val resultado = nutricionRepository.activo()) {
                is Resultado.Exito -> _estado.update { it.copy(cargando = false, plan = resultado.datos, sinPlan = false) }
                is Resultado.Error -> _estado.update {
                    if (resultado.codigo == 404) {
                        it.copy(cargando = false, plan = null, sinPlan = true)
                    } else {
                        it.copy(cargando = false, mensajeError = resultado.mensaje)
                    }
                }
            }
        }
    }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensajeError = null) }
    }
}
