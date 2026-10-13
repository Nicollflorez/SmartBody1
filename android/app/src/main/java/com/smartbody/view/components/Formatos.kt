package com.smartbody.view.components

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formato de los datos para mostrarlos al usuario en espanol. */
object Formatos {

    private val LOCALE = Locale.forLanguageTag("es-CO")
    private val FORMATO_FECHA = DateTimeFormatter.ofPattern("d MMM yyyy", LOCALE)
    private val FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", LOCALE)

    fun decimal(valor: Double, decimales: Int = 1): String = String.format(LOCALE, "%.${decimales}f", valor)

    /** 75.0 -> "75"; 75.5 -> "75,5". */
    fun numero(valor: Double): String = if (valor % 1.0 == 0.0) valor.toInt().toString() else decimal(valor)

    fun entero(valor: Int): String = String.format(LOCALE, "%,d", valor)

    fun duracion(segundos: Int): String = when {
        segundos < 60 -> "$segundos s"
        segundos % 60 == 0 -> "${segundos / 60} min"
        else -> "${segundos / 60} min ${segundos % 60} s"
    }

    /** Series y repeticiones o tiempo de trabajo de un ejercicio. */
    fun prescripcion(series: Int, repeticiones: Int?, duracionSeg: Int?): String = when {
        repeticiones != null -> "$series x $repeticiones reps"
        duracionSeg != null && series == 1 -> duracion(duracionSeg)
        duracionSeg != null -> "$series x ${duracion(duracionSeg)}"
        else -> "$series series"
    }

    fun descanso(segundos: Int): String = if (segundos > 0) "Descanso ${duracion(segundos)}" else "Sin descanso"

    /** "2026-09-16T13:32:35" -> "16 sept 2026, 1:32 p. m.". */
    fun fechaHora(fechaHora: String?): String {
        if (fechaHora.isNullOrBlank()) return ""
        return try {
            LocalDateTime.parse(fechaHora).format(FORMATO_FECHA_HORA)
        } catch (e: Exception) {
            fechaHora.take(16).replace('T', ' ')
        }
    }

    fun fecha(fechaHora: String?): String {
        if (fechaHora.isNullOrBlank()) return ""
        return try {
            LocalDateTime.parse(fechaHora).format(FORMATO_FECHA)
        } catch (e: Exception) {
            fechaHora.take(10)
        }
    }

    /** Codigos del backend cuyo texto lleva tilde. */
    private val CON_TILDE = mapOf(
        "MAQUINA" to "Máquina",
        "LACTEO" to "Lácteo",
        "PROTEINA" to "Proteína"
    )

    /** "CUERPO_COMPLETO" -> "Cuerpo completo". */
    fun legible(codigo: String?): String {
        if (codigo.isNullOrBlank()) return ""
        CON_TILDE[codigo]?.let { return it }
        val texto = codigo.replace('_', ' ').lowercase(LOCALE)
        return texto.replaceFirstChar { it.titlecase(LOCALE) }
    }

    fun diasYMinutos(dias: Int?, minutos: Int?): String {
        if (dias == null || minutos == null) return ""
        val textoDias = if (dias == 1) "1 día" else "$dias días"
        return "$textoDias por semana - $minutos min"
    }
}
