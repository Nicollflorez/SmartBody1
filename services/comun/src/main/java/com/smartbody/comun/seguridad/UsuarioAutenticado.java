package com.smartbody.comun.seguridad;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * Identidad del usuario tomada de los claims del JWT. Ningun servicio distinto de auth
 * necesita consultar la tabla de usuarios para saber quien hace la peticion.
 */
public record UsuarioAutenticado(Long id, String email, List<String> roles, String token) {

    public static final String ROL_ADMINISTRADOR = "ROLE_ADMIN";

    public boolean esAdministrador() {
        return roles.contains(ROL_ADMINISTRADOR);
    }

    /** Usuario de la peticion en curso, o {@code null} si no hay token (rutas publicas). */
    public static UsuarioAutenticado actual() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null && autenticacion.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return usuario;
        }
        return null;
    }
}
