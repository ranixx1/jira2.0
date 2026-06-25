package com.example.jira.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table(name = "comentario")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String mensagem;

    private LocalDateTime dataHoraCriacao;

    private Long userId;

    private String username;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id")
    private Chamado chamado;

    protected Comentario() {}

    public Comentario(String mensagem, Long userId, String username, Chamado chamado) {
        this.mensagem = mensagem;
        this.userId = userId;
        this.username = username;
        this.chamado = chamado;
        this.dataHoraCriacao = LocalDateTime.now();
    }
}