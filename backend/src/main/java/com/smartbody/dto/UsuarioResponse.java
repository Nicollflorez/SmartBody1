package com.smartbody.dto;

import com.smartbody.entity.Usuario;

/** Datos publicos del usuario. Nunca incluye la contrasena ni su hash. */
public record UsuarioResponse(Long id, String nombre, String email, boolean perfilCompleto, boolean esAdministrador) {

    public static UsuarioResponse desde(Usuario usuario) {
        boolean completo = usuario.getPerfil() != null && usuario.getPerfil().estaCompleto();
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getEmail(), completo,
                usuario.esAdministrador());
    }
}
