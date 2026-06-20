package com.example.jira.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subtopico")
@Getter
@Setter
@NoArgsConstructor
public class Subtopico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nome;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    @JsonIgnore
    private Categoria categoria;

    public Subtopico(String nome, Categoria categoria) {
        this.nome = nome;
        this.categoria = categoria;
    }
}