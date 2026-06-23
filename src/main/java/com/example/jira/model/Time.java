package com.example.jira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "time")
@Getter
@Setter
@NoArgsConstructor
public class Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String nome;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "time_membros", joinColumns = @JoinColumn(name = "time_id"))
    @Column(name = "user_id")
    private Set<Long> membros = new HashSet<>();

    public Time(String nome) {
        this.nome = nome;
    }
}