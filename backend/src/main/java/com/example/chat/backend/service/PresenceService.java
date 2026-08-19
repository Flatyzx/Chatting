package com.example.chat.backend.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PresenceService {

    private final Map<String, String> sessionsById = new LinkedHashMap<>();

    public synchronized void connect(String sessionId, String username) {
        sessionsById.put(sessionId, username);
    }

    public synchronized boolean disconnect(String sessionId) {
        return sessionsById.remove(sessionId) != null;
    }

    public synchronized List<String> getConnectedUsers() {
        return new ArrayList<>(sessionsById.values());
    }
}
