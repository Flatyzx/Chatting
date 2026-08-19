package com.example.chat.backend.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final WebSocketUserRegistry userRegistry;

    public JwtChannelInterceptor(WebSocketUserRegistry userRegistry) {
        this.userRegistry = userRegistry;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Object username = accessor.getSessionAttributes() == null
                    ? null
                    : accessor.getSessionAttributes().get(JwtHandshakeInterceptor.USERNAME_ATTRIBUTE);
            if (!(username instanceof String authenticatedUsername) || authenticatedUsername.isBlank()) {
                throw new MessagingException("Conexão WebSocket não autenticada.");
            }

            accessor.setUser(new UsernamePasswordAuthenticationToken(
                    authenticatedUsername,
                    null,
                    List.of(() -> "ROLE_USER")
            ));
            userRegistry.register(accessor.getSessionId(), authenticatedUsername);
            accessor.setLeaveMutable(true);
            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        }

        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            userRegistry.remove(accessor.getSessionId());
        }
        return message;
    }
}
