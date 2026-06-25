package com.example.jira.dto;

import com.example.jira.model.Subtopico;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubtopicoResumoDTO {
    
    private Long id;
    private String nome;

    public static SubtopicoResumoDTO from(Subtopico s) {
        if (s == null) return null;
        return new SubtopicoResumoDTO(s.getId() == null ? null : s.getId().longValue(), s.getNome());
    }
}