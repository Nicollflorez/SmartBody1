package com.smartbody.presenter.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbody.model.dto.ObjetivoDto
import com.smartbody.model.dto.PerfilDto
import com.smartbody.model.dto.PerfilRequest
import com.smartbody.model.dto.RestriccionDto
import com.smartbody.model.repository.PerfilRepository
import com.smartbody.model.repository.Resultado
import com.smartbody.presenter.Validaciones
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilFormUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val paso: Int = 0,
    // Paso 1 - datos personales (RF03)
    val edad: String = "",
    val sexo: String? = null,
    val peso: String = "",
    val altura: String = "",
    // Paso 2 - objetivo (RF04)
    val objetivos: List<ObjetivoDto> = emptyList(),
    val objetivoId: Long? = null,
    // Paso 3 - disponibilidad
    val nivel: String? = null,
    val dias: Int = 3,
    val minutos: Int = 45,
    // Paso 4 - restricciones (RF05)
    val restricciones: List<RestriccionDto> = emptyList(),
    val restriccionesSeleccionadas: Set<Long> = emptySet(),
    // Errores de validacion
    val errorEdad: String? = null,
    val errorSexo: String? = null,
    val errorPeso: String? = null,
    val errorAltura: String? = null,
    val errorObjetivo: String? = null,
    val errorNivel: String? = null,
    val guardando: Boolean = false,
    val mensajeError: String? = null,
    val guardado: Boolean = false
) {
    val esUltimoPaso: Boolean get() = paso == PerfilFormPresenter.TOTAL_PASOS - 1

    /** IMC de referencia mientras el usuario escribe su peso y altura. */
    val imcEstimado: Double?
        get() {
            val pesoKg = Validaciones.leerDecimal(peso) ?: return null
            val alturaCm = Validaciones.leerDecimal(altura)?.takeIf { it >= 100 } ?: return null
            return pesoKg / ((alturaCm / 100) * (alturaCm / 100))
        }
}

/** Presenter del asistente de perfil en cuatro pasos: datos, objetivo, disponibilidad y restricciones. */
class PerfilFormPresenter(private val perfilRepository: PerfilRepository) : ViewModel() {

    companion object {
        const val TOTAL_PASOS = 4
        val SEXOS = listOf("MASCULINO", "FEMENINO", "OTRO")
        val NIVELES = listOf("PRINCIPIANTE", "INTERMEDIO", "AVANZADO")
        val OPCIONES_MINUTOS = listOf(20, 30, 45, 60, 75, 90, 120)
    }

    private val _estado = MutableStateFlow(PerfilFormUiState())
    val estado: StateFlow<PerfilFormUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        _estado.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            val objetivos = async { perfilRepository.objetivos() }
            val restricciones = async { perfilRepository.restricciones() }
            val perfil = async { perfilRepository.perfil() }

            val listaObjetivos = when (val r = objetivos.await()) {
                is Resultado.Exito -> r.datos
                is Resultado.Error -> return@launch errorDeCarga(r.mensaje)
            }
            val listaRestricciones = when (val r = restricciones.await()) {
                is Resultado.Exito -> r.datos
                is Resultado.Error -> return@launch errorDeCarga(r.mensaje)
            }
            val actual = (perfil.await() as? Resultado.Exito<PerfilDto>)?.datos

