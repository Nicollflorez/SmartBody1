package com.smartbody.planes.cliente;

import com.smartbody.comun.cliente.ClienteServicios;
import com.smartbody.comun.error.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Obtiene del servicio auth el perfil con el que se generan rutinas y planes. */
@Component
public class PerfilCliente {

    /** Usuario tal como lo expone auth en /internal/usuarios/{id}; solo interesa su perfil. */
    record UsuarioRemoto(Long id, String nombre, String email, PerfilRemoto perfil) {
    }

    private final ClienteServicios cliente;
    private final String urlAuth;

    public PerfilCliente(ClienteServicios cliente, @Value("${smartbody.servicios.auth}") String urlAuth) {
        this.cliente = cliente;
        this.urlAuth = urlAuth;
    }

    /** Perfil del usuario de la peticion en curso (con su propio token). */
    public PerfilRemoto delUsuarioActual() {
        return cliente.comoUsuario().get(urlAuth + "/api/users/profile", PerfilRemoto.class);
    }

    /** Perfil de cualquier usuario, para las operaciones internas del panel de administracion. */
    public PerfilRemoto deUsuario(Long usuarioId) {
        UsuarioRemoto usuario = cliente.interno().get(urlAuth + "/internal/usuarios/" + usuarioId, UsuarioRemoto.class);
        return usuario == null ? null : usuario.perfil();
    }

    /** Perfil listo para generar planes; si falta informacion se informa al usuario. */
    public static PerfilRemoto exigirCompleto(PerfilRemoto perfil) {
        if (perfil == null || !perfil.completo() || perfil.objetivo() == null) {
            throw ApiException.solicitudInvalida("Completa tu perfil antes de generar tu plan.");
        }
        return perfil;
    }
}
