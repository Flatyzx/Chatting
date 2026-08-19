package com.example.chat.frontend.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class AuthApiClient {

    private final URI baseUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AuthApiClient(String baseUrl) {
        this.baseUri = URI.create(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public AuthSession login(String nomeUsuario, char[] senha) throws AuthApiException {
        ObjectNode body = objectMapper.createObjectNode()
                .put("nomeUsuario", nomeUsuario == null ? "" : nomeUsuario.trim())
                .put("senha", new String(senha == null ? new char[0] : senha));
        HttpResponse<String> response = post("auth/login", body);
        ensureSuccess(response, 200);
        try {
            return objectMapper.readValue(response.body(), AuthSession.class);
        } catch (IOException exception) {
            throw new AuthApiException("A resposta de login do servidor é inválida.", response.statusCode(), exception);
        }
    }

    public void register(String nomeUsuario, char[] senha) throws AuthApiException {
        ObjectNode body = objectMapper.createObjectNode()
                .put("nomeUsuario", nomeUsuario == null ? "" : nomeUsuario.trim())
                .put("senha", new String(senha == null ? new char[0] : senha));
        HttpResponse<String> response = post("auth/registrar", body);
        ensureSuccess(response, 201);
    }

    private HttpResponse<String> post(String path, ObjectNode body) throws AuthApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder(baseUri.resolve(path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
                    .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new AuthApiException("Não foi possível conectar ao servidor. Verifique se o backend está em execução.", 0, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AuthApiException("A comunicação com o servidor foi interrompida.", 0, exception);
        }
    }

    private void ensureSuccess(HttpResponse<String> response, int expectedStatus) throws AuthApiException {
        if (response.statusCode() == expectedStatus) {
            return;
        }

        String message = extractErrorMessage(response.body());
        throw new AuthApiException(message, response.statusCode());
    }

    private String extractErrorMessage(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode message = root.get("mensagem");
            if (message != null && !message.asText().isBlank()) {
                return message.asText();
            }
        } catch (IOException ignored) {
            // Usa a mensagem genérica abaixo quando o corpo não é JSON.
        }
        return "O servidor recusou a operação. Tente novamente.";
    }

    public static class AuthApiException extends Exception {
        private final int statusCode;

        public AuthApiException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public AuthApiException(String message, int statusCode, Throwable cause) {
            super(message, cause);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
