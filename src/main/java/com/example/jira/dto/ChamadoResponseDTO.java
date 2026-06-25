package com.example.jira.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.jira.enums.Escopo;
import com.example.jira.enums.Prioridade;
import com.example.jira.enums.Status;
import com.example.jira.model.Chamado;

public record ChamadoResponseDTO(
    Integer id,
    String titulo,
    String descricao,
    Status status,
    Prioridade prioridade,
    Escopo escopo,
    Long userId,
    LocalDateTime horarioAbertura,
    LocalDateTime horarioAtualizacao,
    CategoriaResumoDTO categoria,
    SubtopicoResumoDTO subtopico,
    List<ComentarioDTO> comentarios
){
    public static ChamadoResponseDTO from(Chamado c) {
        return new ChamadoResponseDTO(
            c.getId(), c.getTitulo(), c.getDescricao(),
            c.getStatus(), c.getPrioridade(), c.getEscopo(),
            c.getUserId(), c.getHorario_abertura(), c.getHorario_atualizacao(),
            c.getCategoria() != null ? CategoriaResumoDTO.from(c.getCategoria()) : null,
            c.getSubtopico() != null ? SubtopicoResumoDTO.from(c.getSubtopico()) : null,
            c.getComentarios().stream().map(ComentarioDTO::from).toList()
        );
    }
}


