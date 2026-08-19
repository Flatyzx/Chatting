package com.example.chat.frontend.auth;

public record AuthSession(String token,
                          String tipo,
                          String nomeUsuario,
                          long expiraEmSegundos) {

    public String authorizationHeader() {
        return tipo + " " + token;
    }
}
