package com.smartbody.presenter.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbody.model.dto.ResumenDto
import com.smartbody.model.repository.PerfilRepository
import com.smartbody.model.repository.Resultado
import com.smartbody.notificaciones.NotificadorRutina
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val cargando: Boolean = true,
    val resumen: ResumenDto? = null,
    val foto: ByteArray? = null,
    val mensajeError: String? = null
) {
    /** Solo tiene sentido pedir el permiso de notificaciones cuando hay una rutina que recordar. */
    val tieneRutina: Boolean get() = resumen?.rutinaActivaId != null
}

/** Presenter de la pantalla de inicio: perfil, plan vigente y aviso de renovacion de rutina (RF11). */
class DashboardPresenter(
    private val perfilRepository: PerfilRepository,
    private val notificadorRutina: NotificadorRutina
) : ViewModel() {

    private val _estado = MutableStateFlow(DashboardUiState())
    val estado: StateFlow<DashboardUiState> = _estado.asStateFlow()

    fun cargar() {
        _estado.update { it.copy(cargando = it.resumen == null, mensajeError = null) }
        viewModelScope.launch {
            val foto = async { perfilRepository.foto() }
            when (val resultado = perfilRepository.resumen()) {
                is Resultado.Exito -> {
                    _estado.update { it.copy(cargando = false, resumen = resultado.datos) }
                    // RF11: la notificacion se sincroniza con la rutina vigente cada vez que se abre el inicio.
                    notificadorRutina.programar(resultado.datos.diasDesdeRutina)
                }
                is Resultado.Error -> _estado.update { it.copy(cargando = false, mensajeError = resultado.mensaje) }
            }
            // La foto es opcional: si falla, se muestra el logo.
            (foto.await() as? Resultado.Exito<ByteArray?>)?.let { f -> _estado.update { it.copy(foto = f.datos) } }
        }
    }

    fun debeSolicitarPermisoNotificaciones(): Boolean = notificadorRutina.debeSolicitarPermiso()

    fun permisoNotificacionesSolicitado() {
        notificadorRutina.marcarPermisoSolicitado()
    }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensajeError = null) }
    }
}
