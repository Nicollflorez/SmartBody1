package com.smartbody.presenter.prediccion

import com.smartbody.model.dto.PrediccionRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Validacion en la app antes de llamar al backend: datos del perfil, peso objetivo y porcentajes. */
class ConstructorPeticionPrediccionTest {

    private val completo = DatosBase(
        pesoActualKg = 90.0,
        alturaCm = 170.0,
        edad = 28,
        sexo = "MASCULINO",
        objetivoCodigo = "PERDER_PESO",
        nivelExperiencia = "INTERMEDIO",
        minutosPorSesion = 45,
        diasPorSemana = 4,
        restricciones = 0,
        caloriasObjetivo = 2430
    )

    private val entradaValida = EntradaPrediccion(
        pesoObjetivo = "80",
        nivelActividad = "MODERADO",
        tipoActividad = "MIXTO",
        adherenciaNutricional = "90",
        adherenciaEjercicio = "85"
    )

    @Test
    fun `arma la peticion con los datos del perfil y del plan`() {
        val resultado = ConstructorPeticionPrediccion.construir(completo, entradaValida)

        assertTrue(resultado is ConstruccionPeticion.Lista)
        val peticion: PrediccionRequest = (resultado as ConstruccionPeticion.Lista).peticion
        assertEquals(90.0, peticion.pesoInicial, 0.0)
        assertEquals(80.0, peticion.pesoObjetivo, 0.0)
        assertEquals(2430.0, peticion.caloriasObjetivo, 0.0)
        assertEquals(4, peticion.entrenamientosSemana)
        assertEquals("PERDER_PESO", peticion.objetivo)
    }

    @Test
    fun `avisa del peso objetivo vacio o no numerico`() {
        val resultado = ConstructorPeticionPrediccion.construir(completo, entradaValida.copy(pesoObjetivo = "abc"))

        assertTrue(resultado is ConstruccionPeticion.Incompleta)
        assertTrue((resultado as ConstruccionPeticion.Incompleta).errores.containsKey("pesoObjetivo"))
    }

    @Test
    fun `rechaza porcentajes de adherencia fuera de 0 a 100`() {
        val resultado = ConstructorPeticionPrediccion.construir(completo, entradaValida.copy(adherenciaEjercicio = "150"))

        assertTrue(resultado is ConstruccionPeticion.Incompleta)
        assertTrue((resultado as ConstruccionPeticion.Incompleta).errores.containsKey("adherenciaEjercicio"))
    }

    @Test
    fun `lista lo que falta en el perfil en lugar de enviar datos incompletos`() {
        val sinPlan = completo.copy(caloriasObjetivo = null, pesoActualKg = null)

        val resultado = ConstructorPeticionPrediccion.construir(sinPlan, entradaValida)

        assertTrue(resultado is ConstruccionPeticion.Incompleta)
        val faltantes = (resultado as ConstruccionPeticion.Incompleta).faltantes
        assertTrue(faltantes.any { it.contains("peso", ignoreCase = true) })
        assertTrue(faltantes.any { it.contains("plan nutricional", ignoreCase = true) })
    }

    @Test
    fun `no estima para objetivos que el modelo no cubre`() {
        val mantener = completo.copy(objetivoCodigo = "MANTENER_PESO")

        val resultado = ConstructorPeticionPrediccion.construir(mantener, entradaValida)

        assertTrue(resultado is ConstruccionPeticion.Incompleta)
        assertTrue((resultado as ConstruccionPeticion.Incompleta).faltantes.any { it.contains("perder peso o ganar masa") })
    }

    @Test
    fun `trata mas de tres restricciones como tres, el maximo del entrenamiento`() {
        val resultado = ConstructorPeticionPrediccion.construir(completo.copy(restricciones = 5), entradaValida)

        assertEquals(3, (resultado as ConstruccionPeticion.Lista).peticion.restriccionesTotal)
    }
}
