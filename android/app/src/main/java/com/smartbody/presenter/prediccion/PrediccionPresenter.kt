package com.smartbody.presenter.prediccion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbody.model.dto.PrediccionDto
import com.smartbody.model.dto.SeguimientoPrediccionDto
import com.smartbody.model.repository.NutricionRepository
import com.smartbody.model.repository.PerfilRepository
import com.smartbody.model.repository.PrediccionRepository
import com.smartbody.model.repository.Resultado
import com.smartbody.model.repository.RutinaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrediccionUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val base: DatosBase? = null,
    val rutinaActiva: Boolean = false,
    val entrada: EntradaPrediccion = EntradaPrediccion(),
    val faltantes: List<String> = emptyList(),
    val errores: Map<String, String> = emptyMap(),
    val calculando: Boolean = false,
    val prediccion: PrediccionDto? = null,
    val seguimiento: SeguimientoPrediccionDto? = null,
    val cargandoSeguimiento: Boolean = false,
    val mensajeError: String? = null
)

/** Estimacion del tiempo hasta el peso objetivo y su seguimiento. Sin logica en las pantallas. */
class PrediccionPresenter(
    private val prediccionRepository: PrediccionRepository,
    private val perfilRepository: PerfilRepository,
    private val nutricionRepository: NutricionRepository,
    private val rutinaRepository: RutinaRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(PrediccionUiState())
    val estado: StateFlow<PrediccionUiState> = _estado.asStateFlow()

    /** Carga los datos del perfil, del plan y de la rutina, y el seguimiento de la ultima estimacion. */
    fun cargar() {
        _estado.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            val perfil = when (val r = perfilRepository.perfil()) {
                is Resultado.Exito -> r.datos
                is Resultado.Error -> {
                    _estado.update { it.copy(cargando = false, errorCarga = r.mensaje) }
                    return@launch
                }
            }
            // Sin plan nutricional no hay calorias objetivo: se indica como dato faltante, no como error.
            val caloriasObjetivo = (nutricionRepository.activo() as? Resultado.Exito)?.datos?.caloriasObjetivo
            val rutinaActiva = rutinaRepository.activa() is Resultado.Exito

            val base = DatosBase(
                pesoActualKg = perfil.pesoKg,
                alturaCm = perfil.alturaCm,
                edad = perfil.edad,
                sexo = perfil.sexo,
                objetivoCodigo = perfil.objetivo?.codigo,
                nivelExperiencia = perfil.nivelExperiencia,
                minutosPorSesion = perfil.minutosPorSesion,
                diasPorSemana = perfil.diasPorSemana,
                restricciones = perfil.restricciones?.size ?: 0,
                caloriasObjetivo = caloriasObjetivo
            )
            _estado.update { it.copy(cargando = false, base = base, rutinaActiva = rutinaActiva) }
            cargarSeguimientoMasReciente()
        }
    }

    fun alCambiarPesoObjetivo(valor: String) = actualizarEntrada { it.copy(pesoObjetivo = valor.take(6)) }

    fun alCambiarNivelActividad(valor: String) = actualizarEntrada { it.copy(nivelActividad = valor) }

    fun alCambiarTipoActividad(valor: String) = actualizarEntrada { it.copy(tipoActividad = valor) }

    fun alCambiarAdherenciaNutricional(valor: String) =
        actualizarEntrada { it.copy(adherenciaNutricional = valor.take(5)) }

    fun alCambiarAdherenciaEjercicio(valor: String) =
        actualizarEntrada { it.copy(adherenciaEjercicio = valor.take(5)) }

    /** Valida la entrada y pide la estimacion al backend. */
    fun calcular() {
        val actual = _estado.value
        val base = actual.base ?: return
        if (actual.calculando) return

        when (val construccion = ConstructorPeticionPrediccion.construir(base, actual.entrada)) {
            is ConstruccionPeticion.Incompleta -> _estado.update {
                it.copy(faltantes = construccion.faltantes, errores = construccion.errores, mensajeError = null)
            }

            is ConstruccionPeticion.Lista -> {
                _estado.update { it.copy(calculando = true, faltantes = emptyList(), errores = emptyMap(), mensajeError = null) }
                viewModelScope.launch {
                    when (val r = prediccionRepository.predecir(construccion.peticion)) {
                        is Resultado.Exito -> {
                            _estado.update { it.copy(calculando = false, prediccion = r.datos) }
                            cargarSeguimiento(r.datos.id)
                        }

                        is Resultado.Error -> _estado.update {
                            it.copy(
                                calculando = false,
                                mensajeError = r.errores.values.firstOrNull() ?: r.mensaje
                            )
                        }
                    }
                }
            }
        }
    }

    /** Vuelve a consultar la evolucion real de la ultima estimacion. */
    fun actualizarSeguimiento() {
        val id = _estado.value.prediccion?.id ?: _estado.value.seguimiento?.id ?: return
        cargarSeguimiento(id)
    }

    fun mensajesMostrados() = _estado.update { it.copy(mensajeError = null) }

    private fun cargarSeguimientoMasReciente() {
        viewModelScope.launch {
            // El historial llega ordenado del mas reciente al mas antiguo.
            val reciente = (prediccionRepository.historial() as? Resultado.Exito)?.datos?.firstOrNull() ?: return@launch
            cargarSeguimiento(reciente.id)
        }
    }

    private fun cargarSeguimiento(id: Long) {
        _estado.update { it.copy(cargandoSeguimiento = true) }
        viewModelScope.launch {
            when (val r = prediccionRepository.seguimiento(id)) {
                is Resultado.Exito -> _estado.update { it.copy(cargandoSeguimiento = false, seguimiento = r.datos) }
                is Resultado.Error -> _estado.update {
                    it.copy(cargandoSeguimiento = false, mensajeError = r.mensaje)
                }
            }
        }
    }

    private fun actualizarEntrada(cambio: (EntradaPrediccion) -> EntradaPrediccion) {
        _estado.update { it.copy(entrada = cambio(it.entrada), errores = emptyMap()) }
    }
}
