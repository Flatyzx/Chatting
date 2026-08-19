package com.example.chat.backend.auth;

public record AuthResponse(String token,
                           String tipo,
                           String nomeUsuario,
                           long expiraEmSegundos) {
}
