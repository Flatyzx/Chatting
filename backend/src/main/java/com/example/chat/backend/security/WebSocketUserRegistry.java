package com.example.chat.backend.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketUserRegistry {

    private final Map<String, String> usernamesBySession = new ConcurrentHashMap<>();

    public void register(String sessionId, String username) {
        if (sessionId != null && username != null && !username.isBlank()) {
            usernamesBySession.put(sessionId, username);
        }
    }

    public String findUsername(String sessionId) {
        return sessionId == null ? null : usernamesBySession.get(sessionId);
    }

    public void remove(String sessionId) {
        if (sessionId != null) {
            usernamesBySession.remove(sessionId);
        }
    }
}
