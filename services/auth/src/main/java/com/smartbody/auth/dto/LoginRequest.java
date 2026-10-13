package com.smartbody.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** RF02 - credenciales de acceso. */
public record LoginRequest(

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El formato del correo no es válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password) {
}
