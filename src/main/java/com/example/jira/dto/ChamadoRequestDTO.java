package com.example.jira.dto;

import java.util.Set;

import com.example.jira.enums.Escopo;
import com.example.jira.enums.Prioridade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ChamadoRequestDTO {

    @NotBlank
    private String titulo;
    @NotBlank
    private String descricao;
    @NotNull
    private Long portalId;
    @NotNull
    private Integer categoriaId;
    private Integer subtopicoId;
    private String outroSubtopico;
    @NotNull
    private Prioridade prioridade;
    @NotNull
    private Escopo escopo;

    private Set<Integer> timeIds;
}
