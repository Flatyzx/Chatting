package com.example.chat.backend.service;

import com.example.chat.backend.security.WebSocketUserRegistry;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class PresenceEventListener {

    private final PresenceService presenceService;
    private final WebSocketUserRegistry userRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    public PresenceEventListener(PresenceService presenceService,
                                  WebSocketUserRegistry userRegistry,
                                  SimpMessagingTemplate messagingTemplate) {
        this.presenceService = presenceService;
        this.userRegistry = userRegistry;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        userRegistry.remove(event.getSessionId());
        if (presenceService.disconnect(event.getSessionId())) {
            messagingTemplate.convertAndSend("/topic/usuarios", presenceService.getConnectedUsers());
        }
    }
}
