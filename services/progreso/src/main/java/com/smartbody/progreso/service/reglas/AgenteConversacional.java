package com.smartbody.progreso.service.reglas;

/**
 * Asistente del chatbot. El MVP responde con reglas sobre los datos del usuario; un agente de IA
 * podra implementar esta interfaz desde el backend sin cambiar la aplicacion ni el controller.
 */
public interface AgenteConversacional {

    RespuestaChat responder(String mensaje, ContextoChat contexto);
}
