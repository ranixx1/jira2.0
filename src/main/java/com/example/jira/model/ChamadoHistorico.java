package com.example.jira.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ChamadoHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    private String tipoEvento;

    private String descricao;

    private Long userId;

    private String username;

    private LocalDateTime dataHora;

    protected ChamadoHistorico() {
    }

    public ChamadoHistorico(
            Chamado chamado,
            String tipoEvento,
            String descricao,
            Long userId,
            String username) {

        this.chamado = chamado;
        this.tipoEvento = tipoEvento;
        this.descricao = descricao;
        this.userId = userId;
        this.username = username;
        this.dataHora = LocalDateTime.now();
    }
}