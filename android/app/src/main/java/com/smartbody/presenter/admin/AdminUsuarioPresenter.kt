package com.smartbody.presenter.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbody.model.dto.DetalleUsuarioAdminDto
import com.smartbody.model.repository.AdminRepository
import com.smartbody.model.repository.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUsuarioUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val detalle: DetalleUsuarioAdminDto? = null,
    val diaSeleccionado: Int = 1,
    val confirmandoGeneracion: Boolean = false,
    val generando: Boolean = false,
    val actualizandoEstado: Boolean = false,
    val mensaje: String? = null
)

/** Detalle de un usuario para el administrador: perfil, progreso, rutina y plan nutricional. */
class AdminUsuarioPresenter(
    private val adminRepository: AdminRepository,
    private val usuarioId: Long
) : ViewModel() {

    private val _estado = MutableStateFlow(AdminUsuarioUiState())
    val estado: StateFlow<AdminUsuarioUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        _estado.update { it.copy(cargando = it.detalle == null, errorCarga = null) }
        viewModelScope.launch {
            when (val resultado = adminRepository.detalleUsuario(usuarioId)) {
                is Resultado.Exito -> _estado.update { it.copy(cargando = false, detalle = resultado.datos) }
                is Resultado.Error -> _estado.update { it.copy(cargando = false, errorCarga = resultado.mensaje) }
            }
        }
    }

    fun seleccionarDia(numero: Int) {
        _estado.update { it.copy(diaSeleccionado = numero) }
    }

    fun pedirGenerarPlan() {
        _estado.update { it.copy(confirmandoGeneracion = true) }
    }

    fun cancelarGeneracion() {
        _estado.update { it.copy(confirmandoGeneracion = false) }
    }

    /** Genera una rutina y un plan nutricional nuevos con el perfil actual del usuario. */
    fun generarPlan() {
        if (_estado.value.generando) return
        _estado.update { it.copy(confirmandoGeneracion = false, generando = true) }
        viewModelScope.launch {
            when (val resultado = adminRepository.generarPlan(usuarioId)) {
                is Resultado.Exito -> _estado.update {
                    it.copy(
                        generando = false,
                        detalle = resultado.datos,
                        diaSeleccionado = 1,
                        mensaje = "Nueva rutina y plan nutricional generados para ${resultado.datos.usuario.nombre}."
                    )
                }

                is Resultado.Error -> _estado.update { it.copy(generando = false, mensaje = resultado.mensaje) }
            }
        }
    }

    fun cambiarEstado(activo: Boolean) {
        if (_estado.value.actualizandoEstado) return
        _estado.update { it.copy(actualizandoEstado = true) }
        viewModelScope.launch {
            when (val resultado = adminRepository.cambiarEstado(usuarioId, activo)) {
                is Resultado.Exito -> {
                    _estado.update {
                        it.copy(
                            actualizandoEstado = false,
                            detalle = it.detalle?.let { d -> d.copy(usuario = d.usuario.copy(activo = activo)) },
                            mensaje = resultado.datos.mensaje
                        )
                    }
                }

                is Resultado.Error -> _estado.update { it.copy(actualizandoEstado = false, mensaje = resultado.mensaje) }
            }
        }
    }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensaje = null) }
    }
}
