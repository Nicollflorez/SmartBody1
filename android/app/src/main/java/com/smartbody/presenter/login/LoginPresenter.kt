package com.smartbody.presenter.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbody.model.repository.AuthRepository
import com.smartbody.model.repository.Resultado
import com.smartbody.presenter.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado que el Presenter entrega a la View: describe exactamente que debe mostrarse. */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorEmail: String? = null,
    val errorPassword: String? = null,
    val mensajeError: String? = null,
    val cargando: Boolean = false,
    val sesionIniciada: Boolean = false
)

/** RF02 - Presenter del inicio de sesion. */
class LoginPresenter(private val authRepository: AuthRepository) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun alCambiarEmail(valor: String) {
        _estado.update { it.copy(email = valor, errorEmail = null, mensajeError = null) }
    }

    fun alCambiarPassword(valor: String) {
        _estado.update { it.copy(password = valor, errorPassword = null, mensajeError = null) }
    }

    fun iniciarSesion() {
        val actual = _estado.value
        if (actual.cargando) return

        val errorEmail = Validaciones.validarEmail(actual.email)
        val errorPassword = Validaciones.validarPasswordLogin(actual.password)
        if (errorEmail != null || errorPassword != null) {
            _estado.update { it.copy(errorEmail = errorEmail, errorPassword = errorPassword) }
            return
        }

        _estado.update { it.copy(cargando = true, mensajeError = null) }
        viewModelScope.launch {
            when (val resultado = authRepository.iniciarSesion(actual.email.trim(), actual.password)) {
                is Resultado.Exito ->
                    _estado.update { it.copy(cargando = false, sesionIniciada = true) }

                is Resultado.Error ->
                    _estado.update { it.copy(cargando = false, mensajeError = resultado.mensaje) }
            }
        }
    }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensajeError = null) }
    }
}
