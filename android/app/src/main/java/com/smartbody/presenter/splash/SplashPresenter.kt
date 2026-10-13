package com.smartbody.presenter.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbody.model.repository.AuthRepository
import com.smartbody.model.repository.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Destino inicial de la aplicacion segun exista o no una sesion valida. */
enum class DestinoInicial { LOGIN, DASHBOARD }

/**
 * Comprueba al arrancar si hay un JWT guardado y si sigue siendo valido.
 * Un token expirado o invalido se elimina y el usuario vuelve al login (RF12, RNF06).
 */
class SplashPresenter(private val authRepository: AuthRepository) : ViewModel() {

    private val _destino = MutableStateFlow<DestinoInicial?>(null)
    val destino: StateFlow<DestinoInicial?> = _destino.asStateFlow()

    init {
        verificarSesion()
    }

    private fun verificarSesion() {
        viewModelScope.launch {
            if (!authRepository.haySesionActiva()) {
                _destino.value = DestinoInicial.LOGIN
                return@launch
            }

            when (val resultado = authRepository.usuarioActual()) {
                is Resultado.Exito -> _destino.value = DestinoInicial.DASHBOARD
                is Resultado.Error -> {
                    if (resultado.codigo == 401 || resultado.codigo == 403) {
                        authRepository.cerrarSesion()
                        _destino.value = DestinoInicial.LOGIN
                    } else {
                        // Sin conexion: se conserva la sesion y se continua dentro de la aplicacion.
                        _destino.value = DestinoInicial.DASHBOARD
                    }
                }
            }
        }
    }
}
