package com.smartbody.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.smartbody.MainActivity
import com.smartbody.R
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * RF11 - notificacion del sistema para renovar la rutina. Es local: se programa con WorkManager
 * a partir de los dias que ya tiene la rutina, sin que el backend tenga que estar encendido.
 */
class NotificadorRutina(contexto: Context) {

    private val contexto = contexto.applicationContext
    private val preferencias = this.contexto.getSharedPreferences("smartbody_notificaciones", Context.MODE_PRIVATE)

    /** Se llama al iniciar la aplicacion; crear un canal existente no tiene efecto. */
    fun crearCanal() {
        val canal = NotificationChannel(CANAL, "Recordatorios", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Aviso para renovar tu rutina de entrenamiento"
        }
        contexto.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    /** Programa el aviso segun la rutina vigente; sin rutina no hay nada que recordar. */
    fun programar(diasDesdeRutina: Long?) = programar(diasDesdeRutina, ExistingWorkPolicy.REPLACE)

    /**
     * Llamado desde la propia tarea: con REPLACE WorkManager la cancelaria mientras se ejecuta,
     * por eso el siguiente recordatorio se encadena para cuando termine.
     */
    fun programarSiguienteRecordatorio() =
        programar(CalculoRecordatorio.DIAS_PARA_RENOVAR, ExistingWorkPolicy.APPEND_OR_REPLACE)

    private fun programar(diasDesdeRutina: Long?, politica: ExistingWorkPolicy) {
        if (diasDesdeRutina == null) {
            cancelar()
            return
        }
        val espera = CalculoRecordatorio.espera(LocalDateTime.now(), diasDesdeRutina)
        val tarea = OneTimeWorkRequestBuilder<RecordatorioRutinaWorker>()
            .setInitialDelay(espera.toMillis(), TimeUnit.MILLISECONDS)
            .build()
        // Tarea unica: solo existe un aviso pendiente y siempre corresponde a la rutina mas reciente.
        WorkManager.getInstance(contexto).enqueueUniqueWork(TAREA, politica, tarea)
    }

    /** RF12 / RNF06 - al cambiar de usuario no deben quedar avisos de la sesion anterior. */
    fun cancelar() {
        WorkManager.getInstance(contexto).cancelUniqueWork(TAREA)
        NotificationManagerCompat.from(contexto).cancel(ID_NOTIFICACION)
    }

    fun mostrar() {
        if (!tienePermiso()) return
        // Abre la aplicacion desde cero: el splash decide entre el login y el inicio con la tarjeta de renovacion.
        val intent = Intent(contexto, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val alPulsar = PendingIntent.getActivity(
            contexto, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificacion = NotificationCompat.Builder(contexto, CANAL)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("Es momento de renovar tu rutina")
            .setContentText("Tu rutina ya tiene 4 semanas. Un plan nuevo te ayuda a seguir progresando.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(alPulsar)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(contexto).notify(ID_NOTIFICACION, notificacion)
        } catch (_: SecurityException) {
            // El usuario retiro el permiso justo antes de mostrarla.
        }
    }

    /** Desde Android 13 el permiso se pide una sola vez; si lo rechaza, la app sigue funcionando. */
    fun debeSolicitarPermiso(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !tienePermiso() &&
            !preferencias.getBoolean(CLAVE_PERMISO_SOLICITADO, false)

    fun marcarPermisoSolicitado() {
        preferencias.edit().putBoolean(CLAVE_PERMISO_SOLICITADO, true).apply()
    }

    private fun tienePermiso(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    companion object {
        private const val CANAL = "recordatorios"
        private const val TAREA = "recordatorio_renovar_rutina"
        private const val ID_NOTIFICACION = 1
        private const val CLAVE_PERMISO_SOLICITADO = "permiso_solicitado"
    }
}
