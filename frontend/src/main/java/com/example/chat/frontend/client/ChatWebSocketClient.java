package com.example.chat.frontend.client;

import com.example.chat.frontend.auth.AuthSession;
import com.example.chat.frontend.dto.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ChatWebSocketClient implements AutoCloseable {

    private final AuthSession authSession;
    private final String brokerUrl;
    private final WebSocketStompClient stompClient;
    private final Consumer<Message> onMessage;
    private final Consumer<List<String>> onUsersUpdated;
    private final Consumer<String> onStatusChanged;

    private volatile StompSession session;

    public ChatWebSocketClient(AuthSession authSession,
                               Consumer<Message> onMessage,
                               Consumer<List<String>> onUsersUpdated,
                               Consumer<String> onStatusChanged) {
        this.authSession = authSession;
        this.brokerUrl = buildBrokerUrl(authSession);
        this.onMessage = onMessage;
        this.onUsersUpdated = onUsersUpdated;
        this.onStatusChanged = onStatusChanged;
        this.stompClient = createStompClient();
    }

    public void connect() {
        onStatusChanged.accept("Conectando...");
        CompletableFuture<StompSession> connection = stompClient.connectAsync(brokerUrl, new SessionHandler());
        connection.exceptionally(error -> {
            onStatusChanged.accept("Falha na conexão: " + rootMessage(error));
            return null;
        });
    }

    public void sendMessage(String content) {
        StompSession currentSession = session;
        if (currentSession == null || !currentSession.isConnected()) {
            onStatusChanged.accept("Ainda não conectado ao servidor.");
            return;
        }
        currentSession.send("/app/mensagens", new Message(authSession.nomeUsuario(), content, null));
    }

    private String buildBrokerUrl(AuthSession session) {
        return "http://localhost:8080/ws?access_token="
                + URLEncoder.encode(session.token(), StandardCharsets.UTF_8);
    }

    private WebSocketStompClient createStompClient() {
        List<Transport> transports = List.of(
                new WebSocketTransport(new StandardWebSocketClient())
        );
        WebSocketStompClient client = new WebSocketStompClient(new SockJsClient(transports));
        client.setMessageConverter(createMessageConverter());
        return client;
    }

    private MessageConverter createMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new MappingJackson2MessageConverter(objectMapper);
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    @Override
    public void close() {
        StompSession currentSession = session;
        if (currentSession != null && currentSession.isConnected()) {
            currentSession.disconnect();
        }
        stompClient.stop();
    }

    private class SessionHandler extends StompSessionHandlerAdapter {

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            ChatWebSocketClient.this.session = session;
            onStatusChanged.accept("Conectado");
            session.subscribe("/topic/mensagens", new MessageHandler());
            session.subscribe("/topic/usuarios", new UserListHandler());
            session.send("/app/presenca/entrar", new ChatPresence());
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            ChatWebSocketClient.this.session = null;
            onStatusChanged.accept("Conexão encerrada: " + rootMessage(exception));
        }

        @Override
        public void handleException(StompSession session,
                                    StompCommand command,
                                    StompHeaders headers,
                                    byte[] payload,
                                    Throwable exception) {
            onStatusChanged.accept("Erro STOMP: " + rootMessage(exception));
        }
    }

    private class MessageHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Message.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            onMessage.accept((Message) payload);
        }
    }

    private class UserListHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders headers) {
            return List.class;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleFrame(StompHeaders headers, Object payload) {
            onUsersUpdated.accept((List<String>) payload);
        }
    }

    private record ChatPresence() {
    }
}
