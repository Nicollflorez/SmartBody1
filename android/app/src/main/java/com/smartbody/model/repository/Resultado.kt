package com.smartbody.model.repository

/** Resultado de una operacion contra la API: exito con datos o error con mensaje para el usuario. */
sealed interface Resultado<out T> {

    data class Exito<T>(val datos: T) : Resultado<T>

    data class Error(
        val mensaje: String,
        val codigo: Int? = null,
        val errores: Map<String, String> = emptyMap()
    ) : Resultado<Nothing>

    val esExito: Boolean get() = this is Exito
}
