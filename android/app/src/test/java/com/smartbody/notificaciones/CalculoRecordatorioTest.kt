package com.smartbody.notificaciones

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

/** Pruebas del momento en que se avisa que la rutina debe renovarse (RF11). */
class CalculoRecordatorioTest {

    private val ahora = LocalDateTime.of(2026, 9, 16, 20, 30)

    @Test
    fun `una rutina nueva se recuerda a los 28 dias a las 9 de la manana`() {
        // 16 sep 20:30 + 28 dias = 14 oct 20:30, ya pasadas las 9: se avisa el 15 oct.
        assertEquals(LocalDateTime.of(2026, 10, 15, 9, 0), CalculoRecordatorio.momentoAviso(ahora, 0))
    }

    @Test
    fun `descuenta los dias que ya tiene la rutina`() {
        val temprano = LocalDateTime.of(2026, 9, 16, 7, 0)
        assertEquals(LocalDateTime.of(2026, 9, 26, 9, 0), CalculoRecordatorio.momentoAviso(temprano, 18))
    }

    @Test
    fun `nunca avisa antes de que la rutina cumpla 28 dias`() {
        for (dias in 0L until CalculoRecordatorio.DIAS_PARA_RENOVAR) {
            val espera = CalculoRecordatorio.espera(ahora, dias)
            assertTrue(espera >= Duration.ofDays(CalculoRecordatorio.DIAS_PARA_RENOVAR - dias))
        }
    }

    @Test
    fun `si la rutina ya vencio se recuerda de nuevo en una semana`() {
        assertEquals(LocalDateTime.of(2026, 9, 24, 9, 0), CalculoRecordatorio.momentoAviso(ahora, 28))
        assertEquals(LocalDateTime.of(2026, 9, 24, 9, 0), CalculoRecordatorio.momentoAviso(ahora, 45))
    }
}
