package com.smartbody.presenter.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbody.model.dto.EjercicioDto
import com.smartbody.model.dto.ItemRutinaDto
import com.smartbody.model.repository.Resultado
import com.smartbody.model.repository.RutinaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalleEjercicioUiState(
    val cargando: Boolean = true,
    val item: ItemRutinaDto? = null,
    val ejercicio: EjercicioDto? = null,
    val errorCarga: String? = null
) {
    /** Instrucciones separadas en pasos numerados. */
    val pasos: List<String>
        get() = ejercicio?.instrucciones
            ?.split(". ")
            ?.map { it.trim().removeSuffix(".") }
            ?.filter { it.isNotBlank() }
            .orEmpty()
}

/** RF08 - detalle de un ejercicio de la rutina con su prescripcion. */
class DetalleEjercicioPresenter(
    private val rutinaRepository: RutinaRepository,
    private val rutinaId: Long,
    private val itemId: Long
) : ViewModel() {

    private val _estado = MutableStateFlow(DetalleEjercicioUiState())
    val estado: StateFlow<DetalleEjercicioUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        _estado.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            val item = when (val rutina = rutinaRepository.rutina(rutinaId)) {
                is Resultado.Exito -> rutina.datos.dias.flatMap { it.ejercicios }.firstOrNull { it.id == itemId }
                is Resultado.Error -> return@launch errorDeCarga(rutina.mensaje)
            } ?: return@launch errorDeCarga("No se encontró el ejercicio en tu rutina.")

            when (val ejercicio = rutinaRepository.ejercicio(item.ejercicioId)) {
                is Resultado.Exito -> _estado.update { it.copy(cargando = false, item = item, ejercicio = ejercicio.datos) }
                is Resultado.Error -> errorDeCarga(ejercicio.mensaje)
            }
        }
    }

    private fun errorDeCarga(mensaje: String) {
        _estado.update { it.copy(cargando = false, errorCarga = mensaje) }
    }
}
