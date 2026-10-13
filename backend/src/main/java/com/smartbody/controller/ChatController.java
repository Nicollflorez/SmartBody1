package com.smartbody.controller;

import com.smartbody.dto.MensajeChatRequest;
import com.smartbody.service.ChatService;
import com.smartbody.service.reglas.RespuestaChat;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ResponseEntity<RespuestaChat> responder(@AuthenticationPrincipal UserDetails autenticado,
                                                   @Valid @RequestBody MensajeChatRequest peticion) {
        return ResponseEntity.ok(chatService.responder(autenticado.getUsername(), peticion.mensaje()));
    }
}
