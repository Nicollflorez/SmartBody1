package com.smartbody.service;

import com.smartbody.dto.PerfilResponse;
import com.smartbody.exception.ApiException;
import com.smartbody.service.reglas.AgenteConversacional;
import com.smartbody.service.reglas.ContextoChat;
import com.smartbody.service.reglas.RespuestaChat;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * Chatbot de Smartbody. Reune los datos del usuario autenticado y delega la respuesta al agente.
 * No es transaccional a proposito: cada consulta usa su propia transaccion de lectura, y la ausencia
 * de rutina o plan (404) no debe invalidar las demas.
 */
@Service
public class ChatService {

    private final UsuarioService usuarioService;
    private final PerfilService perfilService;
    private final RutinaService rutinaService;
    private final NutricionService nutricionService;
    private final ProgresoService progresoService;
    private final AgenteConversacional agente;

    public ChatService(UsuarioService usuarioService,
                       PerfilService perfilService,
                       RutinaService rutinaService,
                       NutricionService nutricionService,
                       ProgresoService progresoService,
                       AgenteConversacional agente) {
        this.usuarioService = usuarioService;
        this.perfilService = perfilService;
        this.rutinaService = rutinaService;
        this.nutricionService = nutricionService;
        this.progresoService = progresoService;
        this.agente = agente;
    }

    public RespuestaChat responder(String email, String mensaje) {
        String nombre = usuarioService.datosBasicos(email).nombre();
        PerfilResponse perfil = perfilService.consultar(email);

        ContextoChat contexto = new ContextoChat(
                nombre,
                perfil,
                siExiste(() -> rutinaService.activa(email)),
                siExiste(() -> nutricionService.activo(email)),
                perfil.completo() ? siExiste(() -> nutricionService.hidratacion(email)) : null,
                siExiste(() -> progresoService.consultar(email)));

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
