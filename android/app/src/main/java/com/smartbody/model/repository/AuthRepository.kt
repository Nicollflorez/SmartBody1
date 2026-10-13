package com.smartbody.model.repository

import com.smartbody.model.dto.LoginRequest
import com.smartbody.model.dto.RegistroRequest
import com.smartbody.model.dto.UsuarioDto
import com.smartbody.network.ApiService
import com.smartbody.security.TokenManager

/**
 * Acceso a los endpoints de autenticacion y custodia del JWT.
 * [alCambiarSesion] limpia los datos locales de la sesion anterior (por ejemplo, el chat).
 */
class AuthRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager,
    private val alCambiarSesion: () -> Unit = {}
) {

    suspend fun registrar(nombre: String, email: String, password: String): Resultado<UsuarioDto> {
        val resultado = ejecutarPeticion { api.registrar(RegistroRequest(nombre, email, password)) }
        return guardarSesionSiCorresponde(resultado)
    }

    suspend fun iniciarSesion(email: String, password: String): Resultado<UsuarioDto> {
        val resultado = ejecutarPeticion { api.login(LoginRequest(email, password)) }
        return guardarSesionSiCorresponde(resultado)
    }

    suspend fun usuarioActual(): Resultado<UsuarioDto> = ejecutarPeticion { api.usuarioActual() }

    /** RF12 - avisa al backend y elimina siempre el token local. */
    suspend fun cerrarSesion(): Resultado<Unit> {
        ejecutarPeticion { api.logout() }
        tokenManager.cerrarSesion()
        alCambiarSesion()
        return Resultado.Exito(Unit)
    }

    suspend fun haySesionActiva(): Boolean = tokenManager.haySesion()

    suspend fun nombreDeSesion(): String? = tokenManager.nombreGuardado()

    private suspend fun guardarSesionSiCorresponde(
        resultado: Resultado<com.smartbody.model.dto.AuthResponse>
    ): Resultado<UsuarioDto> = when (resultado) {
        is Resultado.Exito -> {
            val datos = resultado.datos
            tokenManager.guardarSesion(datos.token, datos.usuario.nombre, datos.usuario.email)
            alCambiarSesion()
            Resultado.Exito(datos.usuario)
        }

        is Resultado.Error -> resultado
    }
}
