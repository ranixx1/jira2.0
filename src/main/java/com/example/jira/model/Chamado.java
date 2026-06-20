package com.example.jira.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import com.example.jira.enums.Escopo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.example.jira.enums.Status;
import com.example.jira.enums.Prioridade;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Chamado {

    public Chamado() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "subtopico_id")
    private Subtopico subtopico;

    @Column(name = "outro_subtopico")
    private String outroSubtopico;

    @Enumerated(EnumType.STRING)
    private Prioridade prioridade;

    private Long userId;

    @Column(name = "horario_abertura")
    private LocalDateTime horario_abertura;

    @Column(name = "horario_atualizacao") // adicionar usúario que fez a ultima atualização
    private LocalDateTime horario_atualizacao;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "titulo")
    @NotNull(message = "O titulo é obrigatório")
    private String titulo;

    @Column(name = "descricao")
    @NotNull(message = "A descrição é obrigatória")
    private String descricao;

    @Enumerated(EnumType.STRING)
    private Escopo escopo;

    @OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = false)
    private List<Comentario> comentarios = new ArrayList<>();

    public Chamado(
            Categoria categoria,
            Subtopico subtopico,
            Prioridade prioridade,
            Status status,
            String titulo,
            String descricao,
            Escopo escopo) {

        this.categoria = categoria;
        this.subtopico = subtopico;
        this.prioridade = prioridade;
        this.status = status;
        this.titulo = titulo;
        this.descricao = descricao;
        this.escopo = escopo;
    }

    public void adicionarComentario(Comentario comentario) {
        comentario.setChamado(this);
        this.comentarios.add(comentario);
        this.horario_atualizacao = LocalDateTime.now();
    }

    public void fechar() {
        if (this.status == Status.FECHADO) {
            throw new IllegalStateException("Chamado já está fechado");
        }

        this.status = Status.FECHADO;
        this.horario_atualizacao = LocalDateTime.now();
    }

    public void alterarStatus(Status novoStatus) {
        this.status = novoStatus;
    }

    @PrePersist
    public void prePersist() {
        this.horario_abertura = LocalDateTime.now();
        this.horario_atualizacao = LocalDateTime.now();
    }
}
