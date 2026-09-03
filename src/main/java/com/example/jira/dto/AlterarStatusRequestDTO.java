package com.example.jira.dto;

import com.example.jira.enums.Status;

import jakarta.validation.constraints.NotNull;

public record AlterarStatusRequestDTO(@NotNull Status status) {
}
