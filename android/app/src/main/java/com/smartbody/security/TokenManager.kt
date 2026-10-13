package com.smartbody.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.sesionDataStore: DataStore<Preferences> by preferencesDataStore(name = "smartbody_sesion")

/**
 * Guarda el JWT y los datos basicos de la sesion en DataStore, fuera del codigo fuente
 * y del almacenamiento compartido de la aplicacion (RNF02).
 */
class TokenManager(private val context: Context) {

    private val claveToken = stringPreferencesKey("jwt")
    private val claveNombre = stringPreferencesKey("nombre")
    private val claveEmail = stringPreferencesKey("email")

    @Volatile
    private var token: String? = null

    @Volatile
    private var cargado: Boolean = false

    suspend fun guardarSesion(token: String, nombre: String, email: String) {
        context.sesionDataStore.edit { preferencias ->
            preferencias[claveToken] = token
            preferencias[claveNombre] = nombre
            preferencias[claveEmail] = email
        }
        this.token = token
        this.cargado = true
    }

    private val _sesionExpirada = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Emite cuando el backend rechaza el token (expirado o invalido) para volver al login. */
    val sesionExpirada: SharedFlow<Unit> = _sesionExpirada.asSharedFlow()

    /** RF12 - al cerrar sesion se elimina el token y los datos temporales. */
    suspend fun cerrarSesion() {
        context.sesionDataStore.edit { it.clear() }
        token = null
        cargado = true
    }

    /** Llamado por el interceptor ante un 401 en una peticion protegida. */
    fun invalidarSesion() {
        runBlocking { cerrarSesion() }
        _sesionExpirada.tryEmit(Unit)
    }

    suspend fun restaurarSesion(): String? {
        token = context.sesionDataStore.data.first()[claveToken]
        cargado = true
        return token
    }

    suspend fun nombreGuardado(): String? = context.sesionDataStore.data.first()[claveNombre]

    suspend fun emailGuardado(): String? = context.sesionDataStore.data.first()[claveEmail]

    suspend fun haySesion(): Boolean = !restaurarSesion().isNullOrBlank()

    /** Lectura sincrona usada por el interceptor de OkHttp. */
    fun tokenActual(): String? {
        if (!cargado) {
            runBlocking { restaurarSesion() }
        }
        return token
    }
}
