package com.smartbody

import android.content.Context
import com.smartbody.model.repository.AdminRepository
import com.smartbody.model.repository.AuthRepository
import com.smartbody.model.repository.ChatRepository
import com.smartbody.model.repository.NutricionRepository
import com.smartbody.model.repository.PerfilRepository
import com.smartbody.model.repository.ProcesadorImagen
import com.smartbody.model.repository.ProgresoRepository
import com.smartbody.model.repository.RutinaRepository
import com.smartbody.network.ApiService
import com.smartbody.notificaciones.NotificadorRutina
import com.smartbody.network.RetrofitClient
import com.smartbody.security.TokenManager

/**
 * Contenedor simple de dependencias. Evita librerias de inyeccion adicionales
 * manteniendo una unica instancia compartida de la capa de red y de los repositorios.
 */
class Contenedor(contexto: Context) {

    val tokenManager: TokenManager by lazy { TokenManager(contexto.applicationContext) }

    val api: ApiService by lazy { RetrofitClient.crear(tokenManager) }

    val authRepository: AuthRepository by lazy {
        AuthRepository(api, tokenManager, alCambiarSesion = {
            chatRepository.limpiar()
            notificadorRutina.cancelar()
        })
    }

    val notificadorRutina: NotificadorRutina by lazy { NotificadorRutina(contexto.applicationContext) }

    val chatRepository: ChatRepository by lazy { ChatRepository(api) }

    val adminRepository: AdminRepository by lazy { AdminRepository(api) }

    val perfilRepository: PerfilRepository by lazy {
        PerfilRepository(api, ProcesadorImagen(contexto.applicationContext.contentResolver))
    }

    val rutinaRepository: RutinaRepository by lazy { RutinaRepository(api) }

    val nutricionRepository: NutricionRepository by lazy { NutricionRepository(api) }

    val progresoRepository: ProgresoRepository by lazy { ProgresoRepository(api) }
}
