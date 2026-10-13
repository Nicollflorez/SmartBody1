package com.smartbody.presenter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Pruebas de las validaciones de formulario de la aplicacion (seccion 29). */
class ValidacionesTest {

    @Test
    fun `acepta un correo con formato valido`() {
        assertNull(Validaciones.validarEmail("demo@smartbody.com"))
    }

    @Test
    fun `rechaza correos con formato invalido`() {
        assertNotNull(Validaciones.validarEmail("demo"))
        assertNotNull(Validaciones.validarEmail("demo@"))
        assertNotNull(Validaciones.validarEmail("demo@smartbody"))
        assertNotNull(Validaciones.validarEmail("@smartbody.com"))
    }

    @Test
    fun `rechaza el correo vacio`() {
        assertEquals("Ingresa tu correo", Validaciones.validarEmail("   "))
    }

    @Test
    fun `exige una contrasena de al menos ocho caracteres`() {
        assertNotNull(Validaciones.validarPassword("1234567"))
        assertNull(Validaciones.validarPassword("Clave12345"))
    }

    @Test
    fun `valida el nombre`() {
        assertNotNull(Validaciones.validarNombre(""))
        assertNotNull(Validaciones.validarNombre("A"))
        assertNull(Validaciones.validarNombre("Ana"))
    }

    @Test
    fun `detecta contrasenas que no coinciden`() {
        assertNotNull(Validaciones.validarConfirmacion("Clave12345", "Clave54321"))
        assertNull(Validaciones.validarConfirmacion("Clave12345", "Clave12345"))
    }

    @Test
    fun `acepta coma o punto como separador decimal`() {
        assertEquals(75.5, Validaciones.leerDecimal("75,5")!!, 0.001)
        assertEquals(75.5, Validaciones.leerDecimal(" 75.5 ")!!, 0.001)
        assertNull(Validaciones.leerDecimal("setenta"))
    }

    @Test
    fun `valida la edad dentro de un rango razonable`() {
        assertNotNull(Validaciones.validarEdad(""))
        assertNotNull(Validaciones.validarEdad("11"))
        assertNotNull(Validaciones.validarEdad("101"))
        assertNull(Validaciones.validarEdad("25"))
    }

    @Test
    fun `el peso y la altura deben ser mayores que cero y razonables`() {
        assertNotNull(Validaciones.validarPeso("0"))
        assertNotNull(Validaciones.validarPeso("-70"))
        assertNotNull(Validaciones.validarPeso(""))
        assertNull(Validaciones.validarPeso("70,5"))
        assertNotNull(Validaciones.validarAltura("50"))
        assertNull(Validaciones.validarAltura("172"))
    }

    @Test
    fun `las medidas opcionales pueden quedar vacias`() {
        assertNull(Validaciones.validarMedidaOpcional("", "cintura"))
        assertNotNull(Validaciones.validarMedidaOpcional("5", "cintura", minimo = 30.0))
        assertNull(Validaciones.validarMedidaOpcional("88,5", "cintura", minimo = 30.0))
    }
}
