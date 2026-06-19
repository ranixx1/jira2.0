package com.example.jira.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "comentario")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String mensagem;

    private LocalDateTime dataHoraCriacao;

    // 👇 usuário vindo do JWT
    private Long userId;

    private String username;

    // relacionamento com chamado
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id")
    private Chamado chamado;

    // =========================
    // CONSTRUTOR
    // =========================
    protected Comentario() {}

    public Comentario(String mensagem, Long userId, String username, Chamado chamado) {
        this.mensagem = mensagem;
        this.userId = userId;
        this.username = username;
        this.chamado = chamado;
        this.dataHoraCriacao = LocalDateTime.now();
    }

    // =========================
    // GETTERS
    // =========================
    public Integer getId() {
        return id;
    }

    public String getMensagem() {
        return mensagem;
    }

    public LocalDateTime getDataHoraCriacao() {
        return dataHoraCriacao;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Chamado getChamado() {
        return chamado;
    }

    // =========================
    // SETTER CHAMADO
    // =========================
    public void setChamado(Chamado chamado) {
        this.chamado = chamado;
    }

    // =========================
    // TO STRING
    // =========================
    @Override
    public String toString() {
        return """
                comentário
                ├─ Autor     : %s
                ├─ Mensagem  : %s
                ├─ Data/Hora : %s
                """.formatted(
                this.username,
                this.mensagem,
                this.dataHoraCriacao
        );
    }
}