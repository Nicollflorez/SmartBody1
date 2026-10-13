package com.smartbody.presenter.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbody.model.dto.PerfilDto
import com.smartbody.model.repository.AuthRepository
import com.smartbody.model.repository.PerfilRepository
import com.smartbody.model.repository.Resultado
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilUiState(
    val cargando: Boolean = true,
    val nombre: String = "",
    val email: String = "",
    val esAdministrador: Boolean = false,
    val perfil: PerfilDto? = null,
    val foto: ByteArray? = null,
    val procesandoFoto: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null,
    val confirmandoCierre: Boolean = false,
    val sesionCerrada: Boolean = false
)

/** Presenter de la pestana de perfil: datos, foto de perfil y cierre de sesion (RF12). */
class PerfilPresenter(
    private val perfilRepository: PerfilRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(PerfilUiState())
    val estado: StateFlow<PerfilUiState> = _estado.asStateFlow()

    fun cargar() {
        _estado.update { it.copy(cargando = it.perfil == null, mensajeError = null) }
        viewModelScope.launch {
            val usuario = async { authRepository.usuarioActual() }
            val perfil = async { perfilRepository.perfil() }
            val foto = async { perfilRepository.foto() }

            when (val u = usuario.await()) {
                is Resultado.Exito -> _estado.update {
                    it.copy(nombre = u.datos.nombre, email = u.datos.email, esAdministrador = u.datos.esAdministrador)
                }
                is Resultado.Error -> _estado.update { it.copy(nombre = authRepository.nombreDeSesion().orEmpty()) }
            }
            (foto.await() as? Resultado.Exito<ByteArray?>)?.let { f -> _estado.update { it.copy(foto = f.datos) } }
            when (val p = perfil.await()) {
                is Resultado.Exito -> _estado.update { it.copy(cargando = false, perfil = p.datos) }
                is Resultado.Error -> _estado.update { it.copy(cargando = false, mensajeError = p.mensaje) }
            }
        }
    }

    /** Recibe la imagen elegida en el selector de fotos del sistema. */
    fun alSeleccionarFoto(uri: Uri) {
        if (_estado.value.procesandoFoto) return
        _estado.update { it.copy(procesandoFoto = true, mensajeError = null) }
        viewModelScope.launch {
            when (val resultado = perfilRepository.subirFoto(uri)) {
                is Resultado.Exito -> _estado.update {
                    it.copy(procesandoFoto = false, foto = resultado.datos, mensajeExito = "Foto de perfil actualizada.")
                }

                is Resultado.Error -> _estado.update { it.copy(procesandoFoto = false, mensajeError = resultado.mensaje) }
            }
        }
    }

    fun quitarFoto() {
        if (_estado.value.procesandoFoto) return
        _estado.update { it.copy(procesandoFoto = true, mensajeError = null) }
        viewModelScope.launch {
            when (val resultado = perfilRepository.eliminarFoto()) {
                is Resultado.Exito -> _estado.update {
                    it.copy(procesandoFoto = false, foto = null, mensajeExito = "Foto de perfil eliminada.")
                }

                is Resultado.Error -> _estado.update { it.copy(procesandoFoto = false, mensajeError = resultado.mensaje) }
            }
        }
    }

    fun pedirCerrarSesion() {
        _estado.update { it.copy(confirmandoCierre = true) }
    }

    fun cancelarCierre() {
        _estado.update { it.copy(confirmandoCierre = false) }
    }

    /** RF12 - elimina el JWT local y vuelve al login. */
    fun cerrarSesion() {
        viewModelScope.launch {
            authRepository.cerrarSesion()
            _estado.update { it.copy(confirmandoCierre = false, sesionCerrada = true) }
        }
    }

    fun mensajesMostrados() {
        _estado.update { it.copy(mensajeError = null, mensajeExito = null) }
    }
}
