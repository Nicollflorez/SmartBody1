package com.smartbody.presenter.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbody.model.dto.MedidaRequest
import com.smartbody.model.dto.ProgresoDto
import com.smartbody.model.repository.ProgresoRepository
import com.smartbody.model.repository.Resultado
import com.smartbody.presenter.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProgresoUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val progreso: ProgresoDto? = null,
    val peso: String = "",
    val cintura: String = "",
    val cadera: String = "",
    val pecho: String = "",
    val brazo: String = "",
    val errorPeso: String? = null,
    val errorCintura: String? = null,
    val errorCadera: String? = null,
    val errorPecho: String? = null,
    val errorBrazo: String? = null,
    val guardando: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null
)

/** RF10 - registro de medidas y consulta de la evolucion. */
class ProgresoPresenter(private val progresoRepository: ProgresoRepository) : ViewModel() {

    private val _estado = MutableStateFlow(ProgresoUiState())
    val estado: StateFlow<ProgresoUiState> = _estado.asStateFlow()

    fun cargar() {
        _estado.update { it.copy(cargando = it.progreso == null, errorCarga = null) }
        viewModelScope.launch {
            when (val resultado = progresoRepository.progreso()) {
                is Resultado.Exito -> _estado.update { it.copy(cargando = false, progreso = resultado.datos) }
                is Resultado.Error -> _estado.update {
                    if (it.progreso == null) it.copy(cargando = false, errorCarga = resultado.mensaje)
                    else it.copy(cargando = false, mensajeError = resultado.mensaje)
                }
            }
        }
    }

    fun alCambiarPeso(valor: String) = _estado.update { it.copy(peso = valor.take(6), errorPeso = null) }

    fun alCambiarCintura(valor: String) = _estado.update { it.copy(cintura = valor.take(6), errorCintura = null) }

    fun alCambiarCadera(valor: String) = _estado.update { it.copy(cadera = valor.take(6), errorCadera = null) }

    fun alCambiarPecho(valor: String) = _estado.update { it.copy(pecho = valor.take(6), errorPecho = null) }

    fun alCambiarBrazo(valor: String) = _estado.update { it.copy(brazo = valor.take(6), errorBrazo = null) }

    fun registrar() {
        val actual = _estado.value
        if (actual.guardando) return

        val errorPeso = Validaciones.validarPeso(actual.peso)
        val errorCintura = Validaciones.validarMedidaOpcional(actual.cintura, "cintura", minimo = 30.0)
        val errorCadera = Validaciones.validarMedidaOpcional(actual.cadera, "cadera", minimo = 30.0)
        val errorPecho = Validaciones.validarMedidaOpcional(actual.pecho, "pecho", minimo = 30.0)
        val errorBrazo = Validaciones.validarMedidaOpcional(actual.brazo, "brazo", maximo = 100.0)
        _estado.update {
            it.copy(
                errorPeso = errorPeso,
                errorCintura = errorCintura,
                errorCadera = errorCadera,
                errorPecho = errorPecho,
                errorBrazo = errorBrazo
            )
        }
        if (listOf(errorPeso, errorCintura, errorCadera, errorPecho, errorBrazo).any { it != null }) return

        val medida = MedidaRequest(
            pesoKg = Validaciones.leerDecimal(actual.peso) ?: return,
            alturaCm = null,
            cinturaCm = Validaciones.leerDecimal(actual.cintura),
            caderaCm = Validaciones.leerDecimal(actual.cadera),
            pechoCm = Validaciones.leerDecimal(actual.pecho),
            brazoCm = Validaciones.leerDecimal(actual.brazo)
        )

        _estado.update { it.copy(guardando = true, mensajeError = null) }
        viewModelScope.launch {
            when (val resultado = progresoRepository.registrar(medida)) {
                is Resultado.Exito -> {
                    _estado.update {
                        it.copy(
                            guardando = false,
                            peso = "", cintura = "", cadera = "", pecho = "", brazo = "",
                            mensajeExito = "Medidas registradas correctamente."
                        )
                    }
                    cargar()
                }

                is Resultado.Error -> _estado.update {
                    it.copy(guardando = false, mensajeError = resultado.errores.values.firstOrNull() ?: resultado.mensaje)
                }
            }
        }
    }

    fun mensajesMostrados() {
        _estado.update { it.copy(mensajeError = null, mensajeExito = null) }
    }
}
