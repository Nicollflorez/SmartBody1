package com.smartbody.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Pregunta del usuario al asistente. */
public record MensajeChatRequest(

        @NotBlank(message = "Escribe un mensaje")
        @Size(max = 500, message = "El mensaje no puede superar 500 caracteres")
        String mensaje) {
}
