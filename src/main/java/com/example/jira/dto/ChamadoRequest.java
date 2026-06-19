package com.example.jira.dto;

import com.example.jira.enums.Escopo;
import com.example.jira.enums.Prioridade;
import com.example.jira.enums.Tipo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ChamadoRequest {
    private Tipo tipo;
    private Prioridade prioridade;
    private String titulo;
    private String descricao;
    private Escopo escopo;
    
}
