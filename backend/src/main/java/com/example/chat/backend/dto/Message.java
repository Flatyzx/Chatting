package com.example.chat.backend.dto;

import java.time.LocalDateTime;

public record Message(String remetente, String conteudo, LocalDateTime horario) {
}
