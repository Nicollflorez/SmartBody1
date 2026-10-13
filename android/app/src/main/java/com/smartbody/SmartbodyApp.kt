package com.smartbody

import android.app.Application
import android.content.Context

class SmartbodyApp : Application() {

    val contenedor: Contenedor by lazy { Contenedor(this) }

    override fun onCreate() {
        super.onCreate()
        // RF11: el canal debe existir antes de publicar cualquier notificacion.
        contenedor.notificadorRutina.crearCanal()
    }
}

/** Acceso al contenedor desde cualquier pantalla Compose. */
val Context.contenedor: Contenedor
    get() = (applicationContext as SmartbodyApp).contenedor