            _estado.update {
                it.copy(
                    cargando = false,
                    objetivos = listaObjetivos,
                    restricciones = listaRestricciones,
                    edad = actual?.edad?.toString().orEmpty(),
                    sexo = actual?.sexo,
                    peso = actual?.pesoKg?.let(::numeroParaCampo).orEmpty(),
                    altura = actual?.alturaCm?.let(::numeroParaCampo).orEmpty(),
                    objetivoId = actual?.objetivo?.id,
                    nivel = actual?.nivelExperiencia,
                    dias = actual?.diasPorSemana ?: it.dias,
                    minutos = actual?.minutosPorSesion ?: it.minutos,
                    restriccionesSeleccionadas = actual?.restricciones?.map { r -> r.id }?.toSet().orEmpty()
                )
            }
        }
    }

    private fun errorDeCarga(mensaje: String) {
        _estado.update { it.copy(cargando = false, errorCarga = mensaje) }
    }

    fun alCambiarEdad(valor: String) {
        _estado.update { it.copy(edad = valor.filter(Char::isDigit).take(3), errorEdad = null) }
    }

    fun alSeleccionarSexo(valor: String) {
        _estado.update { it.copy(sexo = valor, errorSexo = null) }
    }

    fun alCambiarPeso(valor: String) {
        _estado.update { it.copy(peso = valor.take(6), errorPeso = null) }
    }

    fun alCambiarAltura(valor: String) {
        _estado.update { it.copy(altura = valor.take(6), errorAltura = null) }
    }

    fun alSeleccionarObjetivo(id: Long) {
        _estado.update { it.copy(objetivoId = id, errorObjetivo = null) }
    }

    fun alSeleccionarNivel(valor: String) {
        _estado.update { it.copy(nivel = valor, errorNivel = null) }
    }

    fun alCambiarDias(valor: Int) {
        _estado.update { it.copy(dias = valor.coerceIn(1, 7)) }
    }

    fun alCambiarMinutos(valor: Int) {
        _estado.update { it.copy(minutos = valor) }
    }

    fun alAlternarRestriccion(id: Long) {
        _estado.update {
            val seleccion = it.restriccionesSeleccionadas
            it.copy(restriccionesSeleccionadas = if (id in seleccion) seleccion - id else seleccion + id)
        }
    }

    fun siguiente() {
        val actual = _estado.value
        if (actual.guardando || !validarPaso(actual.paso)) return
        if (actual.esUltimoPaso) guardar() else _estado.update { it.copy(paso = it.paso + 1) }
    }

    fun anterior() {
        _estado.update { it.copy(paso = (it.paso - 1).coerceAtLeast(0)) }
    }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensajeError = null) }
    }

    private fun validarPaso(paso: Int): Boolean {
        val actual = _estado.value
        return when (paso) {
            0 -> {
                val errorEdad = Validaciones.validarEdad(actual.edad)
                val errorSexo = if (actual.sexo == null) "Selecciona una opción" else null
                val errorPeso = Validaciones.validarPeso(actual.peso)
                val errorAltura = Validaciones.validarAltura(actual.altura)
                _estado.update {
                    it.copy(errorEdad = errorEdad, errorSexo = errorSexo, errorPeso = errorPeso, errorAltura = errorAltura)
                }
                listOf(errorEdad, errorSexo, errorPeso, errorAltura).all { it == null }
            }

            1 -> {
                val error = if (actual.objetivoId == null) "Selecciona tu objetivo principal" else null
                _estado.update { it.copy(errorObjetivo = error) }
                error == null
            }

            2 -> {
                val error = if (actual.nivel == null) "Selecciona tu nivel de experiencia" else null
                _estado.update { it.copy(errorNivel = error) }
                error == null
            }

            else -> true
        }
    }

    private fun guardar() {
        val actual = _estado.value
        val peticion = PerfilRequest(
            edad = actual.edad.trim().toInt(),
            sexo = actual.sexo ?: return,
            pesoKg = Validaciones.leerDecimal(actual.peso) ?: return,
            alturaCm = Validaciones.leerDecimal(actual.altura) ?: return,
            nivelExperiencia = actual.nivel ?: return,
            minutosPorSesion = actual.minutos,
            diasPorSemana = actual.dias,
            objetivoId = actual.objetivoId ?: return,
            restriccionIds = actual.restriccionesSeleccionadas.toList()
        )

        _estado.update { it.copy(guardando = true, mensajeError = null) }
        viewModelScope.launch {
            when (val resultado = perfilRepository.guardarPerfil(peticion)) {
                is Resultado.Exito -> _estado.update { it.copy(guardando = false, guardado = true) }
                is Resultado.Error -> _estado.update {
                    val detalle = resultado.errores.values.firstOrNull()
                    it.copy(guardando = false, mensajeError = detalle ?: resultado.mensaje)
                }
            }
        }
    }

    private fun numeroParaCampo(valor: Double): String =
        if (valor % 1.0 == 0.0) valor.toInt().toString() else valor.toString()
}
