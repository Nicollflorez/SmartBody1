package com.smartbody.notificaciones

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smartbody.contenedor

/** Tarea en segundo plano que muestra el aviso de RF11 cuando la rutina cumple 28 dias. */
class RecordatorioRutinaWorker(contexto: Context, parametros: WorkerParameters) :
    CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val contenedor = applicationContext.contenedor
        // Sin sesion (cerro sesion o expiro) no se avisa: los datos pertenecen a otro usuario.
        if (!contenedor.tokenManager.haySesion()) return Result.success()

        val notificador = contenedor.notificadorRutina
        notificador.mostrar()
        // Si no renueva la rutina se le recuerda de nuevo; al abrir el inicio o generar un plan se reprograma.
        notificador.programarSiguienteRecordatorio()
        return Result.success()
    }
}
