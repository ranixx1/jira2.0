package com.example.jira.dto;

import com.example.jira.model.Portal;

public record PortalResumoDTO(
        Long id,
        String nome,
        String sigla
) {

    public static PortalResumoDTO from(Portal p) {
        return new PortalResumoDTO(
                p.getId(),
                p.getNome(),
                p.getCodigo()
        );
    }
}