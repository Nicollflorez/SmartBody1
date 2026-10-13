package com.smartbody.presenter.chat

import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas del saludo del chatbot. */
class ChatPresenterTest {

    @Test
    fun `saluda con el primer nombre del usuario`() {
        val saludo = ChatPresenter.bienvenida("nicoll yuherly florez")

        assertTrue(saludo.startsWith("¡Hola, Nicoll! 👋"))
        assertTrue(saludo.contains("💪"))
    }

    @Test
    fun `saluda aunque no conozca el nombre`() {
        assertTrue(ChatPresenter.bienvenida(null).startsWith("¡Hola! 👋"))
        assertTrue(ChatPresenter.bienvenida("  ").startsWith("¡Hola! 👋"))
    }
}
