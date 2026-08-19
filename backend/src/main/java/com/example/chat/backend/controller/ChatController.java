package com.example.chat.backend.controller;

import com.example.chat.backend.dto.Message;
import com.example.chat.backend.security.WebSocketUserRegistry;
import com.example.chat.backend.service.PresenceService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ChatController {

    private final PresenceService presenceService;
    private final WebSocketUserRegistry userRegistry;

    public ChatController(PresenceService presenceService,
                          WebSocketUserRegistry userRegistry) {
        this.presenceService = presenceService;
        this.userRegistry = userRegistry;
    }

    @MessageMapping("/mensagens")
    @SendTo("/topic/mensagens")
    public Message publicarMensagem(@Payload Message message,
                                    StompHeaderAccessor headers) {
        String remetente = authenticatedUsername(headers);
        String conteudo = sanitize(message == null ? null : message.conteudo());

        if (remetente.isBlank() || conteudo.isBlank()) {
            return null;
        }

        return new Message(remetente, conteudo, LocalDateTime.now());
    }

    @MessageMapping("/presenca/entrar")
    @SendTo("/topic/usuarios")
    public List<String> entrar(StompHeaderAccessor headers) {
        String username = authenticatedUsername(headers);
        if (!username.isBlank() && headers.getSessionId() != null) {
            presenceService.connect(headers.getSessionId(), username);
        }
        return presenceService.getConnectedUsers();
    }

    private String authenticatedUsername(StompHeaderAccessor headers) {
        String fromPrincipal = headers.getUser() == null ? "" : sanitize(headers.getUser().getName());
        if (!fromPrincipal.isBlank()) {
            return fromPrincipal;
        }
        return sanitize(userRegistry.findUsername(headers.getSessionId()));
    }

    private String sanitize(String value) {
        return value == null ? "" : value.trim();
    }
}
