package com.smartbody.view.components

import org.junit.Assert.assertEquals
import org.junit.Test

/** Pruebas del formato con el que se muestran rutinas y medidas. */
class FormatosTest {

    @Test
    fun `muestra series y repeticiones`() {
        assertEquals("3 x 15 reps", Formatos.prescripcion(3, 15, null))
    }

    @Test
    fun `muestra el tiempo de ejercicios continuos e isometricos`() {
        assertEquals("16 min", Formatos.prescripcion(1, null, 960))
        assertEquals("3 x 30 s", Formatos.prescripcion(3, null, 30))
    }

    @Test
    fun `formatea duraciones`() {
        assertEquals("45 s", Formatos.duracion(45))
        assertEquals("3 min", Formatos.duracion(180))
        assertEquals("1 min 30 s", Formatos.duracion(90))
        assertEquals("Sin descanso", Formatos.descanso(0))
    }

    @Test
    fun `convierte codigos en texto legible`() {
        assertEquals("Cuerpo completo", Formatos.legible("CUERPO_COMPLETO"))
        assertEquals("Piernas", Formatos.legible("PIERNAS"))
    }

    @Test
    fun `muestra numeros enteros sin decimales`() {
        assertEquals("75", Formatos.numero(75.0))
        assertEquals("75,5", Formatos.numero(75.5))
    }

    @Test
    fun `formatea la fecha de registro`() {
        assertEquals(true, Formatos.fecha("2026-09-16T13:32:35.707838").startsWith("16 "))
        assertEquals(true, Formatos.fecha("2026-09-16T13:32:35.707838").endsWith("2026"))
        // Si el formato no es reconocido se muestra la parte de la fecha tal como llega.
        assertEquals("2026-09-16", Formatos.fecha("2026-09-16 13:32"))
        assertEquals("", Formatos.fecha(null))
    }
}
