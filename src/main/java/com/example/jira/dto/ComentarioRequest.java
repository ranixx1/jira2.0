package com.example.jira.dto;

import jakarta.validation.constraints.NotBlank;

public record ComentarioRequest(@NotBlank String mensagem) {}

