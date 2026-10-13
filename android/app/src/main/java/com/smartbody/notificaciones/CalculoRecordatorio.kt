package com.smartbody.notificaciones

import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * RF11 - decide cuando avisar que la rutina debe renovarse. No depende de Android para poder probarse.
 */
object CalculoRecordatorio {

    /** Mismo valor que DIAS_PARA_RENOVAR_RUTINA en el backend (PerfilService). */
    const val DIAS_PARA_RENOVAR = 28L

    /** Si el usuario no renueva la rutina, se le vuelve a recordar tras estos dias. */
    const val DIAS_ENTRE_RECORDATORIOS = 7L

    /** Hora del dia a la que se muestra el aviso, para no molestar de madrugada. */
    val HORA_AVISO: LocalTime = LocalTime.of(9, 0)

    /**
     * Momento del proximo aviso. [diasDesdeRutina] llega redondeado hacia abajo por el backend,
     * por eso se suma el tiempo restante desde [ahora]: al llegar el aviso la rutina ya tiene
     * al menos 28 dias y la pantalla de inicio muestra la tarjeta de renovacion.
     */
    fun momentoAviso(ahora: LocalDateTime, diasDesdeRutina: Long): LocalDateTime {
        val restantes = DIAS_PARA_RENOVAR - diasDesdeRutina
        val dias = if (restantes > 0) restantes else DIAS_ENTRE_RECORDATORIOS
        return siguienteHoraAviso(ahora.plusDays(dias))
    }

    /** Espera desde [ahora] hasta el proximo aviso. */
    fun espera(ahora: LocalDateTime, diasDesdeRutina: Long): Duration =
        Duration.between(ahora, momentoAviso(ahora, diasDesdeRutina))

    /** Primera [HORA_AVISO] igual o posterior a [momento]. */
    private fun siguienteHoraAviso(momento: LocalDateTime): LocalDateTime {
        val mismoDia = momento.toLocalDate().atTime(HORA_AVISO)
        return if (mismoDia.isBefore(momento)) mismoDia.plusDays(1) else mismoDia
    }
}
