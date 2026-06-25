package com.example.jira.dto;

import com.example.jira.model.Categoria;

public record CategoriaResumoDTO(Integer id, String nome) {
    public static CategoriaResumoDTO from(Categoria c) {
        return new CategoriaResumoDTO(c.getId(), c.getNome());
    }
}