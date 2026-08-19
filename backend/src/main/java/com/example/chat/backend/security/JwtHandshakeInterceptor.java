package com.example.chat.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    public static final String USERNAME_ATTRIBUTE = "jwtUsername";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtHandshakeInterceptor(JwtService jwtService,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String token = extractToken(request);
        if (token == null || token.isBlank()) {
            reject(response);
            return false;
        }

        try {
            String username = jwtService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!jwtService.isTokenValid(token, userDetails)) {
                reject(response);
                return false;
            }
            attributes.put(USERNAME_ATTRIBUTE, username);
            return true;
        } catch (RuntimeException exception) {
            reject(response);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // Nenhuma ação adicional necessária.
    }

    private String extractToken(ServerHttpRequest request) {
        String queryToken = request.getURI().getQuery();
        if (queryToken != null) {
            for (String parameter : queryToken.split("&")) {
                String[] pair = parameter.split("=", 2);
                if (pair.length == 2 && "access_token".equals(pair[0])) {
                    return pair[1];
                }
            }
        }

        String authorization = request.getHeaders().getFirst("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    private void reject(ServerHttpResponse response) {
        if (response instanceof org.springframework.http.server.ServletServerHttpResponse servletResponse) {
            HttpServletResponse servlet = servletResponse.getServletResponse();
            servlet.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }
}
