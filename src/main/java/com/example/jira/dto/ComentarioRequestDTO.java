package com.example.jira.dto;

import jakarta.validation.constraints.NotBlank;

public record ComentarioRequestDTO(
    @NotBlank(message = "A mensagem não pode estar em branco") 
    String mensagem
) {}