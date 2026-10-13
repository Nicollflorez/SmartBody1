package com.smartbody.presenter.register

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

data class RegisterUiState(
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val confirmacion: String = "",
    val errorNombre: String? = null,
    val errorEmail: String? = null,
    val errorPassword: String? = null,
    val errorConfirmacion: String? = null,
    val mensajeError: String? = null,
    val cargando: Boolean = false,
    val registroCompletado: Boolean = false
)

/** RF01 - Presenter del registro de usuarios. */
class RegisterPresenter(private val authRepository: AuthRepository) : ViewModel() {

    private val _estado = MutableStateFlow(RegisterUiState())
    val estado: StateFlow<RegisterUiState> = _estado.asStateFlow()

    fun alCambiarNombre(valor: String) {
        _estado.update { it.copy(nombre = valor, errorNombre = null, mensajeError = null) }
    }

    fun alCambiarEmail(valor: String) {
        _estado.update { it.copy(email = valor, errorEmail = null, mensajeError = null) }
    }

    fun alCambiarPassword(valor: String) {
        _estado.update { it.copy(password = valor, errorPassword = null, mensajeError = null) }
    }

    fun alCambiarConfirmacion(valor: String) {
        _estado.update { it.copy(confirmacion = valor, errorConfirmacion = null, mensajeError = null) }
    }

    fun registrar() {
        val actual = _estado.value
        if (actual.cargando) return

        val errorNombre = Validaciones.validarNombre(actual.nombre)
        val errorEmail = Validaciones.validarEmail(actual.email)
        val errorPassword = Validaciones.validarPassword(actual.password)
        val errorConfirmacion = Validaciones.validarConfirmacion(actual.password, actual.confirmacion)

        if (listOfNotNull(errorNombre, errorEmail, errorPassword, errorConfirmacion).isNotEmpty()) {
            _estado.update {
                it.copy(
                    errorNombre = errorNombre,
                    errorEmail = errorEmail,
                    errorPassword = errorPassword,
                    errorConfirmacion = errorConfirmacion
                )
            }
            return
        }

        _estado.update { it.copy(cargando = true, mensajeError = null) }
        viewModelScope.launch {
            val resultado = authRepository.registrar(
                nombre = actual.nombre.trim(),
                email = actual.email.trim(),
                password = actual.password
            )
            when (resultado) {
                is Resultado.Exito ->
                    _estado.update { it.copy(cargando = false, registroCompletado = true) }

                is Resultado.Error -> _estado.update {
                    it.copy(
                        cargando = false,
                        mensajeError = resultado.mensaje,
                        errorEmail = resultado.errores["email"] ?: it.errorEmail,
                        errorNombre = resultado.errores["nombre"] ?: it.errorNombre,
                        errorPassword = resultado.errores["password"] ?: it.errorPassword
                    )
                }
            }
        }
    }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensajeError = null) }
    }
}
