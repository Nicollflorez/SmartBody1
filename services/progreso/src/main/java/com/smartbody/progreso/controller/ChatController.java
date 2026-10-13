package com.smartbody.progreso.controller;

import com.smartbody.comun.seguridad.UsuarioAutenticado;
import com.smartbody.progreso.dto.MensajeChatRequest;
import com.smartbody.progreso.service.ChatService;
import com.smartbody.progreso.service.reglas.RespuestaChat;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Chatbot: responde dudas sobre la rutina, la nutricion y el uso de la aplicacion. */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<RespuestaChat> responder(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                   @Valid @RequestBody MensajeChatRequest peticion) {
        return ResponseEntity.ok(chatService.responder(usuario, peticion.mensaje()));
    }
}
