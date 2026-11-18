package com.smartbody.progreso.service;

import com.smartbody.comun.error.ApiException;
import com.smartbody.comun.seguridad.UsuarioAutenticado;
import com.smartbody.progreso.cliente.ClienteUsuarioYPlanes;
import com.smartbody.progreso.dto.PerfilResponse;
import com.smartbody.progreso.service.reglas.AgenteConversacional;
import com.smartbody.progreso.service.reglas.ContextoChat;
import com.smartbody.progreso.service.reglas.RespuestaChat;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * Chatbot de Smartbody. Reune los datos del usuario autenticado (cuenta y perfil en auth, rutina y
 * plan en planes, progreso aqui) y delega la respuesta al agente. La ausencia de rutina o plan
 * no invalida las demas consultas.
 */
@Service
public class ChatService {

    private final ClienteUsuarioYPlanes cliente;
    private final ProgresoService progresoService;
    private final AgenteConversacional agente;

    public ChatService(ClienteUsuarioYPlanes cliente, ProgresoService progresoService, AgenteConversacional agente) {
        this.cliente = cliente;
        this.progresoService = progresoService;
        this.agente = agente;
    }

    public RespuestaChat responder(UsuarioAutenticado usuario, String mensaje) {
        String nombre = cliente.usuarioActual().nombre();
        PerfilResponse perfil = cliente.perfilActual();
        ContextoChat contexto = new ContextoChat(
                nombre,
                perfil,
                cliente.rutinaActiva().orElse(null),
                cliente.planActivo().orElse(null),
                perfil != null && perfil.completo() ? siExiste(() -> cliente.hidratacion().orElse(null)) : null,
                siExiste(() -> progresoService.consultar(usuario.id())));
        return agente.responder(mensaje.trim(), contexto);
    }

    /** Datos opcionales: si el usuario aun no los tiene, el asistente lo indica en su respuesta. */
    private static <T> T siExiste(Supplier<T> consulta) {
        try {
            return consulta.get();
        } catch (ApiException e) {
            return null;
        }
    }
}
