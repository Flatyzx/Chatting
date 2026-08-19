package com.example.chat.frontend.dto;

import java.time.LocalDateTime;

public record Message(String remetente, String conteudo, LocalDateTime horario) {
}
