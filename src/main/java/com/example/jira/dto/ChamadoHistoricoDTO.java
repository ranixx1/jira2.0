package com.example.jira.dto;

import java.time.LocalDateTime;

import com.example.jira.model.ChamadoHistorico;

public record ChamadoHistoricoDTO(
        Integer id,
        String tipoEvento,
        String descricao,
        Long userId,
        String username,
        LocalDateTime dataHora
) {

    public static ChamadoHistoricoDTO from(ChamadoHistorico historico) {
        return new ChamadoHistoricoDTO(
                historico.getId(),
                historico.getTipoEvento(),
                historico.getDescricao(),
                historico.getUserId(),
                historico.getUsername(),
                historico.getDataHora()
        );
    }
}