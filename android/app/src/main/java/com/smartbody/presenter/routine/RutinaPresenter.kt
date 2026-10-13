package com.smartbody.presenter.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbody.model.dto.RutinaDto
import com.smartbody.model.repository.Resultado
import com.smartbody.model.repository.RutinaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RutinaUiState(
    val cargando: Boolean = true,
    val rutina: RutinaDto? = null,
    val sinRutina: Boolean = false,
    val diaSeleccionado: Int = 1,
    val mensajeError: String? = null
)

/** RF07 - consulta de la rutina vigente organizada por dias. */
class RutinaPresenter(private val rutinaRepository: RutinaRepository) : ViewModel() {

    private val _estado = MutableStateFlow(RutinaUiState())
    val estado: StateFlow<RutinaUiState> = _estado.asStateFlow()

    fun cargar() {
        _estado.update { it.copy(cargando = it.rutina == null, mensajeError = null) }
        viewModelScope.launch {
            when (val resultado = rutinaRepository.activa()) {
                is Resultado.Exito -> _estado.update {
                    val dias = resultado.datos.dias.map { d -> d.numero }
                    it.copy(
                        cargando = false,
                        rutina = resultado.datos,
                        sinRutina = false,
                        diaSeleccionado = if (it.diaSeleccionado in dias) it.diaSeleccionado else dias.firstOrNull() ?: 1
                    )
                }

                is Resultado.Error -> _estado.update {
                    if (resultado.codigo == 404) {
                        it.copy(cargando = false, rutina = null, sinRutina = true)
                    } else {
                        it.copy(cargando = false, mensajeError = resultado.mensaje)
                    }
                }
            }
        }
    }

    fun seleccionarDia(numero: Int) {
        _estado.update { it.copy(diaSeleccionado = numero) }
    }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensajeError = null) }
    }
}
