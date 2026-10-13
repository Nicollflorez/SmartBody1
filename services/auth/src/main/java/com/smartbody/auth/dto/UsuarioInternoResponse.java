package com.smartbody.auth.dto;

import com.smartbody.auth.entity.Usuario;

import java.time.LocalDateTime;

/** Vista completa de un usuario para el servicio admin (rutas /internal). */
public record UsuarioInternoResponse(Long id,
                                     String nombre,
                                     String email,
                                     boolean activo,
                                     boolean esAdministrador,
                                     LocalDateTime creadoEn,
                                     PerfilResponse perfil) {

    public static UsuarioInternoResponse desde(Usuario usuario) {
        return new UsuarioInternoResponse(usuario.getId(), usuario.getNombre(), usuario.getEmail(),
                usuario.isActivo(), usuario.esAdministrador(), usuario.getCreadoEn(),
                PerfilResponse.desde(usuario.getPerfil(), usuario.getRestricciones()));
    }
}
