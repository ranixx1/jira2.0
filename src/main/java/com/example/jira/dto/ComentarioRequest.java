package com.example.jira.dto;

import jakarta.validation.constraints.NotBlank;

public record ComentarioRequest(
    @NotBlank(message = "A mensagem não pode estar em branco") 
    String mensagem
) {}