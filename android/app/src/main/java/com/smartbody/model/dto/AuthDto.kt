package com.smartbody.model.dto

/** Objetos que viajan entre la aplicacion y la API REST (RF01, RF02). */

data class RegistroRequest(
    val nombre: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val email: String,
    val perfilCompleto: Boolean = false,
    val esAdministrador: Boolean = false
)

data class AuthResponse(
    val token: String,
    val tipo: String,
    val expiraEnSegundos: Long,
    val usuario: UsuarioDto
)

data class MensajeResponse(
    val mensaje: String
)

/** Formato de error devuelto por el backend. */
data class ErrorResponse(
    val estado: Int? = null,
    val mensaje: String? = null,
    val errores: Map<String, String>? = null
)
