package com.example.jira.dto;

import com.example.jira.enums.Escopo;
import com.example.jira.enums.Prioridade;
import com.example.jira.model.Categoria;
import com.example.jira.model.Subtopico;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ChamadoRequest {
    private Categoria categoria;
    private Subtopico subtopico;
    private Prioridade prioridade;
    private String titulo;
    private String descricao;
    private Escopo escopo;
    
}
