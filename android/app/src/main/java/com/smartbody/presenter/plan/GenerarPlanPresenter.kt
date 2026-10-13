package com.smartbody.presenter.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbody.model.dto.PerfilDto
import com.smartbody.model.repository.NutricionRepository
import com.smartbody.model.repository.PerfilRepository
import com.smartbody.model.repository.Resultado
import com.smartbody.model.repository.RutinaRepository
import com.smartbody.notificaciones.NotificadorRutina
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Etapa en curso mientras se genera el plan. */
enum class EtapaGeneracion { RUTINA, NUTRICION }

data class GenerarPlanUiState(
    val cargando: Boolean = true,
    val perfil: PerfilDto? = null,
    val errorCarga: String? = null,
    val etapa: EtapaGeneracion? = null,
    val mensajeError: String? = null,
    val generado: Boolean = false
) {
    val generando: Boolean get() = etapa != null
}

/** RF06 - genera la rutina y el plan nutricional a partir del perfil del usuario. */
class GenerarPlanPresenter(
    private val perfilRepository: PerfilRepository,
    private val rutinaRepository: RutinaRepository,
    private val nutricionRepository: NutricionRepository,
    private val notificadorRutina: NotificadorRutina
) : ViewModel() {

    private val _estado = MutableStateFlow(GenerarPlanUiState())
    val estado: StateFlow<GenerarPlanUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        _estado.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            when (val resultado = perfilRepository.perfil()) {
                is Resultado.Exito -> _estado.update { it.copy(cargando = false, perfil = resultado.datos) }
                is Resultado.Error -> _estado.update { it.copy(cargando = false, errorCarga = resultado.mensaje) }
            }
        }
    }

    fun generar() {
        if (_estado.value.generando) return
        _estado.update { it.copy(etapa = EtapaGeneracion.RUTINA, mensajeError = null) }

        viewModelScope.launch {
            val rutina = rutinaRepository.generar()
            if (rutina is Resultado.Error) {
                _estado.update { it.copy(etapa = null, mensajeError = rutina.mensaje) }
                return@launch
            }
            // RF11: rutina nueva, el aviso de renovacion vuelve a contar 28 dias desde hoy.
            notificadorRutina.programar(0)

            _estado.update { it.copy(etapa = EtapaGeneracion.NUTRICION) }
            when (val plan = nutricionRepository.generar()) {
                is Resultado.Exito -> _estado.update { it.copy(etapa = null, generado = true) }
                is Resultado.Error -> _estado.update {
                    it.copy(
                        etapa = null,
                        mensajeError = "Tu rutina se generó, pero no fue posible crear el plan nutricional. ${plan.mensaje}"
                    )
                }
            }
        }
    }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensajeError = null) }
    }
}
