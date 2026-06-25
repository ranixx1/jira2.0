package com.example.jira.dto;

import java.time.LocalDateTime;

import com.example.jira.model.Comentario;

public record ComentarioDTO(
    Integer id, String mensagem, String username, LocalDateTime dataHoraCriacao
) {
    public static ComentarioDTO from(Comentario c) {
        return new ComentarioDTO(c.getId(), c.getMensagem(), c.getUsername(), c.getDataHoraCriacao());
    }
}