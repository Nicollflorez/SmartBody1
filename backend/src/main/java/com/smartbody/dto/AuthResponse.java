package com.smartbody.dto;

/** Respuesta de registro/login: el cliente solo recibe el JWT, nunca las claves. */
public record AuthResponse(String token, String tipo, long expiraEnSegundos, UsuarioResponse usuario) {

    public static AuthResponse de(String token, long expiraEnSegundos, UsuarioResponse usuario) {
        return new AuthResponse(token, "Bearer", expiraEnSegundos, usuario);
    }
}
