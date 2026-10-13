package com.smartbody.network

import com.smartbody.security.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Anade automaticamente el encabezado {@code Authorization: Bearer <token>}
 * a todas las peticiones que no sean publicas.
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    private val rutasPublicas = listOf("/api/auth/login", "/api/auth/register", "/api/ping")

    override fun intercept(chain: Interceptor.Chain): Response {
        val peticion = chain.request()
        val ruta = peticion.url.encodedPath

        if (rutasPublicas.any { ruta.endsWith(it) }) {
            return chain.proceed(peticion)
        }

        val token = tokenManager.tokenActual()
        val peticionFinal = if (token.isNullOrBlank()) {
            peticion
        } else {
            peticion.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        val respuesta = chain.proceed(peticionFinal)

        // Token expirado o invalido: se elimina la sesion local y la aplicacion vuelve al login.
        if (respuesta.code == 401 && !token.isNullOrBlank()) {
            tokenManager.invalidarSesion()
        }
        return respuesta
    }
}
