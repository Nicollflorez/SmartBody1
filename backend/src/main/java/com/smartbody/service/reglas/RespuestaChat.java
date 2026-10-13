package com.smartbody.service.reglas;

import java.util.List;

/** Respuesta del asistente con preguntas sugeridas para continuar la conversacion. */
public record RespuestaChat(String respuesta, List<String> sugerencias) {
}
